package com.evdealer.controller;

import com.evdealer.entity.TestDriveSchedule;
import com.evdealer.service.TestDriveScheduleService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test-drive-schedules")
@CrossOrigin(origins = "*")
@Tag(name = "Test Drive Schedule Management", description = "APIs quản lý lịch lái thử xe")
public class TestDriveScheduleController {
    
    @Autowired
    private TestDriveScheduleService testDriveScheduleService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy tất cả lịch lái thử", description = "Lấy danh sách tất cả lịch lái thử xe")
    public ResponseEntity<?> getAllTestDriveSchedules() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<TestDriveSchedule> schedules = testDriveScheduleService.getAllTestDriveSchedules();
            
            List<Map<String, Object>> scheduleList = schedules.stream()
                .map(this::scheduleToMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve test drive schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{scheduleId}")
    @Operation(summary = "Lấy lịch lái thử theo ID", description = "Lấy thông tin lịch lái thử theo ID")
    public ResponseEntity<?> getTestDriveScheduleById(@PathVariable UUID scheduleId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            return testDriveScheduleService.getTestDriveScheduleById(scheduleId)
                .map(schedule -> ResponseEntity.ok(scheduleToMap(schedule)))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve test drive schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Lấy lịch lái thử theo khách hàng", description = "Lấy danh sách lịch lái thử của một khách hàng")
    public ResponseEntity<?> getSchedulesByCustomer(@PathVariable UUID customerId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<TestDriveSchedule> schedules = testDriveScheduleService.getSchedulesByCustomer(customerId);
            List<Map<String, Object>> scheduleList = schedules.stream()
                .map(this::scheduleToMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/variant/{variantId}")
    @Operation(summary = "Lấy lịch lái thử theo variant", description = "Lấy danh sách lịch lái thử của một variant xe")
    public ResponseEntity<?> getSchedulesByVariant(@PathVariable UUID variantId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<TestDriveSchedule> schedules = testDriveScheduleService.getSchedulesByVariant(variantId);
            List<Map<String, Object>> scheduleList = schedules.stream()
                .map(this::scheduleToMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Lấy lịch lái thử theo trạng thái", description = "Lấy danh sách lịch lái thử theo trạng thái")
    public ResponseEntity<?> getSchedulesByStatus(@PathVariable String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<TestDriveSchedule> schedules = testDriveScheduleService.getSchedulesByStatus(status);
            List<Map<String, Object>> scheduleList = schedules.stream()
                .map(this::scheduleToMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/date/{date}")
    @Operation(summary = "Lấy lịch lái thử theo ngày", description = "Lấy danh sách lịch lái thử theo ngày")
    public ResponseEntity<?> getSchedulesByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<TestDriveSchedule> schedules = testDriveScheduleService.getSchedulesByDate(date);
            List<Map<String, Object>> scheduleList = schedules.stream()
                .map(this::scheduleToMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Tạo lịch lái thử mới", description = "Tạo lịch lái thử xe mới")
    public ResponseEntity<?> createTestDriveSchedule(@RequestBody TestDriveSchedule schedule) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            TestDriveSchedule createdSchedule = testDriveScheduleService.createTestDriveSchedule(schedule);
            return ResponseEntity.status(HttpStatus.CREATED).body(scheduleToMap(createdSchedule));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{scheduleId}")
    @Operation(summary = "Cập nhật lịch lái thử", description = "Cập nhật thông tin lịch lái thử")
    public ResponseEntity<?> updateTestDriveSchedule(
            @PathVariable UUID scheduleId,
            @RequestBody TestDriveSchedule scheduleDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            TestDriveSchedule updatedSchedule = testDriveScheduleService.updateTestDriveSchedule(scheduleId, scheduleDetails);
            return ResponseEntity.ok(scheduleToMap(updatedSchedule));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "Xóa lịch lái thử", description = "Xóa lịch lái thử")
    public ResponseEntity<?> deleteTestDriveSchedule(@PathVariable UUID scheduleId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete test drive schedules");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            testDriveScheduleService.deleteTestDriveSchedule(scheduleId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Test drive schedule deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    private Map<String, Object> scheduleToMap(TestDriveSchedule schedule) {
        Map<String, Object> map = new HashMap<>();
        map.put("scheduleId", schedule.getScheduleId());
        map.put("preferredDate", schedule.getPreferredDate());
        map.put("preferredTime", schedule.getPreferredTime());
        map.put("status", schedule.getStatus());
        map.put("notes", schedule.getNotes());
        map.put("createdAt", schedule.getCreatedAt());
        map.put("updatedAt", schedule.getUpdatedAt());
        
        if (schedule.getCustomer() != null) {
            map.put("customerId", schedule.getCustomer().getCustomerId());
        }
        if (schedule.getVariant() != null) {
            map.put("variantId", schedule.getVariant().getVariantId());
        }
        
        return map;
    }
}

