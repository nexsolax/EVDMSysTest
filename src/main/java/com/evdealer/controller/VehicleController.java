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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
@Tag(name = "Vehicle Management", description = "APIs quản lý xe")
public class VehicleController {
    
    @Autowired
    private VehicleService vehicleService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    // Helper methods to convert entities to Map
    private Map<String, Object> brandToMap(VehicleBrand brand) {
        Map<String, Object> map = new HashMap<>();
        map.put("brandId", brand.getBrandId());
        map.put("brandName", brand.getBrandName());
        map.put("country", brand.getCountry());
        map.put("foundedYear", brand.getFoundedYear());
        map.put("brandLogoUrl", brand.getBrandLogoUrl());
        map.put("brandLogoPath", brand.getBrandLogoPath());
        map.put("isActive", brand.getIsActive());
        map.put("createdAt", brand.getCreatedAt());
        return map;
    }
    
    private Map<String, Object> modelToMap(VehicleModel model) {
        Map<String, Object> map = new HashMap<>();
        map.put("modelId", model.getModelId());
        map.put("modelName", model.getModelName());
        map.put("modelYear", model.getModelYear());
        map.put("vehicleType", model.getVehicleType());
        map.put("description", model.getDescription());
        map.put("specifications", model.getSpecifications());
        map.put("modelImageUrl", model.getModelImageUrl());
        map.put("modelImagePath", model.getModelImagePath());
        map.put("isActive", model.getIsActive());
        map.put("createdAt", model.getCreatedAt());
        if (model.getBrand() != null) {
            map.put("brandId", model.getBrand().getBrandId());
        }
        return map;
    }
    
    private Map<String, Object> variantToMap(VehicleVariant variant) {
        Map<String, Object> map = new HashMap<>();
        map.put("variantId", variant.getVariantId());
        map.put("variantName", variant.getVariantName());
        map.put("batteryCapacity", variant.getBatteryCapacity());
        map.put("rangeKm", variant.getRangeKm());
        map.put("powerKw", variant.getPowerKw());
        map.put("acceleration0100", variant.getAcceleration0100());
        map.put("topSpeed", variant.getTopSpeed());
        map.put("chargingTimeFast", variant.getChargingTimeFast());
        map.put("chargingTimeSlow", variant.getChargingTimeSlow());
        map.put("priceBase", variant.getPriceBase());
        map.put("variantImageUrl", variant.getVariantImageUrl());
        map.put("variantImagePath", variant.getVariantImagePath());
        map.put("isActive", variant.getIsActive());
        map.put("createdAt", variant.getCreatedAt());
        if (variant.getModel() != null) {
            map.put("modelId", variant.getModel().getModelId());
        }
        return map;
    }
    
    private Map<String, Object> colorToMap(VehicleColor color) {
        Map<String, Object> map = new HashMap<>();
        map.put("colorId", color.getColorId());
        map.put("colorName", color.getColorName());
        map.put("colorCode", color.getColorCode());
        map.put("colorSwatchUrl", color.getColorSwatchUrl());
        map.put("colorSwatchPath", color.getColorSwatchPath());
        map.put("isActive", color.getIsActive());
        return map;
    }
    
    // Vehicle Brand endpoints
    @GetMapping("/brands")
    @Operation(summary = "Lấy danh sách thương hiệu", description = "Lấy tất cả thương hiệu xe")
    public ResponseEntity<?> getAllBrands() {
        try {
            List<VehicleBrand> brands = vehicleService.getAllBrands();
            List<Map<String, Object>> brandList = brands.stream().map(this::brandToMap).collect(Collectors.toList());
            return ResponseEntity.ok(brandList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve brands: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/brands/active")
    @Operation(summary = "Lấy thương hiệu đang hoạt động", description = "Lấy thương hiệu xe đang hoạt động")
    public ResponseEntity<?> getActiveBrands() {
        try {
            List<VehicleBrand> brands = vehicleService.getActiveBrands();
            List<Map<String, Object>> brandList = brands.stream().map(this::brandToMap).collect(Collectors.toList());
            return ResponseEntity.ok(brandList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve active brands: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/brands/{brandId}")
    @Operation(summary = "Lấy thương hiệu theo ID", description = "Lấy thông tin thương hiệu theo ID")
    public ResponseEntity<?> getBrandById(@PathVariable Integer brandId) {
        try {
            return vehicleService.getBrandById(brandId)
                    .map(brand -> ResponseEntity.ok(brandToMap(brand)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve brand: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/brands/name/{brandName}")
    public ResponseEntity<?> getBrandByName(@PathVariable String brandName) {
        try {
            return vehicleService.getBrandByName(brandName)
                    .map(brand -> ResponseEntity.ok(brandToMap(brand)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve brand: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/brands/country/{country}")
    public ResponseEntity<?> getBrandsByCountry(@PathVariable String country) {
        try {
            List<VehicleBrand> brands = vehicleService.getBrandsByCountry(country);
            List<Map<String, Object>> brandList = brands.stream().map(this::brandToMap).collect(Collectors.toList());
            return ResponseEntity.ok(brandList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve brands: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/brands/search")
    public ResponseEntity<?> searchBrandsByName(@RequestParam String name) {
        try {
            List<VehicleBrand> brands = vehicleService.searchBrandsByName(name);
            List<Map<String, Object>> brandList = brands.stream().map(this::brandToMap).collect(Collectors.toList());
            return ResponseEntity.ok(brandList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to search brands: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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
            return ResponseEntity.status(HttpStatus.CREATED).body(brandToMap(createdBrand));
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
            return ResponseEntity.ok(brandToMap(updatedBrand));
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
            String errorMessage = e.getMessage();
            error.put("error", errorMessage);
            // Phân biệt giữa entity không tồn tại và lỗi foreign key constraint
            if (errorMessage != null && errorMessage.contains("Cannot delete")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else if (errorMessage != null && errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete brand: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Vehicle Model endpoints
    @GetMapping("/models")
    @Operation(summary = "Lấy danh sách mẫu xe", description = "Lấy tất cả mẫu xe")
    public ResponseEntity<?> getAllModels() {
        try {
            List<VehicleModel> models = vehicleService.getAllModels();
            List<Map<String, Object>> modelList = models.stream().map(this::modelToMap).collect(Collectors.toList());
            return ResponseEntity.ok(modelList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve models: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/models/active")
    @Operation(summary = "Lấy mẫu xe đang hoạt động", description = "Lấy mẫu xe đang hoạt động")
    public ResponseEntity<?> getActiveModels() {
        try {
            List<VehicleModel> models = vehicleService.getActiveModels();
            List<Map<String, Object>> modelList = models.stream().map(this::modelToMap).collect(Collectors.toList());
            return ResponseEntity.ok(modelList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve active models: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/models/{modelId}")
    public ResponseEntity<?> getModelById(@PathVariable Integer modelId) {
        try {
            return vehicleService.getModelById(modelId)
                    .map(model -> ResponseEntity.ok(modelToMap(model)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve model: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/models/brand/{brandId}")
    public ResponseEntity<?> getModelsByBrand(@PathVariable Integer brandId) {
        try {
            List<VehicleModel> models = vehicleService.getModelsByBrand(brandId);
            List<Map<String, Object>> modelList = models.stream().map(this::modelToMap).collect(Collectors.toList());
            return ResponseEntity.ok(modelList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve models: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/models/brand/{brandId}/active")
    public ResponseEntity<?> getActiveModelsByBrand(@PathVariable Integer brandId) {
        try {
            List<VehicleModel> models = vehicleService.getActiveModelsByBrand(brandId);
            List<Map<String, Object>> modelList = models.stream().map(this::modelToMap).collect(Collectors.toList());
            return ResponseEntity.ok(modelList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve active models: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/models/search")
    public ResponseEntity<?> searchModelsByName(@RequestParam String name) {
        try {
            List<VehicleModel> models = vehicleService.searchModelsByName(name);
            List<Map<String, Object>> modelList = models.stream().map(this::modelToMap).collect(Collectors.toList());
            return ResponseEntity.ok(modelList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to search models: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/models/type/{vehicleType}")
    public ResponseEntity<?> getModelsByType(@PathVariable String vehicleType) {
        try {
            List<VehicleModel> models = vehicleService.getModelsByType(vehicleType);
            List<Map<String, Object>> modelList = models.stream().map(this::modelToMap).collect(Collectors.toList());
            return ResponseEntity.ok(modelList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve models: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/models/year/{year}")
    public ResponseEntity<?> getModelsByYear(@PathVariable Integer year) {
        try {
            List<VehicleModel> models = vehicleService.getModelsByYear(year);
            List<Map<String, Object>> modelList = models.stream().map(this::modelToMap).collect(Collectors.toList());
            return ResponseEntity.ok(modelList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve models: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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
            return ResponseEntity.status(HttpStatus.CREATED).body(modelToMap(createdModel));
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
            return ResponseEntity.ok(modelToMap(updatedModel));
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
            String errorMessage = e.getMessage();
            error.put("error", errorMessage);
            // Phân biệt giữa entity không tồn tại và lỗi foreign key constraint
            if (errorMessage != null && errorMessage.contains("Cannot delete")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else if (errorMessage != null && errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete model: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Vehicle Variant endpoints
    @GetMapping("/variants")
    @Operation(summary = "Lấy danh sách phiên bản xe", description = "Lấy tất cả phiên bản xe")
    public ResponseEntity<?> getAllVariants() {
        try {
            List<VehicleVariant> variants = vehicleService.getAllVariants();
            List<Map<String, Object>> variantList = variants.stream().map(this::variantToMap).collect(Collectors.toList());
            return ResponseEntity.ok(variantList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve variants: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/variants/active")
    @Operation(summary = "Lấy phiên bản xe đang hoạt động", description = "Lấy phiên bản xe đang hoạt động")
    public ResponseEntity<?> getActiveVariants() {
        try {
            List<VehicleVariant> variants = vehicleService.getActiveVariants();
            List<Map<String, Object>> variantList = variants.stream().map(this::variantToMap).collect(Collectors.toList());
            return ResponseEntity.ok(variantList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve active variants: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/variants/{variantId}")
    public ResponseEntity<?> getVariantById(@PathVariable Integer variantId) {
        try {
            return vehicleService.getVariantById(variantId)
                    .map(variant -> ResponseEntity.ok(variantToMap(variant)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve variant: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/variants/model/{modelId}")
    public ResponseEntity<?> getVariantsByModel(@PathVariable Integer modelId) {
        try {
            List<VehicleVariant> variants = vehicleService.getVariantsByModel(modelId);
            List<Map<String, Object>> variantList = variants.stream().map(this::variantToMap).collect(Collectors.toList());
            return ResponseEntity.ok(variantList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve variants: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/variants/model/{modelId}/active")
    public ResponseEntity<?> getActiveVariantsByModel(@PathVariable Integer modelId) {
        try {
            List<VehicleVariant> variants = vehicleService.getActiveVariantsByModel(modelId);
            List<Map<String, Object>> variantList = variants.stream().map(this::variantToMap).collect(Collectors.toList());
            return ResponseEntity.ok(variantList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve active variants: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/variants/search")
    public ResponseEntity<?> searchVariantsByName(@RequestParam String name) {
        try {
            List<VehicleVariant> variants = vehicleService.searchVariantsByName(name);
            List<Map<String, Object>> variantList = variants.stream().map(this::variantToMap).collect(Collectors.toList());
            return ResponseEntity.ok(variantList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to search variants: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/variants/price-range")
    public ResponseEntity<?> getVariantsByPriceRange(
            @RequestParam BigDecimal minPrice, 
            @RequestParam BigDecimal maxPrice) {
        try {
            List<VehicleVariant> variants = vehicleService.getVariantsByPriceRange(minPrice, maxPrice);
            List<Map<String, Object>> variantList = variants.stream().map(this::variantToMap).collect(Collectors.toList());
            return ResponseEntity.ok(variantList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve variants: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/variants/min-range/{minRange}")
    public ResponseEntity<?> getVariantsByMinRange(@PathVariable Integer minRange) {
        try {
            List<VehicleVariant> variants = vehicleService.getVariantsByMinRange(minRange);
            List<Map<String, Object>> variantList = variants.stream().map(this::variantToMap).collect(Collectors.toList());
            return ResponseEntity.ok(variantList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve variants: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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
            return ResponseEntity.status(HttpStatus.CREATED).body(variantToMap(createdVariant));
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
            return ResponseEntity.ok(variantToMap(updatedVariant));
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
            String errorMessage = e.getMessage();
            error.put("error", errorMessage);
            // Phân biệt giữa entity không tồn tại và lỗi foreign key constraint
            if (errorMessage != null && errorMessage.contains("Cannot delete")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else if (errorMessage != null && errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete variant: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Vehicle Color endpoints
    @GetMapping("/colors")
    @Operation(summary = "Lấy danh sách màu sắc", description = "Lấy tất cả màu sắc xe")
    public ResponseEntity<?> getAllColors() {
        try {
            List<VehicleColor> colors = vehicleService.getAllColors();
            List<Map<String, Object>> colorList = colors.stream().map(this::colorToMap).collect(Collectors.toList());
            return ResponseEntity.ok(colorList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve colors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/colors/active")
    @Operation(summary = "Lấy màu sắc đang hoạt động", description = "Lấy màu sắc xe đang hoạt động")
    public ResponseEntity<?> getActiveColors() {
        try {
            List<VehicleColor> colors = vehicleService.getActiveColors();
            List<Map<String, Object>> colorList = colors.stream().map(this::colorToMap).collect(Collectors.toList());
            return ResponseEntity.ok(colorList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve active colors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/colors/{colorId}")
    public ResponseEntity<?> getColorById(@PathVariable Integer colorId) {
        try {
            return vehicleService.getColorById(colorId)
                    .map(color -> ResponseEntity.ok(colorToMap(color)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve color: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/colors/name/{colorName}")
    public ResponseEntity<?> getColorByName(@PathVariable String colorName) {
        try {
            return vehicleService.getColorByName(colorName)
                    .map(color -> ResponseEntity.ok(colorToMap(color)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve color: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/colors/code/{colorCode}")
    public ResponseEntity<?> getColorByCode(@PathVariable String colorCode) {
        try {
            return vehicleService.getColorByCode(colorCode)
                    .map(color -> ResponseEntity.ok(colorToMap(color)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve color: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/colors/search")
    public ResponseEntity<?> searchColorsByName(@RequestParam String name) {
        try {
            List<VehicleColor> colors = vehicleService.searchColorsByName(name);
            List<Map<String, Object>> colorList = colors.stream().map(this::colorToMap).collect(Collectors.toList());
            return ResponseEntity.ok(colorList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to search colors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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
            return ResponseEntity.status(HttpStatus.CREATED).body(colorToMap(createdColor));
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
            return ResponseEntity.ok(colorToMap(updatedColor));
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
            String errorMessage = e.getMessage();
            error.put("error", errorMessage);
            // Phân biệt giữa entity không tồn tại và lỗi foreign key constraint
            if (errorMessage != null && errorMessage.contains("Cannot delete")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else if (errorMessage != null && errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete color: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

