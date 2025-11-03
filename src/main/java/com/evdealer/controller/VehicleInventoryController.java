package com.evdealer.controller;

import com.evdealer.dto.VehicleInventoryDTO;
import com.evdealer.dto.VehicleInventoryRequest;
import com.evdealer.entity.VehicleInventory;
import com.evdealer.service.VehicleInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicle-inventory")
@CrossOrigin(origins = "*")
@Tag(name = "Vehicle Inventory Management", description = "APIs for managing vehicle inventory")
public class VehicleInventoryController {
    
    @Autowired
    private VehicleInventoryService vehicleInventoryService;
    
    @GetMapping
    @Operation(summary = "Get all vehicle inventory", description = "Retrieve a list of all vehicle inventory")
    public ResponseEntity<List<VehicleInventoryDTO>> getAllVehicleInventory() {
        List<VehicleInventory> inventory = vehicleInventoryService.getAllVehicleInventory();
        return ResponseEntity.ok(inventory.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/{inventoryId}")
    @Operation(summary = "Get vehicle inventory by ID", description = "Retrieve a specific vehicle inventory by its ID with all relationships")
    public ResponseEntity<VehicleInventoryDTO> getInventoryById(@PathVariable @Parameter(description = "Inventory ID") UUID inventoryId) {
        // Use method with eager loading to ensure warehouse is loaded
        return vehicleInventoryService.getInventoryByIdWithDetails(inventoryId)
                .map(inventory -> ResponseEntity.ok(toDTO(inventory)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/vin/{vin}")
    @Operation(summary = "Get vehicle inventory by VIN", description = "Retrieve a specific vehicle inventory by its VIN")
    public ResponseEntity<VehicleInventoryDTO> getInventoryByVin(@PathVariable String vin) {
        return vehicleInventoryService.getInventoryByVin(vin)
                .map(inventory -> ResponseEntity.ok(toDTO(inventory)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/statuses")
    @Operation(summary = "Get all available statuses", description = "Retrieve all unique statuses used in vehicle inventory")
    public ResponseEntity<List<String>> getAllStatuses() {
        List<VehicleInventory> allInventory = vehicleInventoryService.getAllVehicleInventory();
        List<String> statuses = allInventory.stream()
                .map(VehicleInventory::getStatus)
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
                .collect(java.util.stream.Collectors.groupingBy(
                    VehicleInventory::getStatus,
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
    public ResponseEntity<Map<String, Object>> validateStatus(@RequestParam String status) {
        boolean isValid = vehicleInventoryService.isValidStatus(status);
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("isValid", isValid);
        if (!isValid) {
            result.put("message", "Invalid status. Valid options: " + String.join(", ", vehicleInventoryService.getAllStatusOptions().keySet()));
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/variant/{variantId}")
    @Operation(summary = "Get inventory by variant", description = "Retrieve vehicle inventory for a specific variant")
    public ResponseEntity<List<VehicleInventoryDTO>> getInventoryByVariant(@PathVariable Integer variantId) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByVariant(variantId);
        return ResponseEntity.ok(inventory.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/color/{colorId}")
    @Operation(summary = "Get inventory by color", description = "Retrieve vehicle inventory for a specific color")
    public ResponseEntity<List<VehicleInventoryDTO>> getInventoryByColor(@PathVariable Integer colorId) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByColor(colorId);
        return ResponseEntity.ok(inventory.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get inventory by warehouse", description = "Retrieve vehicle inventory for a specific warehouse")
    public ResponseEntity<List<VehicleInventoryDTO>> getInventoryByWarehouse(@PathVariable UUID warehouseId) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByWarehouse(warehouseId);
        return ResponseEntity.ok(inventory.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/warehouse-location/{location}")
    @Operation(summary = "Get inventory by warehouse location", description = "Retrieve vehicle inventory for a specific warehouse location")
    public ResponseEntity<List<VehicleInventoryDTO>> getInventoryByWarehouseLocation(@PathVariable String location) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByWarehouseLocation(location);
        return ResponseEntity.ok(inventory.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/price-range")
    @Operation(summary = "Get inventory by price range", description = "Retrieve vehicle inventory within a price range")
    public ResponseEntity<List<VehicleInventoryDTO>> getInventoryByPriceRange(
            @RequestParam @Parameter(description = "Minimum price") BigDecimal minPrice,
            @RequestParam @Parameter(description = "Maximum price") BigDecimal maxPrice) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(inventory.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/manufacturing-date-range")
    @Operation(summary = "Get inventory by manufacturing date range", description = "Retrieve vehicle inventory within a manufacturing date range")
    public ResponseEntity<List<VehicleInventoryDTO>> getInventoryByManufacturingDateRange(
            @RequestParam @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @Parameter(description = "End date") LocalDate endDate) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByManufacturingDateRange(startDate, endDate);
        return ResponseEntity.ok(inventory.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/arrival-date-range")
    @Operation(summary = "Get inventory by arrival date range", description = "Retrieve vehicle inventory within an arrival date range")
    public ResponseEntity<List<VehicleInventoryDTO>> getInventoryByArrivalDateRange(
            @RequestParam @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @Parameter(description = "End date") LocalDate endDate) {
        List<VehicleInventory> inventory = vehicleInventoryService.getInventoryByArrivalDateRange(startDate, endDate);
        return ResponseEntity.ok(inventory.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/search/vin")
    @Operation(summary = "Search inventory by VIN", description = "Search vehicle inventory by VIN")
    public ResponseEntity<List<VehicleInventoryDTO>> searchByVin(@RequestParam String vin) {
        List<VehicleInventory> inventory = vehicleInventoryService.searchByVin(vin);
        return ResponseEntity.ok(inventory.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/search/chassis")
    @Operation(summary = "Search inventory by chassis number", description = "Search vehicle inventory by chassis number")
    public ResponseEntity<List<VehicleInventoryDTO>> searchByChassisNumber(@RequestParam String chassisNumber) {
        List<VehicleInventory> inventory = vehicleInventoryService.searchByChassisNumber(chassisNumber);
        return ResponseEntity.ok(inventory.stream().map(this::toDTO).toList());
    }
    
    @PostMapping
    @Operation(summary = "Create vehicle inventory", description = "Create a new vehicle inventory record")
    public ResponseEntity<VehicleInventoryDTO> createVehicleInventory(@RequestBody VehicleInventoryRequest request) {
        try {
            VehicleInventory createdInventory = vehicleInventoryService.createVehicleInventoryFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdInventory));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{inventoryId}")
    @Operation(summary = "Update vehicle inventory", description = "Update an existing vehicle inventory record")
    public ResponseEntity<VehicleInventoryDTO> updateVehicleInventory(
            @PathVariable UUID inventoryId, 
            @RequestBody VehicleInventoryRequest request) {
        try {
            VehicleInventory updatedInventory = vehicleInventoryService.updateVehicleInventoryFromRequest(inventoryId, request);
            return ResponseEntity.ok(toDTO(updatedInventory));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{inventoryId}/status")
    @Operation(summary = "Update inventory status", description = "Update the status of a vehicle inventory record")
    public ResponseEntity<VehicleInventoryDTO> updateInventoryStatus(
            @PathVariable UUID inventoryId, 
            @RequestParam String status) {
        try {
            VehicleInventory updatedInventory = vehicleInventoryService.updateInventoryStatus(inventoryId, status);
            return ResponseEntity.ok(toDTO(updatedInventory));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{inventoryId}")
    @Operation(summary = "Delete vehicle inventory", description = "Delete a vehicle inventory record")
    public ResponseEntity<Void> deleteVehicleInventory(@PathVariable UUID inventoryId) {
        try {
            vehicleInventoryService.deleteVehicleInventory(inventoryId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private VehicleInventoryDTO toDTO(VehicleInventory inv) {
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
}
