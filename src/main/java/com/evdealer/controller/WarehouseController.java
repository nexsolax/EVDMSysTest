package com.evdealer.controller;

import com.evdealer.entity.Warehouse;
import com.evdealer.service.WarehouseService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/warehouses")
@CrossOrigin(origins = "*")
@Tag(name = "Warehouse Management", description = "APIs quản lý kho")
public class WarehouseController {
    
    @Autowired
    private WarehouseService warehouseService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy danh sách kho", description = "Lấy tất cả kho")
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        return ResponseEntity.ok(warehouses);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Lấy kho đang hoạt động", description = "Lấy kho đang hoạt động")
    public ResponseEntity<List<Warehouse>> getActiveWarehouses() {
        List<Warehouse> warehouses = warehouseService.getActiveWarehouses();
        return ResponseEntity.ok(warehouses);
    }
    
    @GetMapping("/{warehouseId}")
    @Operation(summary = "Lấy kho theo ID", description = "Lấy thông tin kho theo ID")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable @Parameter(description = "Warehouse ID") UUID warehouseId) {
        return warehouseService.getWarehouseById(warehouseId)
                .map(warehouse -> ResponseEntity.ok(warehouse))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{warehouseCode}")
    @Operation(summary = "Lấy kho theo mã", description = "Lấy thông tin kho theo mã kho")
    public ResponseEntity<Warehouse> getWarehouseByCode(@PathVariable String warehouseCode) {
        return warehouseService.getWarehouseByCode(warehouseCode)
                .map(warehouse -> ResponseEntity.ok(warehouse))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/city/{city}")
    @Operation(summary = "Lấy kho theo thành phố", description = "Lấy kho theo thành phố")
    public ResponseEntity<List<Warehouse>> getWarehousesByCity(@PathVariable String city) {
        List<Warehouse> warehouses = warehouseService.getWarehousesByCity(city);
        return ResponseEntity.ok(warehouses);
    }
    
    @GetMapping("/province/{province}")
    @Operation(summary = "Lấy kho theo tỉnh", description = "Lấy kho theo tỉnh")
    public ResponseEntity<List<Warehouse>> getWarehousesByProvince(@PathVariable String province) {
        List<Warehouse> warehouses = warehouseService.getWarehousesByProvince(province);
        return ResponseEntity.ok(warehouses);
    }
    
    @PostMapping
    @Operation(summary = "Tạo kho mới", description = "Tạo kho mới")
    public ResponseEntity<?> createWarehouse(@RequestBody Warehouse warehouse) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo warehouse
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create warehouses");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Warehouse createdWarehouse = warehouseService.createWarehouse(warehouse);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdWarehouse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create warehouse: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create warehouse: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{warehouseId}")
    @Operation(summary = "Cập nhật kho", description = "Cập nhật thông tin kho")
    public ResponseEntity<?> updateWarehouse(
            @PathVariable UUID warehouseId, 
            @RequestBody Warehouse warehouseDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update warehouse
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update warehouses");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Warehouse updatedWarehouse = warehouseService.updateWarehouse(warehouseId, warehouseDetails);
            return ResponseEntity.ok(updatedWarehouse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update warehouse: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update warehouse: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{warehouseId}/activate")
    @Operation(summary = "Kích hoạt kho", description = "Kích hoạt kho")
    public ResponseEntity<?> activateWarehouse(@PathVariable UUID warehouseId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể activate warehouse
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can activate warehouses");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Warehouse activatedWarehouse = warehouseService.activateWarehouse(warehouseId);
            return ResponseEntity.ok(activatedWarehouse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to activate warehouse: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to activate warehouse: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{warehouseId}/deactivate")
    @Operation(summary = "Vô hiệu hóa kho", description = "Vô hiệu hóa kho")
    public ResponseEntity<?> deactivateWarehouse(@PathVariable UUID warehouseId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể deactivate warehouse
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can deactivate warehouses");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Warehouse deactivatedWarehouse = warehouseService.deactivateWarehouse(warehouseId);
            return ResponseEntity.ok(deactivatedWarehouse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to deactivate warehouse: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to deactivate warehouse: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{warehouseId}")
    @Operation(summary = "Xóa kho", description = "Xóa kho")
    public ResponseEntity<?> deleteWarehouse(@PathVariable UUID warehouseId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa warehouse
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete warehouses");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            warehouseService.deleteWarehouse(warehouseId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Warehouse deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete warehouse: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete warehouse: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
