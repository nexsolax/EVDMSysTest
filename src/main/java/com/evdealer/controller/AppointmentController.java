package com.evdealer.controller;

import com.evdealer.entity.Appointment;
import com.evdealer.service.AppointmentService;
import com.evdealer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    public ResponseEntity<?> getAllAppointments() {
        try {
            List<Appointment> appointments = appointmentService.getAllAppointments();
            System.out.println("AppointmentController.getAllAppointments() - Service returned " + appointments.size() + " appointments");
            
            List<Map<String, Object>> appointmentList = appointments.stream()
                .map(appointment -> {
                    try {
                        return appointmentToMap(appointment);
                    } catch (Exception e) {
                        System.err.println("AppointmentController - Error mapping appointment " + appointment.getAppointmentId() + ": " + e.getMessage());
                        e.printStackTrace();
                        Map<String, Object> errorMap = new HashMap<>();
                        try {
                            errorMap.put("appointmentId", appointment.getAppointmentId());
                        } catch (Exception e2) {
                            errorMap.put("appointmentId", "unknown");
                        }
                        errorMap.put("error", "Failed to map appointment: " + e.getMessage());
                        return errorMap;
                    }
                })
                .collect(Collectors.toList());
            
            System.out.println("AppointmentController.getAllAppointments() - Returning " + appointmentList.size() + " mapped appointments");
            return ResponseEntity.ok(appointmentList);
        } catch (Exception e) {
            System.err.println("AppointmentController.getAllAppointments() - Exception: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    private Map<String, Object> appointmentToMap(Appointment appointment) {
        Map<String, Object> appointmentMap = new HashMap<>();
        try {
            appointmentMap.put("appointmentId", appointment.getAppointmentId());
            
            // Map enum values safely
            try {
                appointmentMap.put("appointmentType", appointment.getAppointmentType() != null ? appointment.getAppointmentType().getValue() : null);
            } catch (Exception e) {
                appointmentMap.put("appointmentType", appointment.getAppointmentType() != null ? appointment.getAppointmentType().name() : null);
            }
            
            appointmentMap.put("title", appointment.getTitle());
            appointmentMap.put("description", appointment.getDescription());
            appointmentMap.put("appointmentDate", appointment.getAppointmentDate());
            appointmentMap.put("durationMinutes", appointment.getDurationMinutes());
            appointmentMap.put("location", appointment.getLocation());
            
            try {
                appointmentMap.put("status", appointment.getStatus() != null ? appointment.getStatus().getValue() : null);
            } catch (Exception e) {
                appointmentMap.put("status", appointment.getStatus() != null ? appointment.getStatus().name() : null);
            }
            
            appointmentMap.put("notes", appointment.getNotes());
            appointmentMap.put("createdAt", appointment.getCreatedAt());
            appointmentMap.put("updatedAt", appointment.getUpdatedAt());
            
            // Safely access relationships - không load nếu null hoặc có lỗi
            try {
                if (appointment.getCustomer() != null) {
                    appointmentMap.put("customerId", appointment.getCustomer().getCustomerId());
                } else {
                    appointmentMap.put("customerId", null);
                }
            } catch (Exception e) {
                // Customer có thể đã bị xóa hoặc không load được
                appointmentMap.put("customerId", null);
                appointmentMap.put("customerError", "Customer not available: " + e.getMessage());
            }
            
            try {
                if (appointment.getStaff() != null) {
                    appointmentMap.put("staffId", appointment.getStaff().getUserId());
                } else {
                    appointmentMap.put("staffId", null);
                }
            } catch (Exception e) {
                appointmentMap.put("staffId", null);
            }
            
            try {
                if (appointment.getVariant() != null) {
                    appointmentMap.put("variantId", appointment.getVariant().getVariantId());
                } else {
                    appointmentMap.put("variantId", null);
                }
            } catch (Exception e) {
                appointmentMap.put("variantId", null);
            }
        } catch (Exception e) {
            // Nếu có lỗi nghiêm trọng, vẫn cố gắng trả về appointmentId
            try {
                appointmentMap.put("appointmentId", appointment.getAppointmentId());
            } catch (Exception e2) {
                appointmentMap.put("appointmentId", "unknown");
            }
            appointmentMap.put("error", "Failed to map appointment: " + e.getMessage());
            e.printStackTrace();
        }
        return appointmentMap;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable UUID id) {
        try {
            return appointmentService.getAppointmentById(id)
                    .map(appointment -> ResponseEntity.ok(appointmentToMap(appointment)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getAppointmentsByCustomer(@PathVariable UUID customerId) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByCustomer(customerId);
            List<Map<String, Object>> appointmentList = appointments.stream().map(this::appointmentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(appointmentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<?> getAppointmentsByStaff(@PathVariable UUID staffId) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByStaff(staffId);
            List<Map<String, Object>> appointmentList = appointments.stream().map(this::appointmentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(appointmentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getAppointmentsByStatus(@PathVariable String status) {
        try {
            // Validate và convert status string to enum
            com.evdealer.enums.AppointmentStatus statusEnum = com.evdealer.enums.AppointmentStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.AppointmentStatus.values())
                    .map(com.evdealer.enums.AppointmentStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<Appointment> appointments = appointmentService.getAppointmentsByStatus(statusEnum.getValue());
            List<Map<String, Object>> appointmentList = appointments.stream().map(this::appointmentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(appointmentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/type/{appointmentType}")
    public ResponseEntity<?> getAppointmentsByType(@PathVariable String appointmentType) {
        try {
            // Validate và convert appointmentType string to enum
            com.evdealer.enums.AppointmentType typeEnum = com.evdealer.enums.AppointmentType.fromString(appointmentType);
            if (typeEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid appointment type: " + appointmentType);
                error.put("validTypes", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.AppointmentType.values())
                    .map(com.evdealer.enums.AppointmentType::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<Appointment> appointments = appointmentService.getAppointmentsByType(typeEnum.getValue());
            List<Map<String, Object>> appointmentList = appointments.stream().map(this::appointmentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(appointmentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<?> getAppointmentsByVariant(@PathVariable Integer variantId) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByVariant(variantId);
            List<Map<String, Object>> appointmentList = appointments.stream().map(this::appointmentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(appointmentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/test-drives")
    public ResponseEntity<?> getTestDriveAppointments() {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByType("test_drive");
            List<Map<String, Object>> appointmentList = appointments.stream().map(this::appointmentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(appointmentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingAppointments() {
        try {
            List<Appointment> appointments = appointmentService.getUpcomingAppointments();
            List<Map<String, Object>> appointmentList = appointments.stream().map(this::appointmentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(appointmentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> getAppointmentsByTitle(@RequestParam String title) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByTitle(title);
            List<Map<String, Object>> appointmentList = appointments.stream().map(this::appointmentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(appointmentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<?> getAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByDateRange(startDate, endDate);
            List<Map<String, Object>> appointmentList = appointments.stream().map(this::appointmentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(appointmentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Cho phép tất cả user đã authenticated tạo appointment (customer, dealer user, EVM_STAFF, ADMIN)
            Appointment createdAppointment = appointmentService.createAppointment(appointment);
            return ResponseEntity.status(HttpStatus.CREATED).body(appointmentToMap(createdAppointment));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable UUID id, @RequestBody Appointment appointmentDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Lấy appointment hiện tại để kiểm tra ownership
            Appointment existingAppointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Kiểm tra phân quyền: ADMIN, EVM_STAFF hoặc user tạo appointment
            if (!securityUtils.isAdmin() && !securityUtils.isEvmStaff()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent()) {
                    UUID currentUserId = currentUserOpt.get().getUserId();
                    // User chỉ có thể update appointment của chính mình (nếu appointment có staff hoặc customer)
                    boolean canUpdate = false;
                    if (existingAppointment.getStaff() != null && existingAppointment.getStaff().getUserId().equals(currentUserId)) {
                        canUpdate = true;
                    }
                    if (existingAppointment.getCustomer() != null && existingAppointment.getCustomer().getCustomerId() != null) {
                        // Nếu appointment có customer, có thể cần kiểm tra thêm customer ownership
                        // Hiện tại cho phép customer update appointment của mình
                        canUpdate = true;
                    }
                    if (!canUpdate) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only update your own appointments");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only admin, EVM staff or the appointment creator can update appointments");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            Appointment updatedAppointment = appointmentService.updateAppointment(id, appointmentDetails);
            return ResponseEntity.ok(appointmentToMap(updatedAppointment));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable UUID id, @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update appointment status
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update appointment status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Appointment updatedAppointment = appointmentService.updateAppointmentStatus(id, status);
            return ResponseEntity.ok(appointmentToMap(updatedAppointment));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update appointment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update appointment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable UUID id) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa appointment
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete appointments");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            appointmentService.deleteAppointment(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Appointment deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

