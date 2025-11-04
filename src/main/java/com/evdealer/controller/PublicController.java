package com.evdealer.controller;

import com.evdealer.dto.VehicleComparisonRequest;
import com.evdealer.dto.VehicleComparisonResponse;
import com.evdealer.dto.VehicleBrandDTO;
import com.evdealer.dto.VehicleModelDTO;
import com.evdealer.dto.VehicleVariantDTO;
import com.evdealer.dto.VehicleColorDTO;
import com.evdealer.dto.VehicleInventoryDTO;
import com.evdealer.dto.PromotionDTO;
import com.evdealer.entity.VehicleBrand;
import com.evdealer.entity.VehicleModel;
import com.evdealer.entity.VehicleVariant;
import com.evdealer.entity.VehicleColor;
import com.evdealer.entity.VehicleInventory;
import com.evdealer.entity.Promotion;
import com.evdealer.service.VehicleService;
import com.evdealer.service.VehicleInventoryService;
import com.evdealer.service.PromotionService;
import com.evdealer.service.VehicleComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/public", "/"})
@CrossOrigin(origins = "*")
@Tag(name = "Public Access", description = "APIs công khai cho khách hàng không cần đăng nhập")
public class PublicController {
    
    @Autowired
    private VehicleService vehicleService;
    
    @Autowired
    private VehicleInventoryService vehicleInventoryService;
    
    @Autowired
    private PromotionService promotionService;
    
    // Removed unused injected services after DTO refactor to avoid warnings
    
    @Autowired
    private VehicleComparisonService vehicleComparisonService;
    
    // ==================== HOME PAGE ENDPOINTS ====================
    
    @GetMapping("/")
    @Operation(summary = "Trang chủ", description = "Thông tin tổng quan cho trang chủ")
    public ResponseEntity<Map<String, Object>> getHomePage() {
        Map<String, Object> homeData = new HashMap<>();
        
        // Featured vehicles (available inventory)
        List<VehicleInventory> featuredVehicles = vehicleInventoryService.getInventoryByStatus("available");
        homeData.put("featuredVehicles", featuredVehicles.stream().map(this::toInventoryDTO).toList());
        
        // Active promotions
        List<Promotion> activePromotions = promotionService.getPromotionsByStatus("active");
        homeData.put("activePromotions", activePromotions.stream().map(this::toPromotionDTO).toList());
        
        // Statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVehicles", featuredVehicles.size());
        stats.put("activePromotions", activePromotions.size());
        homeData.put("statistics", stats);
        
        return ResponseEntity.ok(homeData);
    }
    
    @GetMapping("/catalog")
    @Operation(summary = "Danh mục xe", description = "Xem tất cả xe có sẵn")
    public ResponseEntity<Map<String, Object>> getVehicleCatalog() {
        Map<String, Object> catalog = new HashMap<>();
        
        // Vehicle brands
        List<VehicleBrand> brands = vehicleService.getAllBrands();
        catalog.put("brands", brands.stream().map(this::toBrandDTO).toList());
        
        // Vehicle models
        List<VehicleModel> models = vehicleService.getAllModels();
        catalog.put("models", models.stream().map(this::toModelDTO).toList());
        
        // Vehicle variants
        List<VehicleVariant> variants = vehicleService.getAllVariants();
        catalog.put("variants", variants.stream().map(this::toVariantDTO).toList());
        
        // Vehicle colors
        List<VehicleColor> colors = vehicleService.getAllColors();
        catalog.put("colors", colors.stream().map(this::toColorDTO).toList());
        
        // Available inventory
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByStatus("available");
        catalog.put("availableInventory", inventory.stream().map(this::toInventoryDTO).toList());
        
        return ResponseEntity.ok(catalog);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm", description = "Tìm kiếm xe theo tiêu chí")
    public ResponseEntity<Map<String, Object>> searchVehicles(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String variant,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        
        Map<String, Object> searchResults = new HashMap<>();
        
        // Get all available inventory
        List<VehicleInventory> allInventory = vehicleInventoryService.getInventoryByStatus("available");
        searchResults.put("results", allInventory.stream().map(this::toInventoryDTO).toList());
        searchResults.put("totalCount", allInventory.size());
        
        return ResponseEntity.ok(searchResults);
    }
    
    // ==================== VEHICLE CATALOG ====================
    
    // Alias endpoints for backward compatibility with HomeController paths
    @GetMapping({"/brands", "/vehicle-brands"})
    @Operation(summary = "Xem danh sách thương hiệu", description = "Khách hàng có thể xem tất cả thương hiệu xe")
    public ResponseEntity<List<VehicleBrandDTO>> getAllVehicleBrands() {
        List<VehicleBrand> brands = vehicleService.getAllBrands();
        return ResponseEntity.ok(brands.stream().map(this::toBrandDTO).toList());
    }
    
    @GetMapping({"/brands/{brandId}", "/vehicle-brands/{brandId}"})
    @Operation(summary = "Xem chi tiết thương hiệu", description = "Khách hàng có thể xem chi tiết thương hiệu xe")
    public ResponseEntity<VehicleBrandDTO> getVehicleBrandById(@PathVariable Integer brandId) {
        return vehicleService.getBrandById(brandId)
                .map(brand -> ResponseEntity.ok(toBrandDTO(brand)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping({"/models", "/vehicle-models"})
    @Operation(summary = "Xem danh sách mẫu xe", description = "Khách hàng có thể xem tất cả mẫu xe")
    public ResponseEntity<List<VehicleModelDTO>> getAllVehicleModels() {
        List<VehicleModel> models = vehicleService.getAllModels();
        return ResponseEntity.ok(models.stream().map(this::toModelDTO).toList());
    }
    
    @GetMapping({"/models/{modelId}", "/vehicle-models/{modelId}"})
    @Operation(summary = "Xem chi tiết mẫu xe", description = "Khách hàng có thể xem chi tiết mẫu xe")
    public ResponseEntity<VehicleModelDTO> getVehicleModelById(@PathVariable Integer modelId) {
        return vehicleService.getModelById(modelId)
                .map(model -> ResponseEntity.ok(toModelDTO(model)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping({"/variants", "/vehicle-variants"})
    @Operation(summary = "Xem danh sách phiên bản xe", description = "Khách hàng có thể xem tất cả phiên bản xe")
    public ResponseEntity<List<VehicleVariantDTO>> getAllVehicleVariants() {
        List<VehicleVariant> variants = vehicleService.getAllVariants();
        return ResponseEntity.ok(variants.stream().map(this::toVariantDTO).toList());
    }
    
    @GetMapping({"/variants/{variantId}", "/vehicle-variants/{variantId}"})
    @Operation(summary = "Xem chi tiết phiên bản xe", description = "Khách hàng có thể xem chi tiết phiên bản xe")
    public ResponseEntity<VehicleVariantDTO> getVehicleVariantById(@PathVariable Integer variantId) {
        return vehicleService.getVariantById(variantId)
                .map(variant -> ResponseEntity.ok(toVariantDTO(variant)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping({"/colors", "/vehicle-colors"})
    @Operation(summary = "Xem danh sách màu xe", description = "Khách hàng có thể xem tất cả màu xe")
    public ResponseEntity<List<VehicleColorDTO>> getAllVehicleColors() {
        List<VehicleColor> colors = vehicleService.getAllColors();
        return ResponseEntity.ok(colors.stream().map(this::toColorDTO).toList());
    }
    
    @GetMapping({"/colors/{colorId}", "/vehicle-colors/{colorId}"})
    @Operation(summary = "Xem chi tiết màu xe", description = "Khách hàng có thể xem chi tiết màu xe")
    public ResponseEntity<VehicleColorDTO> getVehicleColorById(@PathVariable Integer colorId) {
        return vehicleService.getColorById(colorId)
                .map(color -> ResponseEntity.ok(toColorDTO(color)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping({"/inventory/available", "/vehicle-inventory"})
    @Operation(summary = "Xem kho xe", description = "Khách hàng có thể xem xe có sẵn trong kho")
    public ResponseEntity<List<VehicleInventoryDTO>> getAllInventory() {
        List<VehicleInventory> inventory = vehicleInventoryService.getAllVehicleInventory();
        return ResponseEntity.ok(inventory.stream().map(this::toInventoryDTO).toList());
    }
    
    @GetMapping({"/inventory/{inventoryId}", "/vehicle-inventory/{inventoryId}"})
    @Operation(summary = "Xem chi tiết xe trong kho", description = "Khách hàng có thể xem chi tiết xe trong kho")
    public ResponseEntity<VehicleInventoryDTO> getInventoryById(@PathVariable UUID inventoryId) {
        return vehicleInventoryService.getInventoryById(inventoryId)
                .map(inventory -> ResponseEntity.ok(toInventoryDTO(inventory)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // ==================== PROMOTIONS ====================
    
    @GetMapping("/promotions")
    @Operation(summary = "Xem khuyến mãi", description = "Khách hàng có thể xem tất cả khuyến mãi đang hoạt động")
    public ResponseEntity<List<PromotionDTO>> getActivePromotions() {
        List<Promotion> promotions = promotionService.getPromotionsByStatus("active");
        return ResponseEntity.ok(promotions.stream().map(this::toPromotionDTO).toList());
    }
    
    @GetMapping("/promotions/{promotionId}")
    @Operation(summary = "Xem chi tiết khuyến mãi", description = "Khách hàng có thể xem chi tiết khuyến mãi")
    public ResponseEntity<PromotionDTO> getPromotionById(@PathVariable UUID promotionId) {
        return promotionService.getPromotionById(promotionId)
                .map(promotion -> ResponseEntity.ok(toPromotionDTO(promotion)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // ==================== CUSTOMER ACTIONS (READ-ONLY) ====================
    // Note: All CRUD operations have been moved to authenticated endpoints
    // This controller now only provides read-only access for customers
    
    // ==================== SEARCH & FILTER ====================
    
    @GetMapping("/vehicle-inventory/status/{status}")
    @Operation(summary = "Xem xe theo trạng thái", description = "Khách hàng có thể xem xe theo trạng thái (available, sold, reserved)")
    public ResponseEntity<List<VehicleInventoryDTO>> getInventoryByStatus(@PathVariable String status) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByStatus(status);
        return ResponseEntity.ok(inventory.stream().map(this::toInventoryDTO).toList());
    }
    
    @GetMapping("/vehicle-models/brand/{brandId}")
    @Operation(summary = "Xem mẫu xe theo thương hiệu", description = "Khách hàng có thể xem mẫu xe theo thương hiệu")
    public ResponseEntity<List<VehicleModelDTO>> getModelsByBrand(@PathVariable Integer brandId) {
        List<VehicleModel> models = vehicleService.getModelsByBrand(brandId);
        return ResponseEntity.ok(models.stream().map(this::toModelDTO).toList());
    }
    
    // ==================== VEHICLE COMPARISON ====================
    
    @PostMapping({"/vehicle-compare", "/compare"})
    @Operation(summary = "So sánh xe", description = "Khách hàng có thể so sánh nhiều xe theo các tiêu chí khác nhau")
    public ResponseEntity<?> compareVehicles(@RequestBody VehicleComparisonRequest request) {
        try {
            VehicleComparisonResponse response = vehicleComparisonService.compareVehicles(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Vehicle comparison failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping({"/vehicle-compare/quick", "/compare/quick"})
    @Operation(summary = "So sánh nhanh xe", description = "Khách hàng có thể so sánh nhanh các xe theo danh sách ID")
    public ResponseEntity<?> quickCompareVehicles(
            @RequestParam List<Integer> variantIds) {
        try {
            VehicleComparisonResponse response = vehicleComparisonService.quickCompare(variantIds);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Quick comparison failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping({"/vehicle-compare/available", "/compare/available"})
    @Operation(summary = "Xe có thể so sánh", description = "Khách hàng có thể xem danh sách xe có thể so sánh")
    public ResponseEntity<List<VehicleVariantDTO>> getAvailableVehiclesForComparison() {
        try {
            List<VehicleVariant> variants = vehicleComparisonService.getAvailableVariantsForComparison();
            return ResponseEntity.ok(variants.stream().map(this::toVariantDTO).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping({"/vehicle-compare/{variantId1}/vs/{variantId2}", "/compare/{variantId1}/vs/{variantId2}"})
    @Operation(summary = "So sánh 2 xe", description = "Khách hàng có thể so sánh trực tiếp 2 xe cụ thể")
    public ResponseEntity<?> compareTwoVehicles(
            @PathVariable Integer variantId1,
            @PathVariable Integer variantId2) {
        try {
            VehicleComparisonResponse response = vehicleComparisonService.quickCompare(
                    List.of(variantId1, variantId2));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Two-vehicle comparison failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping({"/vehicle-compare/criteria", "/compare/criteria"})
    @Operation(summary = "Tiêu chí so sánh", description = "Khách hàng có thể xem danh sách các tiêu chí so sánh có sẵn")
    public ResponseEntity<Map<String, Object>> getComparisonCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("availableCriteria", List.of(
                "price", "range", "power", "acceleration", "topSpeed", 
                "batteryCapacity", "chargingTime", "availability"
        ));
        criteria.put("maxVehicles", 5);
        criteria.put("defaultCriteria", List.of("price", "range", "power", "acceleration"));
        return ResponseEntity.ok(criteria);
    }

    private VehicleBrandDTO toBrandDTO(VehicleBrand b) {
        VehicleBrandDTO dto = new VehicleBrandDTO();
        dto.setBrandId(b.getBrandId());
        dto.setBrandName(b.getBrandName());
        dto.setCountry(b.getCountry());
        dto.setFoundedYear(b.getFoundedYear());
        return dto;
    }

    private VehicleModelDTO toModelDTO(VehicleModel m) {
        VehicleModelDTO dto = new VehicleModelDTO();
        dto.setModelId(m.getModelId());
        dto.setBrandId(m.getBrand() != null ? m.getBrand().getBrandId() : null);
        dto.setModelName(m.getModelName());
        dto.setModelYear(m.getModelYear());
        dto.setVehicleType(m.getVehicleType());
        return dto;
    }

    private VehicleVariantDTO toVariantDTO(VehicleVariant v) {
        VehicleVariantDTO dto = new VehicleVariantDTO();
        dto.setVariantId(v.getVariantId());
        dto.setModelId(v.getModel() != null ? v.getModel().getModelId() : null);
        dto.setVariantName(v.getVariantName());
        dto.setPriceBase(v.getPriceBase());
        dto.setRangeKm(v.getRangeKm());
        return dto;
    }

    private VehicleColorDTO toColorDTO(VehicleColor c) {
        VehicleColorDTO dto = new VehicleColorDTO();
        dto.setColorId(c.getColorId());
        dto.setColorName(c.getColorName());
        dto.setColorCode(c.getColorCode());
        return dto;
    }

    private VehicleInventoryDTO toInventoryDTO(VehicleInventory inv) {
        VehicleInventoryDTO dto = new VehicleInventoryDTO();
        dto.setInventoryId(inv.getInventoryId());
        dto.setVariantId(inv.getVariant() != null ? inv.getVariant().getVariantId() : null);
        dto.setColorId(inv.getColor() != null ? inv.getColor().getColorId() : null);
        dto.setWarehouseId(inv.getWarehouse() != null ? inv.getWarehouse().getWarehouseId() : null);
        dto.setStatus(inv.getStatus());
        dto.setVin(inv.getVin());
        dto.setArrivalDate(inv.getArrivalDate());
        dto.setSellingPrice(inv.getSellingPrice());
        return dto;
    }

    private PromotionDTO toPromotionDTO(Promotion p) {
        PromotionDTO dto = new PromotionDTO();
        dto.setPromotionId(p.getPromotionId());
        dto.setVariantId(p.getVariant() != null ? p.getVariant().getVariantId() : null);
        dto.setTitle(p.getTitle());
        dto.setDiscountPercent(p.getDiscountPercent());
        dto.setDiscountAmount(p.getDiscountAmount());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        return dto;
    }
}
