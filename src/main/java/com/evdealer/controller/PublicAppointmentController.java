package com.evdealer.controller;

import com.evdealer.entity.Appointment;
import com.evdealer.enums.AppointmentType;
import com.evdealer.enums.AppointmentStatus;
import com.evdealer.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/appointments")
@CrossOrigin(origins = "*")
@Tag(name = "Public Appointment Management", description = "APIs đặt lịch cho khách vãng lai - không cần đăng nhập")
public class PublicAppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @PostMapping("/test-drive")
    @Operation(summary = "Đặt lịch lái thử", description = "Khách vãng lai có thể đặt lịch lái thử xe")
    public ResponseEntity<?> bookTestDrive(@RequestBody Map<String, Object> request) {
        try {
            String customerName = (String) request.get("customerName");
            String customerPhone = (String) request.get("customerPhone");
            String customerEmail = (String) request.get("customerEmail");
            Integer variantId = (Integer) request.get("variantId");
            String appointmentDateStr = (String) request.get("appointmentDate");
            LocalDateTime appointmentDate = LocalDateTime.parse(appointmentDateStr);
            String notes = (String) request.get("notes");
            
            Appointment appointment = new Appointment();
            appointment.setTitle("Lái thử xe - " + customerName);
            appointment.setAppointmentType(AppointmentType.TEST_DRIVE);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            appointment.setNotes(notes);
            appointment.setNotes((appointment.getNotes() != null ? appointment.getNotes() + "\n" : "") + 
                                "Customer: " + customerName + " (" + customerEmail + ", " + customerPhone + "), Variant ID: " + variantId);
            
            Appointment createdAppointment = appointmentService.createAppointment(appointment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test drive appointment booked successfully");
            response.put("appointmentId", createdAppointment.getAppointmentId());
            response.put("appointmentDate", appointmentDate);
            response.put("customerName", customerName);
            response.put("status", "scheduled");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Test drive booking failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/delivery")
    @Operation(summary = "Đặt lịch nhận xe", description = "Khách vãng lai có thể đặt lịch nhận xe")
    public ResponseEntity<?> bookDelivery(@RequestBody Map<String, Object> request) {
        try {
            String customerName = (String) request.get("customerName");
            String customerPhone = (String) request.get("customerPhone");
            String customerEmail = (String) request.get("customerEmail");
            UUID orderId = UUID.fromString(request.get("orderId").toString());
            String appointmentDateStr = (String) request.get("appointmentDate");
            LocalDateTime appointmentDate = LocalDateTime.parse(appointmentDateStr);
            String deliveryAddress = (String) request.get("deliveryAddress");
            String notes = (String) request.get("notes");
            
            Appointment appointment = new Appointment();
            appointment.setTitle("Nhận xe - " + customerName);
            appointment.setAppointmentType(AppointmentType.DELIVERY);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            appointment.setNotes(notes);
            appointment.setNotes((appointment.getNotes() != null ? appointment.getNotes() + "\n" : "") + 
                                "Customer: " + customerName + " (" + customerEmail + ", " + customerPhone + "), Order ID: " + orderId + 
                                (deliveryAddress != null ? ", Address: " + deliveryAddress : ""));
            
            Appointment createdAppointment = appointmentService.createAppointment(appointment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Delivery appointment booked successfully");
            response.put("appointmentId", createdAppointment.getAppointmentId());
            response.put("appointmentDate", appointmentDate);
            response.put("customerName", customerName);
            response.put("orderId", orderId);
            response.put("status", "scheduled");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Delivery booking failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{appointmentId}")
    @Operation(summary = "Xem chi tiết lịch hẹn", description = "Khách vãng lai có thể xem chi tiết lịch hẹn")
    public ResponseEntity<?> getAppointmentById(@PathVariable UUID appointmentId) {
        try {
            return appointmentService.getAppointmentById(appointmentId)
                    .map(appointment -> {
                        Map<String, Object> details = new HashMap<>();
                        details.put("appointmentId", appointment.getAppointmentId());
                        details.put("title", appointment.getTitle());
                        details.put("appointmentType", appointment.getAppointmentType() != null ? appointment.getAppointmentType().getValue() : null);
                        details.put("appointmentDate", appointment.getAppointmentDate());
                        details.put("status", appointment.getStatus() != null ? appointment.getStatus().getValue() : null);
                        details.put("customerName", appointment.getCustomer() != null ? 
                            appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName() : "N/A");
                        details.put("customerPhone", appointment.getCustomer() != null ? 
                            appointment.getCustomer().getPhone() : "N/A");
                        details.put("customerEmail", appointment.getCustomer() != null ? 
                            appointment.getCustomer().getEmail() : "N/A");
                        details.put("notes", appointment.getNotes());
                        details.put("deliveryAddress", "See notes");
                        details.put("createdAt", appointment.getCreatedAt());
                        
                        return ResponseEntity.ok(details);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{appointmentId}/reschedule")
    @Operation(summary = "Đổi lịch", description = "Khách vãng lai có thể đổi lịch hẹn")
    public ResponseEntity<?> rescheduleAppointment(
            @PathVariable UUID appointmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDate,
            @RequestParam(required = false) String reason) {
        try {
            var appointment = appointmentService.getAppointmentById(appointmentId);
            if (appointment.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Appointment appointmentEntity = appointment.get();
            appointmentEntity.setAppointmentDate(newDate);
            appointmentEntity.setStatus(AppointmentStatus.SCHEDULED); // Rescheduled is still scheduled
            if (reason != null && !reason.trim().isEmpty()) {
                appointmentEntity.setNotes((appointmentEntity.getNotes() != null ? appointmentEntity.getNotes() + "\n" : "") + 
                                         "Reschedule reason: " + reason);
            }
            
            appointmentService.updateAppointment(appointmentId, appointmentEntity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment rescheduled successfully");
            response.put("appointmentId", appointmentId);
            response.put("newDate", newDate);
            response.put("status", "rescheduled");
            response.put("reason", reason);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Reschedule failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{appointmentId}/cancel")
    @Operation(summary = "Hủy lịch", description = "Khách vãng lai có thể hủy lịch hẹn")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable UUID appointmentId,
            @RequestParam(required = false) String reason) {
        try {
            var appointment = appointmentService.getAppointmentById(appointmentId);
            if (appointment.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Appointment appointmentEntity = appointment.get();
            appointmentEntity.setStatus(AppointmentStatus.CANCELLED);
            if (reason != null && !reason.trim().isEmpty()) {
                appointmentEntity.setNotes((appointmentEntity.getNotes() != null ? appointmentEntity.getNotes() + "\n" : "") + 
                                         "Cancellation reason: " + reason);
            }
            
            appointmentService.updateAppointment(appointmentId, appointmentEntity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment cancelled successfully");
            response.put("appointmentId", appointmentId);
            response.put("status", AppointmentStatus.CANCELLED.getValue());
            response.put("reason", reason);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Cancellation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/available-slots")
    @Operation(summary = "Xem lịch trống", description = "Khách vãng lai có thể xem các lịch trống có sẵn")
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date,
            @RequestParam(required = false) String appointmentType) {
        try {
            // In a real implementation, you would check actual availability
            Map<String, Object> slots = new HashMap<>();
            slots.put("date", date);
            slots.put("appointmentType", appointmentType != null ? appointmentType : "any");
            slots.put("availableSlots", new String[]{
                "09:00", "10:00", "11:00", "14:00", "15:00", "16:00"
            });
            slots.put("message", "Available time slots for the selected date");
            
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve available slots: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/types")
    @Operation(summary = "Loại lịch hẹn", description = "Khách vãng lai có thể xem các loại lịch hẹn có sẵn")
    public ResponseEntity<?> getAppointmentTypes() {
        try {
            Map<String, Object> types = new HashMap<>();
            types.put("availableTypes", new String[]{
                "test_drive", "delivery", "consultation", "maintenance"
            });
            types.put("typeDescriptions", Map.of(
                "test_drive", "Lái thử xe",
                "delivery", "Nhận xe",
                "consultation", "Tư vấn",
                "maintenance", "Bảo trì"
            ));
            types.put("duration", Map.of(
                "test_drive", "30-60 minutes",
                "delivery", "60-120 minutes",
                "consultation", "30-45 minutes",
                "maintenance", "120-240 minutes"
            ));
            
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve appointment types: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
