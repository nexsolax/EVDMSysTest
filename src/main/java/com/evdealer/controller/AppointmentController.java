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

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable UUID id) {
        return appointmentService.getAppointmentById(id)
                .map(appointment -> ResponseEntity.ok(appointment))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByCustomer(@PathVariable UUID customerId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByCustomer(customerId);
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByStaff(@PathVariable UUID staffId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByStaff(staffId);
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(@PathVariable String status) {
        List<Appointment> appointments = appointmentService.getAppointmentsByStatus(status);
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/type/{appointmentType}")
    public ResponseEntity<List<Appointment>> getAppointmentsByType(@PathVariable String appointmentType) {
        List<Appointment> appointments = appointmentService.getAppointmentsByType(appointmentType);
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByVariant(@PathVariable Integer variantId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByVariant(variantId);
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/test-drives")
    public ResponseEntity<List<Appointment>> getTestDriveAppointments() {
        List<Appointment> appointments = appointmentService.getAppointmentsByType("test_drive");
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<List<Appointment>> getUpcomingAppointments() {
        List<Appointment> appointments = appointmentService.getUpcomingAppointments();
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Appointment>> getAppointmentsByTitle(@RequestParam String title) {
        List<Appointment> appointments = appointmentService.getAppointmentsByTitle(title);
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<Appointment>> getAppointmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Appointment> appointments = appointmentService.getAppointmentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(appointments);
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
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
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
            return ResponseEntity.ok(updatedAppointment);
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
            return ResponseEntity.ok(updatedAppointment);
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

