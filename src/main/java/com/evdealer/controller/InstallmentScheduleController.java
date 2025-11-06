package com.evdealer.controller;

import com.evdealer.entity.InstallmentSchedule;
import com.evdealer.service.InstallmentScheduleService;
import com.evdealer.util.SecurityUtils;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/installment-schedules")
@CrossOrigin(origins = "*")
@Tag(name = "Installment Schedule Management", description = "APIs for managing installment schedules")
public class InstallmentScheduleController {
    
    @Autowired
    private InstallmentScheduleService installmentScheduleService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Get all installment schedules", description = "Retrieve a list of all installment schedules")
    public ResponseEntity<?> getAllInstallmentSchedules() {
        try {
            List<InstallmentSchedule> schedules = installmentScheduleService.getAllInstallmentSchedules();
            List<Map<String, Object>> scheduleList = schedules.stream().map(this::scheduleToMap).collect(Collectors.toList());
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    private Map<String, Object> scheduleToMap(InstallmentSchedule schedule) {
        Map<String, Object> scheduleMap = new HashMap<>();
        scheduleMap.put("scheduleId", schedule.getScheduleId());
        scheduleMap.put("installmentNumber", schedule.getInstallmentNumber());
        scheduleMap.put("dueDate", schedule.getDueDate());
        scheduleMap.put("amount", schedule.getAmount());
        scheduleMap.put("principalAmount", schedule.getPrincipalAmount());
        scheduleMap.put("interestAmount", schedule.getInterestAmount());
        scheduleMap.put("status", schedule.getStatus());
        scheduleMap.put("paidDate", schedule.getPaidDate());
        scheduleMap.put("paidAmount", schedule.getPaidAmount());
        scheduleMap.put("lateFee", schedule.getLateFee());
        scheduleMap.put("notes", schedule.getNotes());
        scheduleMap.put("createdAt", schedule.getCreatedAt());
        
        if (schedule.getPlan() != null) {
            scheduleMap.put("planId", schedule.getPlan().getPlanId());
        }
        
        return scheduleMap;
    }
    
    @GetMapping("/{scheduleId}")
    @Operation(summary = "Get installment schedule by ID", description = "Retrieve a specific installment schedule by its ID")
    public ResponseEntity<?> getScheduleById(@PathVariable @Parameter(description = "Schedule ID") UUID scheduleId) {
        try {
            return installmentScheduleService.getScheduleById(scheduleId)
                    .map(schedule -> ResponseEntity.ok(scheduleToMap(schedule)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/plan/{planId}")
    @Operation(summary = "Get schedules by plan", description = "Retrieve installment schedules for a specific plan")
    public ResponseEntity<?> getSchedulesByPlan(@PathVariable UUID planId) {
        try {
            List<InstallmentSchedule> schedules = installmentScheduleService.getSchedulesByPlan(planId);
            List<Map<String, Object>> scheduleList = schedules.stream().map(this::scheduleToMap).collect(Collectors.toList());
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get schedules by status", description = "Retrieve installment schedules filtered by status")
    public ResponseEntity<?> getSchedulesByStatus(@PathVariable String status) {
        try {
            List<InstallmentSchedule> schedules = installmentScheduleService.getSchedulesByStatus(status);
            List<Map<String, Object>> scheduleList = schedules.stream().map(this::scheduleToMap).collect(Collectors.toList());
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/due-date-range")
    @Operation(summary = "Get schedules by due date range", description = "Retrieve installment schedules within a due date range")
    public ResponseEntity<?> getSchedulesByDueDateRange(
            @RequestParam @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @Parameter(description = "End date") LocalDate endDate) {
        try {
            List<InstallmentSchedule> schedules = installmentScheduleService.getSchedulesByDueDateRange(startDate, endDate);
            List<Map<String, Object>> scheduleList = schedules.stream().map(this::scheduleToMap).collect(Collectors.toList());
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue schedules", description = "Retrieve installment schedules that are overdue")
    public ResponseEntity<?> getOverdueSchedules() {
        try {
            List<InstallmentSchedule> schedules = installmentScheduleService.getOverdueSchedules();
            List<Map<String, Object>> scheduleList = schedules.stream().map(this::scheduleToMap).collect(Collectors.toList());
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/due-soon")
    @Operation(summary = "Get due soon schedules", description = "Retrieve installment schedules that are due within 7 days")
    public ResponseEntity<?> getDueSoonSchedules() {
        try {
            List<InstallmentSchedule> schedules = installmentScheduleService.getDueSoonSchedules();
            List<Map<String, Object>> scheduleList = schedules.stream().map(this::scheduleToMap).collect(Collectors.toList());
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/installment-number/{installmentNumber}")
    @Operation(summary = "Get schedules by installment number", description = "Retrieve installment schedules by installment number")
    public ResponseEntity<?> getSchedulesByInstallmentNumber(@PathVariable Integer installmentNumber) {
        try {
            List<InstallmentSchedule> schedules = installmentScheduleService.getSchedulesByInstallmentNumber(installmentNumber);
            List<Map<String, Object>> scheduleList = schedules.stream().map(this::scheduleToMap).collect(Collectors.toList());
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/paid-date-range")
    @Operation(summary = "Get schedules by paid date range", description = "Retrieve installment schedules within a paid date range")
    public ResponseEntity<?> getSchedulesByPaidDateRange(
            @RequestParam @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @Parameter(description = "End date") LocalDate endDate) {
        try {
            List<InstallmentSchedule> schedules = installmentScheduleService.getSchedulesByPaidDateRange(startDate, endDate);
            List<Map<String, Object>> scheduleList = schedules.stream().map(this::scheduleToMap).collect(Collectors.toList());
            return ResponseEntity.ok(scheduleList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Create installment schedule", description = "Create a new installment schedule")
    public ResponseEntity<?> createInstallmentSchedule(@RequestBody InstallmentSchedule installmentSchedule) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo installment schedule
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create installment schedules");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            InstallmentSchedule createdSchedule = installmentScheduleService.createInstallmentSchedule(installmentSchedule);
            return ResponseEntity.status(HttpStatus.CREATED).body(scheduleToMap(createdSchedule));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create installment schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create installment schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{scheduleId}")
    @Operation(summary = "Update installment schedule", description = "Update an existing installment schedule")
    public ResponseEntity<?> updateInstallmentSchedule(
            @PathVariable UUID scheduleId, 
            @RequestBody InstallmentSchedule installmentScheduleDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update installment schedule
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update installment schedules");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            InstallmentSchedule updatedSchedule = installmentScheduleService.updateInstallmentSchedule(scheduleId, installmentScheduleDetails);
            return ResponseEntity.ok(scheduleToMap(updatedSchedule));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update installment schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update installment schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{scheduleId}/status")
    @Operation(summary = "Update schedule status", description = "Update the status of an installment schedule")
    public ResponseEntity<?> updateScheduleStatus(
            @PathVariable UUID scheduleId, 
            @RequestParam String status) {
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
            
            InstallmentSchedule updatedSchedule = installmentScheduleService.updateScheduleStatus(scheduleId, status);
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
    @Operation(summary = "Mark schedule as paid", description = "Mark an installment schedule as paid")
    public ResponseEntity<?> markAsPaid(
            @PathVariable UUID scheduleId, 
            @RequestParam LocalDate paidDate,
            @RequestParam BigDecimal paidAmount) {
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
            
            InstallmentSchedule updatedSchedule = installmentScheduleService.markAsPaid(scheduleId, paidDate, paidAmount);
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
    @Operation(summary = "Delete installment schedule", description = "Delete an installment schedule")
    public ResponseEntity<?> deleteInstallmentSchedule(@PathVariable UUID scheduleId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa installment schedule
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete installment schedules");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            installmentScheduleService.deleteInstallmentSchedule(scheduleId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Installment schedule deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete installment schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete installment schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
