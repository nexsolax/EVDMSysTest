package com.evdealer.controller;

import com.evdealer.entity.VehicleInventory;
import com.evdealer.service.VehicleInventoryService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.evdealer.dto.VehicleInventoryRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/inventory", "/api/vehicle-inventory"})
@CrossOrigin(origins = "*")
@Tag(name = "Vehicle Inventory Management", description = "APIs for managing electric vehicle inventory and distribution")
public class InventoryManagementController {

    @Autowired
    private VehicleInventoryService vehicleInventoryService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    private Map<String, Object> inventoryToMap(VehicleInventory inventory) {
        Map<String, Object> map = new HashMap<>();
        map.put("inventoryId", inventory.getInventoryId());
        map.put("status", inventory.getStatus());
        map.put("vin", inventory.getVin());
        map.put("chassisNumber", inventory.getChassisNumber());
        map.put("arrivalDate", inventory.getArrivalDate());
        map.put("manufacturingDate", inventory.getManufacturingDate());
        map.put("sellingPrice", inventory.getSellingPrice());
        map.put("costPrice", inventory.getCostPrice());
        map.put("warehouseLocation", inventory.getWarehouseLocation());
        map.put("condition", inventory.getCondition() != null ? inventory.getCondition().toString() : null);
        
        if (inventory.getVariant() != null) {
            map.put("variantId", inventory.getVariant().getVariantId());
        }
        if (inventory.getColor() != null) {
            map.put("colorId", inventory.getColor().getColorId());
        }
        if (inventory.getWarehouse() != null) {
            map.put("warehouseId", inventory.getWarehouse().getWarehouseId());
        }
        
        return map;
    }

    @GetMapping
    @Operation(summary = "Get all inventory", description = "Retrieve a list of all vehicle inventory")
    public ResponseEntity<?> getAllVehicleInventory() {
        List<VehicleInventory> inventory = vehicleInventoryService.getAllVehicleInventory();
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available inventory", description = "Retrieve all available vehicles in inventory")
    public ResponseEntity<?> getAvailableInventory() {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByStatus("available");
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get inventory by status", description = "Retrieve inventory by status")
    public ResponseEntity<?> getInventoryByStatus(
            @PathVariable @Parameter(description = "Status value", example = "available") String status) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByStatus(status);
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
         return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get inventory by warehouse", description = "Retrieve inventory for a specific warehouse")
    public ResponseEntity<?> getInventoryByWarehouse(
            @PathVariable @Parameter(description = "Warehouse ID") UUID warehouseId) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByWarehouse(warehouseId);
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/variant/{variantId}")
    @Operation(summary = "Get inventory by variant", description = "Retrieve inventory for a specific vehicle variant")
    public ResponseEntity<?> getInventoryByVariant(
            @PathVariable @Parameter(description = "Variant ID", example = "1") Integer variantId) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByVariant(variantId);
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/color/{colorId}")
    @Operation(summary = "Get inventory by color", description = "Retrieve inventory for a specific color")
    public ResponseEntity<?> getInventoryByColor(
            @PathVariable @Parameter(description = "Color ID", example = "1") Integer colorId) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByColor(colorId);
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get inventory by date range", description = "Retrieve inventory within a date range")
    public ResponseEntity<?> getInventoryByDateRange(
            @RequestParam @Parameter(description = "Start date", example = "2024-01-01") LocalDate startDate,
            @RequestParam @Parameter(description = "End date", example = "2024-12-31") LocalDate endDate) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByArrivalDateRange(startDate, endDate);
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/{inventoryId}")
    @Operation(summary = "Get inventory by ID", description = "Retrieve a specific inventory item by its ID")
    public ResponseEntity<?> getInventoryById(@PathVariable @Parameter(description = "Inventory ID") UUID inventoryId) {
        return vehicleInventoryService.getInventoryById(inventoryId)
                .map(inventory -> ResponseEntity.ok(inventoryToMap(inventory)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vin/{vin}")
    @Operation(summary = "Get inventory by VIN", description = "Retrieve inventory by VIN number")
    public ResponseEntity<?> getInventoryByVin(
            @PathVariable @Parameter(description = "VIN number", example = "1HGBH41JXMN109186") String vin) {
        return vehicleInventoryService.getInventoryByVin(vin)
                .map(inventory -> ResponseEntity.ok(inventoryToMap(inventory)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create inventory", description = "Create a new inventory item")
    public ResponseEntity<?> createVehicleInventory(@RequestBody VehicleInventoryRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo inventory
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create vehicle inventory");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleInventory createdInventory = vehicleInventoryService.createVehicleInventoryFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInventory);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{inventoryId}")
    @Operation(summary = "Update inventory", description = "Update an existing inventory item")
    public ResponseEntity<?> updateVehicleInventory(
            @PathVariable UUID inventoryId, 
            @RequestBody VehicleInventoryRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update inventory
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update vehicle inventory");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleInventory updatedInventory = vehicleInventoryService.updateVehicleInventoryFromRequest(inventoryId, request);
            return ResponseEntity.ok(updatedInventory);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{inventoryId}/status")
    @Operation(summary = "Update inventory status", description = "Update the status of an inventory item")
    public ResponseEntity<?> updateInventoryStatus(
            @PathVariable @Parameter(description = "Inventory ID") UUID inventoryId, 
            @RequestParam @Parameter(description = "Status value", example = "available") String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update inventory status
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update inventory status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleInventory updatedInventory = vehicleInventoryService.updateInventoryStatus(inventoryId, status);
            return ResponseEntity.ok(inventoryToMap(updatedInventory));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update inventory status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update inventory status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{inventoryId}/mark-sold")
    @Operation(summary = "Mark as sold", description = "Mark an inventory item as sold")
    public ResponseEntity<?> markAsSold(
            @PathVariable @Parameter(description = "Inventory ID") UUID inventoryId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể mark inventory as sold
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can mark inventory as sold");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleInventory soldInventory = vehicleInventoryService.updateInventoryStatus(inventoryId, "sold");
            return ResponseEntity.ok(inventoryToMap(soldInventory));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to mark inventory as sold: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to mark inventory as sold: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{inventoryId}/mark-reserved")
    @Operation(summary = "Mark as reserved", description = "Mark an inventory item as reserved")
    public ResponseEntity<?> markAsReserved(
            @PathVariable @Parameter(description = "Inventory ID") UUID inventoryId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể mark inventory as reserved
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can mark inventory as reserved");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleInventory reservedInventory = vehicleInventoryService.updateInventoryStatus(inventoryId, "reserved");
            return ResponseEntity.ok(inventoryToMap(reservedInventory));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to mark inventory as reserved: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to mark inventory as reserved: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{inventoryId}")
    @Operation(summary = "Delete inventory", description = "Delete an inventory item")
    public ResponseEntity<?> deleteVehicleInventory(
            @PathVariable @Parameter(description = "Inventory ID") UUID inventoryId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa inventory
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete vehicle inventory");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            vehicleInventoryService.deleteVehicleInventory(inventoryId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Vehicle inventory deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search inventory", description = "Search for inventory by VIN, chassis number, or other criteria")
    public ResponseEntity<?> searchInventory(
            @RequestParam @Parameter(description = "Search keyword", example = "1HGBH41JXMN109186") String keyword) {
        List<VehicleInventory> inventory = vehicleInventoryService.searchByVin(keyword);
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get all available statuses", description = "Retrieve all unique statuses used in vehicle inventory")
    public ResponseEntity<List<String>> getAllStatuses() {
        List<VehicleInventory> allInventory = vehicleInventoryService.getAllVehicleInventory();
        List<String> statuses = allInventory.stream()
                .map(inv -> inv.getStatus() != null ? inv.getStatus().getValue() : null)
                .filter(status -> status != null)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(statuses);
    }
    
    @GetMapping("/status-summary")
    @Operation(summary = "Get status summary", description = "Get count of vehicles for each status")
    public ResponseEntity<Map<String, Object>> getStatusSummary() {
        List<VehicleInventory> allInventory = vehicleInventoryService.getAllVehicleInventory();
        Map<String, Long> statusCounts = allInventory.stream()
                .filter(inv -> inv.getStatus() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                    inv -> inv.getStatus().getValue(),
                    java.util.stream.Collectors.counting()
                ));
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalVehicles", allInventory.size());
        summary.put("statusCounts", statusCounts);
        summary.put("availableStatuses", statusCounts.keySet().stream().sorted().collect(java.util.stream.Collectors.toList()));
        
        return ResponseEntity.ok(summary);
    }
    
    @PostMapping("/normalize-statuses")
    @Operation(summary = "Normalize all status values", description = "Fix case sensitivity and normalize all existing status values in the database")
    public ResponseEntity<Map<String, Object>> normalizeAllStatuses() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể normalize statuses
            if (!securityUtils.isAdmin()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Access denied. Only admin can normalize statuses");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            int updatedCount = vehicleInventoryService.normalizeAllStatuses();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Status normalization completed");
            result.put("updatedCount", updatedCount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to normalize statuses: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status-options")
    @Operation(summary = "Get all available status options", description = "Get all valid status values with descriptions")
    public ResponseEntity<Map<String, String>> getStatusOptions() {
        Map<String, String> statusOptions = vehicleInventoryService.getAllStatusOptions();
        return ResponseEntity.ok(statusOptions);
    }
    
    @PostMapping("/validate-status")
    @Operation(summary = "Validate status value", description = "Check if a status value is valid")
    public ResponseEntity<Map<String, Object>> validateStatus(
            @RequestParam @Parameter(description = "Status value to validate", example = "available") String status) {
        boolean isValid = vehicleInventoryService.isValidStatus(status);
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("isValid", isValid);
        if (!isValid) {
            result.put("message", "Invalid status. Valid options: " + String.join(", ", vehicleInventoryService.getAllStatusOptions().keySet()));
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/warehouse-location/{location}")
    @Operation(summary = "Get inventory by warehouse location", description = "Retrieve vehicle inventory for a specific warehouse location")
    public ResponseEntity<?> getInventoryByWarehouseLocation(
            @PathVariable @Parameter(description = "Warehouse location", example = "Warehouse A, Bay 1") String location) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByWarehouseLocation(location);
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get inventory by price range", description = "Retrieve vehicle inventory within a price range")
    public ResponseEntity<?> getInventoryByPriceRange(
            @RequestParam @Parameter(description = "Minimum price") BigDecimal minPrice,
            @RequestParam @Parameter(description = "Maximum price") BigDecimal maxPrice) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByPriceRange(minPrice, maxPrice);
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/manufacturing-date-range")
    @Operation(summary = "Get inventory by manufacturing date range", description = "Retrieve vehicle inventory within a manufacturing date range")
    public ResponseEntity<?> getInventoryByManufacturingDateRange(
            @RequestParam @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @Parameter(description = "End date") LocalDate endDate) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByManufacturingDateRange(startDate, endDate);
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/search/vin")
    @Operation(summary = "Search inventory by VIN", description = "Search vehicle inventory by VIN")
    public ResponseEntity<?> searchByVin(
            @RequestParam @Parameter(description = "VIN number", example = "1HGBH41JXMN109186") String vin) {
        List<VehicleInventory> inventory = vehicleInventoryService.searchByVin(vin);
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/search/chassis")
    @Operation(summary = "Search inventory by chassis number", description = "Search vehicle inventory by chassis number")
    public ResponseEntity<?> searchByChassisNumber(
            @RequestParam @Parameter(description = "Chassis number", example = "CH123456789") String chassisNumber) {
        List<VehicleInventory> inventory = vehicleInventoryService.searchByChassisNumber(chassisNumber);
        List<Map<String, Object>> inventoryList = inventory.stream()
                .map(this::inventoryToMap)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(inventoryList);
    }
    
    // Support VehicleInventoryRequest for backward compatibility
    @PostMapping(value = "/create-from-request", consumes = "application/json")
    @Operation(summary = "Create inventory from request", description = "Create a new inventory item using VehicleInventoryRequest")
    public ResponseEntity<?> createVehicleInventoryFromRequest(@RequestBody VehicleInventoryRequest request) {
        try {
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create vehicle inventory");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleInventory createdInventory = vehicleInventoryService.createVehicleInventoryFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(inventoryToMap(createdInventory));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping(value = "/{inventoryId}/update-from-request", consumes = "application/json")
    @Operation(summary = "Update inventory from request", description = "Update an existing inventory item using VehicleInventoryRequest")
    public ResponseEntity<?> updateVehicleInventoryFromRequest(
            @PathVariable @Parameter(description = "Inventory ID") UUID inventoryId, 
            @RequestBody VehicleInventoryRequest request) {
        try {
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update vehicle inventory");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleInventory updatedInventory = vehicleInventoryService.updateVehicleInventoryFromRequest(inventoryId, request);
            return ResponseEntity.ok(inventoryToMap(updatedInventory));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
