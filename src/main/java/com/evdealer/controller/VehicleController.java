package com.evdealer.controller;

import com.evdealer.entity.VehicleBrand;
import com.evdealer.entity.VehicleModel;
import com.evdealer.entity.VehicleVariant;
import com.evdealer.entity.VehicleColor;
import com.evdealer.dto.VehicleModelRequest;
import com.evdealer.dto.VehicleVariantRequest;
import com.evdealer.dto.VehicleBrandRequest;
import com.evdealer.dto.VehicleColorRequest;
import com.evdealer.service.VehicleService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
@Tag(name = "Vehicle Management", description = "APIs quản lý xe")
public class VehicleController {
    
    @Autowired
    private VehicleService vehicleService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    // Vehicle Brand endpoints
    @GetMapping("/brands")
    @Operation(summary = "Lấy danh sách thương hiệu", description = "Lấy tất cả thương hiệu xe")
    public ResponseEntity<List<VehicleBrand>> getAllBrands() {
        List<VehicleBrand> brands = vehicleService.getAllBrands();
        return ResponseEntity.ok(brands);
    }
    
    @GetMapping("/brands/active")
    @Operation(summary = "Lấy thương hiệu đang hoạt động", description = "Lấy thương hiệu xe đang hoạt động")
    public ResponseEntity<List<VehicleBrand>> getActiveBrands() {
        List<VehicleBrand> brands = vehicleService.getActiveBrands();
        return ResponseEntity.ok(brands);
    }
    
    @GetMapping("/brands/{brandId}")
    @Operation(summary = "Lấy thương hiệu theo ID", description = "Lấy thông tin thương hiệu theo ID")
    public ResponseEntity<VehicleBrand> getBrandById(@PathVariable Integer brandId) {
        return vehicleService.getBrandById(brandId)
                .map(brand -> ResponseEntity.ok(brand))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/brands/name/{brandName}")
    public ResponseEntity<VehicleBrand> getBrandByName(@PathVariable String brandName) {
        return vehicleService.getBrandByName(brandName)
                .map(brand -> ResponseEntity.ok(brand))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/brands/country/{country}")
    public ResponseEntity<List<VehicleBrand>> getBrandsByCountry(@PathVariable String country) {
        List<VehicleBrand> brands = vehicleService.getBrandsByCountry(country);
        return ResponseEntity.ok(brands);
    }
    
    @GetMapping("/brands/search")
    public ResponseEntity<List<VehicleBrand>> searchBrandsByName(@RequestParam String name) {
        List<VehicleBrand> brands = vehicleService.searchBrandsByName(name);
        return ResponseEntity.ok(brands);
    }
    
    @PostMapping("/brands")
    @Operation(summary = "Tạo thương hiệu mới", description = "Tạo thương hiệu xe mới")
    public ResponseEntity<?> createBrand(@RequestBody VehicleBrandRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo brand
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create vehicle brands");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleBrand createdBrand = vehicleService.createBrandFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBrand);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create brand: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create brand: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/brands/{brandId}")
    @Operation(summary = "Cập nhật thương hiệu", description = "Cập nhật thông tin thương hiệu")
    public ResponseEntity<?> updateBrand(@PathVariable Integer brandId, @RequestBody VehicleBrandRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update brand
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update vehicle brands");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleBrand updatedBrand = vehicleService.updateBrandFromRequest(brandId, request);
            return ResponseEntity.ok(updatedBrand);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update brand: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update brand: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/brands/{brandId}")
    public ResponseEntity<?> deleteBrand(@PathVariable Integer brandId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa brand
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete vehicle brands");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            vehicleService.deleteBrand(brandId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Vehicle brand deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete brand: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete brand: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Vehicle Model endpoints
    @GetMapping("/models")
    @Operation(summary = "Lấy danh sách mẫu xe", description = "Lấy tất cả mẫu xe")
    public ResponseEntity<List<VehicleModel>> getAllModels() {
        try {
            List<VehicleModel> models = vehicleService.getAllModels();
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/models/active")
    @Operation(summary = "Lấy mẫu xe đang hoạt động", description = "Lấy mẫu xe đang hoạt động")
    public ResponseEntity<List<VehicleModel>> getActiveModels() {
        List<VehicleModel> models = vehicleService.getActiveModels();
        return ResponseEntity.ok(models);
    }
    
    @GetMapping("/models/{modelId}")
    public ResponseEntity<VehicleModel> getModelById(@PathVariable Integer modelId) {
        return vehicleService.getModelById(modelId)
                .map(model -> ResponseEntity.ok(model))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/models/brand/{brandId}")
    public ResponseEntity<List<VehicleModel>> getModelsByBrand(@PathVariable Integer brandId) {
        List<VehicleModel> models = vehicleService.getModelsByBrand(brandId);
        return ResponseEntity.ok(models);
    }
    
    @GetMapping("/models/brand/{brandId}/active")
    public ResponseEntity<List<VehicleModel>> getActiveModelsByBrand(@PathVariable Integer brandId) {
        List<VehicleModel> models = vehicleService.getActiveModelsByBrand(brandId);
        return ResponseEntity.ok(models);
    }
    
    @GetMapping("/models/search")
    public ResponseEntity<List<VehicleModel>> searchModelsByName(@RequestParam String name) {
        List<VehicleModel> models = vehicleService.searchModelsByName(name);
        return ResponseEntity.ok(models);
    }
    
    @GetMapping("/models/type/{vehicleType}")
    public ResponseEntity<List<VehicleModel>> getModelsByType(@PathVariable String vehicleType) {
        List<VehicleModel> models = vehicleService.getModelsByType(vehicleType);
        return ResponseEntity.ok(models);
    }
    
    @GetMapping("/models/year/{year}")
    public ResponseEntity<List<VehicleModel>> getModelsByYear(@PathVariable Integer year) {
        List<VehicleModel> models = vehicleService.getModelsByYear(year);
        return ResponseEntity.ok(models);
    }
    
    @PostMapping("/models")
    @Operation(summary = "Tạo mẫu xe mới", description = "Tạo mẫu xe mới")
    public ResponseEntity<?> createModel(@RequestBody VehicleModelRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo model
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create vehicle models");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleModel createdModel = vehicleService.createModelFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdModel);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create model: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create model: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/models/{modelId}")
    @Operation(summary = "Cập nhật mẫu xe", description = "Cập nhật thông tin mẫu xe")
    public ResponseEntity<?> updateModel(@PathVariable Integer modelId, @RequestBody VehicleModelRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update model
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update vehicle models");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleModel updatedModel = vehicleService.updateModelFromRequest(modelId, request);
            return ResponseEntity.ok(updatedModel);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update model: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update model: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/models/{modelId}")
    public ResponseEntity<?> deleteModel(@PathVariable Integer modelId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa model
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete vehicle models");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            vehicleService.deleteModel(modelId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Vehicle model deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete model: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete model: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Vehicle Variant endpoints
    @GetMapping("/variants")
    @Operation(summary = "Lấy danh sách phiên bản xe", description = "Lấy tất cả phiên bản xe")
    public ResponseEntity<List<VehicleVariant>> getAllVariants() {
        try {
            List<VehicleVariant> variants = vehicleService.getAllVariants();
            return ResponseEntity.ok(variants);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/variants/active")
    @Operation(summary = "Lấy phiên bản xe đang hoạt động", description = "Lấy phiên bản xe đang hoạt động")
    public ResponseEntity<List<VehicleVariant>> getActiveVariants() {
        List<VehicleVariant> variants = vehicleService.getActiveVariants();
        return ResponseEntity.ok(variants);
    }
    
    @GetMapping("/variants/{variantId}")
    public ResponseEntity<VehicleVariant> getVariantById(@PathVariable Integer variantId) {
        return vehicleService.getVariantById(variantId)
                .map(variant -> ResponseEntity.ok(variant))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/variants/model/{modelId}")
    public ResponseEntity<List<VehicleVariant>> getVariantsByModel(@PathVariable Integer modelId) {
        List<VehicleVariant> variants = vehicleService.getVariantsByModel(modelId);
        return ResponseEntity.ok(variants);
    }
    
    @GetMapping("/variants/model/{modelId}/active")
    public ResponseEntity<List<VehicleVariant>> getActiveVariantsByModel(@PathVariable Integer modelId) {
        List<VehicleVariant> variants = vehicleService.getActiveVariantsByModel(modelId);
        return ResponseEntity.ok(variants);
    }
    
    @GetMapping("/variants/search")
    public ResponseEntity<List<VehicleVariant>> searchVariantsByName(@RequestParam String name) {
        List<VehicleVariant> variants = vehicleService.searchVariantsByName(name);
        return ResponseEntity.ok(variants);
    }
    
    @GetMapping("/variants/price-range")
    public ResponseEntity<List<VehicleVariant>> getVariantsByPriceRange(
            @RequestParam BigDecimal minPrice, 
            @RequestParam BigDecimal maxPrice) {
        List<VehicleVariant> variants = vehicleService.getVariantsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(variants);
    }
    
    @GetMapping("/variants/min-range/{minRange}")
    public ResponseEntity<List<VehicleVariant>> getVariantsByMinRange(@PathVariable Integer minRange) {
        List<VehicleVariant> variants = vehicleService.getVariantsByMinRange(minRange);
        return ResponseEntity.ok(variants);
    }
    
    @PostMapping("/variants")
    @Operation(summary = "Tạo phiên bản xe mới", description = "Tạo phiên bản xe điện mới")
    public ResponseEntity<?> createVariant(@RequestBody VehicleVariantRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo variant
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create vehicle variants");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleVariant createdVariant = vehicleService.createVariantFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVariant);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create variant: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create variant: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/variants/{variantId}")
    @Operation(summary = "Cập nhật phiên bản xe", description = "Cập nhật thông tin phiên bản xe điện")
    public ResponseEntity<?> updateVariant(@PathVariable Integer variantId, @RequestBody VehicleVariantRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update variant
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update vehicle variants");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleVariant updatedVariant = vehicleService.updateVariantFromRequest(variantId, request);
            return ResponseEntity.ok(updatedVariant);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update variant: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update variant: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<?> deleteVariant(@PathVariable Integer variantId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa variant
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete vehicle variants");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            vehicleService.deleteVariant(variantId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Vehicle variant deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete variant: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete variant: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Vehicle Color endpoints
    @GetMapping("/colors")
    @Operation(summary = "Lấy danh sách màu sắc", description = "Lấy tất cả màu sắc xe")
    public ResponseEntity<List<VehicleColor>> getAllColors() {
        List<VehicleColor> colors = vehicleService.getAllColors();
        return ResponseEntity.ok(colors);
    }
    
    @GetMapping("/colors/active")
    @Operation(summary = "Lấy màu sắc đang hoạt động", description = "Lấy màu sắc xe đang hoạt động")
    public ResponseEntity<List<VehicleColor>> getActiveColors() {
        List<VehicleColor> colors = vehicleService.getActiveColors();
        return ResponseEntity.ok(colors);
    }
    
    @GetMapping("/colors/{colorId}")
    public ResponseEntity<VehicleColor> getColorById(@PathVariable Integer colorId) {
        return vehicleService.getColorById(colorId)
                .map(color -> ResponseEntity.ok(color))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/colors/name/{colorName}")
    public ResponseEntity<VehicleColor> getColorByName(@PathVariable String colorName) {
        return vehicleService.getColorByName(colorName)
                .map(color -> ResponseEntity.ok(color))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/colors/code/{colorCode}")
    public ResponseEntity<VehicleColor> getColorByCode(@PathVariable String colorCode) {
        return vehicleService.getColorByCode(colorCode)
                .map(color -> ResponseEntity.ok(color))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/colors/search")
    public ResponseEntity<List<VehicleColor>> searchColorsByName(@RequestParam String name) {
        List<VehicleColor> colors = vehicleService.searchColorsByName(name);
        return ResponseEntity.ok(colors);
    }
    
    @PostMapping("/colors")
    @Operation(summary = "Tạo màu sắc mới", description = "Tạo màu sắc xe mới")
    public ResponseEntity<?> createColor(@RequestBody VehicleColorRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo color
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create vehicle colors");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleColor createdColor = vehicleService.createColorFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdColor);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create color: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create color: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/colors/{colorId}")
    @Operation(summary = "Cập nhật màu sắc", description = "Cập nhật thông tin màu sắc")
    public ResponseEntity<?> updateColor(@PathVariable Integer colorId, @RequestBody VehicleColorRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update color
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update vehicle colors");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleColor updatedColor = vehicleService.updateColorFromRequest(colorId, request);
            return ResponseEntity.ok(updatedColor);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update color: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update color: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/colors/{colorId}")
    public ResponseEntity<?> deleteColor(@PathVariable Integer colorId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa color
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete vehicle colors");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            vehicleService.deleteColor(colorId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Vehicle color deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete color: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete color: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

