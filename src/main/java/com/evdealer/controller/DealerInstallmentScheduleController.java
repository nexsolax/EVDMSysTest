package com.evdealer.controller;

import com.evdealer.entity.DealerInstallmentSchedule;
import com.evdealer.service.DealerInstallmentScheduleService;
import com.evdealer.util.SecurityUtils;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dealer-installment-schedules")
@CrossOrigin(origins = "*")
@Tag(name = "Dealer Installment Schedule Management", description = "APIs quản lý lịch trả góp đại lý")
public class DealerInstallmentScheduleController {
    
    @Autowired
    private DealerInstallmentScheduleService dealerInstallmentScheduleService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy tất cả lịch trả góp đại lý", description = "Lấy danh sách tất cả lịch trả góp đại lý")
    public ResponseEntity<?> getAllSchedules() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerInstallmentSchedule> schedules = dealerInstallmentScheduleService.getAllSchedules();
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
    
    @GetMapping("/{scheduleId}")
    @Operation(summary = "Lấy lịch trả góp theo ID", description = "Lấy thông tin chi tiết của một lịch trả góp")
    public ResponseEntity<?> getScheduleById(@PathVariable UUID scheduleId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            return dealerInstallmentScheduleService.getScheduleById(scheduleId)
                    .map(schedule -> ResponseEntity.ok(scheduleToMap(schedule)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/plan/{planId}")
    @Operation(summary = "Lấy lịch trả góp theo plan", description = "Lấy danh sách lịch trả góp cho một plan cụ thể")
    public ResponseEntity<?> getSchedulesByPlanId(@PathVariable UUID planId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerInstallmentSchedule> schedules = dealerInstallmentScheduleService.getSchedulesByPlanId(planId);
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
    @Operation(summary = "Lấy lịch trả góp theo trạng thái", description = "Lấy danh sách lịch trả góp theo trạng thái")
    public ResponseEntity<?> getSchedulesByStatus(@PathVariable String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerInstallmentSchedule> schedules = dealerInstallmentScheduleService.getSchedulesByStatus(status);
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
    @Operation(summary = "Tạo lịch trả góp mới", description = "Tạo một lịch trả góp đại lý mới")
    public ResponseEntity<?> createSchedule(@RequestBody DealerInstallmentSchedule schedule) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo schedule
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create installment schedules");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerInstallmentSchedule createdSchedule = dealerInstallmentScheduleService.createSchedule(schedule);
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
    @Operation(summary = "Cập nhật lịch trả góp", description = "Cập nhật thông tin của một lịch trả góp")
    public ResponseEntity<?> updateSchedule(@PathVariable UUID scheduleId, @RequestBody DealerInstallmentSchedule scheduleDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update schedule
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update installment schedules");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerInstallmentSchedule updatedSchedule = dealerInstallmentScheduleService.updateSchedule(scheduleId, scheduleDetails);
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
    
    @PutMapping("/{scheduleId}/status")
    @Operation(summary = "Cập nhật trạng thái lịch trả góp", description = "Cập nhật trạng thái của một lịch trả góp")
    public ResponseEntity<?> updateScheduleStatus(@PathVariable UUID scheduleId, @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update schedule status
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update installment schedule status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerInstallmentSchedule updatedSchedule = dealerInstallmentScheduleService.updateScheduleStatus(scheduleId, status);
            return ResponseEntity.ok(scheduleToMap(updatedSchedule));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update schedule status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update schedule status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{scheduleId}/mark-paid")
    @Operation(summary = "Đánh dấu đã thanh toán", description = "Đánh dấu một lịch trả góp đã được thanh toán")
    public ResponseEntity<?> markAsPaid(
            @PathVariable UUID scheduleId,
            @RequestParam java.time.LocalDate paidDate,
            @RequestParam java.math.BigDecimal paidAmount) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể mark schedule as paid
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can mark installment schedules as paid");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerInstallmentSchedule updatedSchedule = dealerInstallmentScheduleService.markAsPaid(scheduleId, paidDate, paidAmount);
            return ResponseEntity.ok(scheduleToMap(updatedSchedule));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to mark schedule as paid: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to mark schedule as paid: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "Xóa lịch trả góp", description = "Xóa một lịch trả góp đại lý")
    public ResponseEntity<?> deleteSchedule(@PathVariable UUID scheduleId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa schedule
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete installment schedules");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            dealerInstallmentScheduleService.deleteSchedule(scheduleId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Installment schedule deleted successfully");
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
    
    private Map<String, Object> scheduleToMap(DealerInstallmentSchedule schedule) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("scheduleId", schedule.getScheduleId());
            map.put("installmentNumber", schedule.getInstallmentNumber());
            map.put("dueDate", schedule.getDueDate());
            map.put("amount", schedule.getAmount());
            map.put("principalAmount", schedule.getPrincipalAmount());
            map.put("interestAmount", schedule.getInterestAmount());
            map.put("status", schedule.getStatus());
            map.put("paidDate", schedule.getPaidDate());
            map.put("paidAmount", schedule.getPaidAmount());
            map.put("lateFee", schedule.getLateFee());
            map.put("notes", schedule.getNotes());
            map.put("createdAt", schedule.getCreatedAt());
            
            if (schedule.getPlan() != null) {
                try {
                    Map<String, Object> planMap = new HashMap<>();
                    planMap.put("planId", schedule.getPlan().getPlanId());
                    map.put("plan", planMap);
                } catch (Exception e) {
                    map.put("plan", null);
                }
            } else {
                map.put("plan", null);
            }
        } catch (Exception e) {
            // Return partial data on error
        }
        return map;
    }
}

