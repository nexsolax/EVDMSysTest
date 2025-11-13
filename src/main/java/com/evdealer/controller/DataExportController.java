package com.evdealer.controller;

import com.evdealer.service.DataExportService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/data-export")
@CrossOrigin(origins = "*")
@Tag(name = "Data Export", description = "APIs để xuất và thống kê toàn bộ dữ liệu từ tất cả bảng")
public class DataExportController {
    
    @Autowired
    private DataExportService dataExportService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping("/all")
    @Operation(summary = "Lấy toàn bộ dữ liệu", description = "Lấy tất cả dữ liệu từ tất cả các bảng trong database. Mỗi entity được xử lý độc lập, nếu có lỗi ở một entity thì không ảnh hưởng đến các entity khác.")
    public ResponseEntity<?> getAllData() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể export toàn bộ dữ liệu
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can export all data");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Map<String, Object> allData = dataExportService.getAllData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("timestamp", java.time.LocalDateTime.now());
            response.put("data", allData);
            response.put("entityCount", allData.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to export data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Thống kê dữ liệu", description = "Thống kê số lượng records cho từng entity và phân loại entities có dữ liệu vs entities rỗng")
    public ResponseEntity<?> getDataStatistics() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xem thống kê
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can view data statistics");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Map<String, Object> statistics = dataExportService.getDataStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("timestamp", java.time.LocalDateTime.now());
            response.putAll(statistics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/delete-all")
    @Operation(summary = "Xóa toàn bộ dữ liệu", description = "Xóa tất cả dữ liệu từ tất cả các bảng trong database. Chỉ giữ lại tài khoản admin (username: admin). Xóa theo thứ tự đúng để tránh lỗi foreign key constraint.")
    public ResponseEntity<?> deleteAllData() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa toàn bộ dữ liệu
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete all data");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Map<String, Object> deleteResult = dataExportService.deleteAllData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("timestamp", java.time.LocalDateTime.now());
            response.put("message", "All data deleted successfully. Admin user preserved.");
            response.putAll(deleteResult);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete all data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

