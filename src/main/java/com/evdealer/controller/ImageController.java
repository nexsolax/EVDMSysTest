package com.evdealer.controller;

import com.evdealer.entity.VehicleBrand;
import com.evdealer.entity.VehicleVariant;
import com.evdealer.entity.VehicleInventory;
import com.evdealer.repository.VehicleBrandRepository;
import com.evdealer.repository.VehicleVariantRepository;
import com.evdealer.repository.VehicleInventoryRepository;
import com.evdealer.service.FileUploadService;
import com.evdealer.service.ImageUpdateService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
@Tag(name = "Image Management", description = "APIs quản lý hình ảnh")
public class ImageController {
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private ImageUpdateService imageUpdateService;
    
    @Autowired
    private VehicleBrandRepository vehicleBrandRepository;
    
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    
    @Autowired
    private VehicleInventoryRepository vehicleInventoryRepository;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload hình ảnh", description = "Upload một hình ảnh duy nhất")
    public ResponseEntity<?> uploadImage(
            @Parameter(description = "File hình ảnh cần upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Danh mục lưu trữ (vehicles, brands, models, variants, colors)") @RequestParam("category") String category) {
        
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể upload hình ảnh
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can upload images");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            FileUploadService.FileUploadResult result = fileUploadService.uploadImage(file, category);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload nhiều hình ảnh", description = "Upload nhiều hình ảnh cùng lúc")
    public ResponseEntity<?> uploadMultipleImages(
            @Parameter(description = "Danh sách file hình ảnh cần upload") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "Danh mục lưu trữ") @RequestParam("category") String category) {
        
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể upload nhiều hình ảnh
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can upload images");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            FileUploadService.FileUploadResult result = fileUploadService.uploadMultipleImages(files, category);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping(value = "/upload/vehicle-brand", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload logo thương hiệu", description = "Upload logo cho thương hiệu xe. Tự động tạo thư mục theo tên brand")
    public ResponseEntity<?> uploadBrandLogo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "brandId", required = false) Integer brandId) {
        
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể upload brand logo
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can upload brand logos");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            String category = "brands";
            
            // If brandId is provided, get brand name and create subfolder
            if (brandId != null) {
                VehicleBrand brand = vehicleBrandRepository.findById(brandId).orElse(null);
                if (brand != null && brand.getBrandName() != null) {
                    // Sanitize brand name for folder name (lowercase, replace spaces and special chars with underscore)
                    String brandFolderName = sanitizeFolderName(brand.getBrandName());
                    category = "brands/" + brandFolderName;
                }
            }
            
            FileUploadService.FileUploadResult result = fileUploadService.uploadImage(file, category);
            
            // Update brand entity with image URLs if brandId is provided
            Map<String, Object> response = new HashMap<>();
            response.put("uploadResult", result);
            response.put("message", "Brand logo uploaded successfully");
            response.put("brandId", brandId);
            response.put("category", category);
            
            if (brandId != null) {
                // Create path from category and stored filename
                String imagePath = result.getCategory() + "/" + result.getStoredFilename();
                Map<String, Object> updateResult = imageUpdateService.updateBrandImage(
                        brandId, result.getUrl(), imagePath);
                response.put("updateResult", updateResult);
                
                if (!(Boolean) updateResult.get("success")) {
                    response.put("warning", "Logo uploaded but failed to update database: " + updateResult.get("message"));
                }
            } else {
                response.put("warning", "brandId not provided - logo uploaded to generic brands folder");
            }
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Brand logo upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Brand logo upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Sanitize string to be used as folder name
     * Converts to lowercase, replaces spaces and special characters with underscore
     */
    private String sanitizeFolderName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "unknown";
        }
        return name.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]", "_")  // Replace non-alphanumeric with underscore
                .replaceAll("_{2,}", "_")       // Replace multiple underscores with single
                .replaceAll("^_|_$", "");       // Remove leading/trailing underscores
    }
    
    @PostMapping(value = "/upload/vehicle-model", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload hình ảnh mẫu xe", description = "Upload hình ảnh cho mẫu xe")
    public ResponseEntity<?> uploadModelImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "modelId", required = false) Integer modelId) {
        
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể upload model image
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can upload model images");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            FileUploadService.FileUploadResult result = fileUploadService.uploadImage(file, "models");
            
            Map<String, Object> response = new HashMap<>();
            response.put("uploadResult", result);
            response.put("message", "Model image uploaded successfully");
            response.put("modelId", modelId);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Model image upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Model image upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping(value = "/upload/vehicle-variant", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload hình ảnh phiên bản xe", description = "Upload hình ảnh cho phiên bản xe. Tự động tạo thư mục theo tên variant")
    public ResponseEntity<?> uploadVariantImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "variantId", required = false) Integer variantId) {
        
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể upload variant image
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can upload variant images");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            String category = "variants";
            
            // If variantId is provided, get variant name and create subfolder
            if (variantId != null) {
                VehicleVariant variant = vehicleVariantRepository.findById(variantId).orElse(null);
                if (variant != null && variant.getVariantName() != null) {
                    // Sanitize variant name for folder name
                    String variantFolderName = sanitizeFolderName(variant.getVariantName());
                    category = "variants/" + variantFolderName;
                }
            }
            
            FileUploadService.FileUploadResult result = fileUploadService.uploadImage(file, category);
            
            // Update variant entity with image URLs if variantId is provided
            Map<String, Object> response = new HashMap<>();
            response.put("uploadResult", result);
            response.put("message", "Variant image uploaded successfully");
            response.put("variantId", variantId);
            response.put("category", category);
            
            if (variantId != null) {
                // Create path from category and stored filename
                String imagePath = result.getCategory() + "/" + result.getStoredFilename();
                Map<String, Object> updateResult = imageUpdateService.updateVariantImage(
                        variantId, result.getUrl(), imagePath);
                response.put("updateResult", updateResult);
                
                if (!(Boolean) updateResult.get("success")) {
                    response.put("warning", "Image uploaded but failed to update database: " + updateResult.get("message"));
                }
            } else {
                response.put("warning", "variantId not provided - image uploaded to generic variants folder");
            }
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Variant image upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Variant image upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping(value = "/upload/vehicle-inventory", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload hình ảnh xe trong kho", description = "Upload hình ảnh chi tiết cho xe trong kho. Tự động tạo thư mục theo VIN")
    public ResponseEntity<?> uploadInventoryImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("imageType") String imageType, // main, interior, exterior
            @RequestParam(value = "inventoryId", required = false) String inventoryId) {
        
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể upload inventory images
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can upload inventory images");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            String category = "inventory/" + imageType;
            
            // If inventoryId is provided, get inventory and create subfolder by VIN
            if (inventoryId != null && !inventoryId.trim().isEmpty()) {
                try {
                    UUID inventoryUuid = UUID.fromString(inventoryId);
                    VehicleInventory inventory = vehicleInventoryRepository.findById(inventoryUuid).orElse(null);
                    if (inventory != null) {
                        // Use VIN if available, otherwise use inventoryId
                        String vehicleIdentifier;
                        if (inventory.getVin() != null && !inventory.getVin().trim().isEmpty()) {
                            vehicleIdentifier = sanitizeFolderName(inventory.getVin());
                        } else {
                            vehicleIdentifier = inventoryId.replace("-", "_");
                        }
                        category = "inventory/" + imageType + "/" + vehicleIdentifier;
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid UUID format, continue with default category
                }
            }
            
            FileUploadService.FileUploadResult result = fileUploadService.uploadMultipleImages(files, category);
            
            // Update database with image URLs if inventoryId is provided
            if (inventoryId != null && !inventoryId.trim().isEmpty()) {
                try {
                    UUID inventoryUuid = UUID.fromString(inventoryId);
                    Map<String, Object> updateResult = imageUpdateService.updateInventoryImagesFromUploadResult(
                            inventoryUuid, imageType, result);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("uploadResult", result);
                    response.put("updateResult", updateResult);
                    response.put("message", "Inventory images uploaded and database updated successfully");
                    response.put("imageType", imageType);
                    response.put("inventoryId", inventoryId);
                    response.put("category", category);
                    
                    return ResponseEntity.ok(response);
                } catch (IllegalArgumentException e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid inventory ID format: " + inventoryId);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                } catch (Exception e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Failed to update database: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                }
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("uploadResult", result);
                response.put("message", "Inventory images uploaded successfully (database not updated - no inventoryId provided)");
                response.put("imageType", imageType);
                response.put("inventoryId", inventoryId);
                response.put("category", category);
                response.put("warning", "inventoryId not provided - images uploaded to generic folder");
                
                return ResponseEntity.ok(response);
            }
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Inventory images upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping(value = "/upload/color-swatch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload mẫu màu xe", description = "Upload mẫu màu cho màu xe")
    public ResponseEntity<?> uploadColorSwatch(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "colorId", required = false) Integer colorId) {
        
        try {
            FileUploadService.FileUploadResult result = fileUploadService.uploadImage(file, "colors");
            
            Map<String, Object> response = new HashMap<>();
            response.put("uploadResult", result);
            response.put("message", "Color swatch uploaded successfully");
            response.put("colorId", colorId);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Color swatch upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Color swatch upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/delete/{category}/{filename}")
    @Operation(summary = "Xóa hình ảnh", description = "Xóa hình ảnh theo danh mục và tên file")
    public ResponseEntity<?> deleteImage(
            @PathVariable String category,
            @PathVariable String filename) {
        
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể xóa hình ảnh
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can delete images");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            boolean deleted = fileUploadService.deleteImage(category, filename);
            
            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Image deleted successfully");
                response.put("category", category);
                response.put("filename", filename);
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Failed to delete image");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Delete failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/delete-category/{category}")
    @Operation(summary = "Xóa toàn bộ danh mục hình ảnh", description = "Xóa toàn bộ hình ảnh trong một danh mục")
    public ResponseEntity<?> deleteImageCategory(@PathVariable String category) {
        
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa toàn bộ danh mục hình ảnh
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete entire image categories");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            boolean deleted = fileUploadService.deleteImageDirectory(category);
            
            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Category images deleted successfully");
                response.put("category", category);
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Failed to delete category images");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Delete failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // ==================== READ APIs ====================
    
    @GetMapping("/list")
    @Operation(summary = "Lấy danh sách hình ảnh", description = "Lấy danh sách tất cả hình ảnh theo danh mục")
    public ResponseEntity<?> listImages(
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        
        try {
            Map<String, Object> result = fileUploadService.listImages(category, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to list images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/list/{category}")
    @Operation(summary = "Lấy hình ảnh theo danh mục", description = "Lấy danh sách hình ảnh trong một danh mục cụ thể")
    public ResponseEntity<?> listImagesByCategory(
            @PathVariable String category,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        
        try {
            Map<String, Object> result = fileUploadService.listImages(category, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to list images for category " + category + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/info/{category}/{filename}")
    @Operation(summary = "Thông tin hình ảnh", description = "Lấy thông tin chi tiết của một hình ảnh")
    public ResponseEntity<?> getImageInfo(
            @PathVariable String category,
            @PathVariable String filename) {
        
        try {
            Map<String, Object> info = fileUploadService.getImageInfo(category, filename);
            if (info != null) {
                return ResponseEntity.ok(info);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Image not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get image info: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm hình ảnh", description = "Tìm kiếm hình ảnh theo tên file hoặc danh mục")
    public ResponseEntity<?> searchImages(
            @RequestParam(required = false) String filename,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        
        try {
            Map<String, Object> result = fileUploadService.searchImages(filename, category, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Search failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // ==================== UPDATE APIs ====================
    
    @PutMapping(value = "/update/{category}/{filename}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật hình ảnh", description = "Thay thế hình ảnh hiện tại bằng hình ảnh mới")
    public ResponseEntity<?> updateImage(
            @PathVariable String category,
            @PathVariable String filename,
            @RequestParam("file") MultipartFile newFile) {
        
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update hình ảnh
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update images");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // First delete the old image
            boolean deleted = fileUploadService.deleteImage(category, filename);
            if (!deleted) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Failed to delete old image");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Upload the new image with the same filename
            FileUploadService.FileUploadResult result = fileUploadService.uploadImageWithName(newFile, category, filename);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image updated successfully");
            response.put("category", category);
            response.put("filename", filename);
            response.put("uploadResult", result);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Image update failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Image update failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/rename/{category}/{oldFilename}")
    @Operation(summary = "Đổi tên hình ảnh", description = "Đổi tên file hình ảnh")
    public ResponseEntity<?> renameImage(
            @PathVariable String category,
            @PathVariable String oldFilename,
            @RequestParam String newFilename) {
        
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể rename hình ảnh
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can rename images");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            boolean renamed = fileUploadService.renameImage(category, oldFilename, newFilename);
            
            if (renamed) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Image renamed successfully");
                response.put("category", category);
                response.put("oldFilename", oldFilename);
                response.put("newFilename", newFilename);
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Failed to rename image");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Rename failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/move/{oldCategory}/{filename}")
    @Operation(summary = "Di chuyển hình ảnh", description = "Di chuyển hình ảnh sang danh mục khác")
    public ResponseEntity<?> moveImage(
            @PathVariable String oldCategory,
            @PathVariable String filename,
            @RequestParam String newCategory) {
        
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể move hình ảnh
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can move images");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            boolean moved = fileUploadService.moveImage(oldCategory, filename, newCategory);
            
            if (moved) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Image moved successfully");
                response.put("oldCategory", oldCategory);
                response.put("newCategory", newCategory);
                response.put("filename", filename);
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Failed to move image");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Move failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // ==================== BULK OPERATIONS ====================
    
    @PostMapping("/bulk-delete")
    @Operation(summary = "Xóa nhiều hình ảnh", description = "Xóa nhiều hình ảnh cùng lúc")
    public ResponseEntity<?> bulkDeleteImages(@RequestBody Map<String, Object> request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể bulk delete images
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can bulk delete images");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, String>> images = (java.util.List<Map<String, String>>) request.get("images");
            
            if (images == null || images.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No images specified for deletion");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Map<String, Object> result = fileUploadService.bulkDeleteImages(images);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bulk delete failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/bulk-move")
    @Operation(summary = "Di chuyển nhiều hình ảnh", description = "Di chuyển nhiều hình ảnh sang danh mục khác")
    public ResponseEntity<?> bulkMoveImages(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, String>> images = (java.util.List<Map<String, String>>) request.get("images");
            String newCategory = (String) request.get("newCategory");
            
            if (images == null || images.isEmpty() || newCategory == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Images and newCategory are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Map<String, Object> result = fileUploadService.bulkMoveImages(images, newCategory);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bulk move failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // ==================== STATISTICS ====================
    
    @GetMapping("/stats")
    @Operation(summary = "Thống kê hình ảnh", description = "Lấy thống kê về hình ảnh trong hệ thống")
    public ResponseEntity<?> getImageStats() {
        try {
            Map<String, Object> stats = fileUploadService.getImageStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get image stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/stats/{category}")
    @Operation(summary = "Thống kê theo danh mục", description = "Lấy thống kê hình ảnh theo danh mục")
    public ResponseEntity<?> getImageStatsByCategory(@PathVariable String category) {
        try {
            Map<String, Object> stats = fileUploadService.getImageStatsByCategory(category);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get image stats for category " + category + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // ==================== UTILITY APIs ====================
    
    @GetMapping("/info")
    @Operation(summary = "Thông tin upload", description = "Lấy thông tin cấu hình upload")
    public ResponseEntity<?> getUploadInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("maxFileSize", "10MB");
            info.put("allowedExtensions", "jpg, jpeg, png, gif, webp");
            info.put("allowedTypes", "image/jpeg, image/jpg, image/png, image/gif, image/webp");
            info.put("maxDimensions", "1920x1080");
            info.put("thumbnailDimensions", "300x200");
            info.put("categories", new String[]{"vehicles", "brands", "models", "variants", "colors", "inventory"});
            
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve upload info: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
