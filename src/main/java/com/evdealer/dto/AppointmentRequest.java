package com.evdealer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Appointment request DTO for public booking")
public class AppointmentRequest {
    
    @Schema(description = "Customer ID", example = "78fe7eb0-ceb8-4793-a8af-187a3fe26f67")
    private UUID customerId;
    
    @Schema(description = "Vehicle variant ID", example = "1")
    private Integer variantId;
    
    @Schema(description = "Appointment type", example = "test_drive", allowableValues = {"test_drive", "consultation", "delivery", "service", "inspection"})
    private String appointmentType;
    
    @Schema(description = "Appointment title", example = "Test Drive - Tesla Model 3")
    private String title;
    
    @Schema(description = "Appointment description", example = "Test drive for Tesla Model 3 Standard Range")
    private String description;
    
    @Schema(description = "Appointment date and time", example = "2025-01-25T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime appointmentDate;
    
    @Schema(description = "Duration in minutes", example = "60")
    private Integer durationMinutes;
    
    @Schema(description = "Location", example = "Showroom District 1")
    private String location;
    
    @Schema(description = "Status", example = "pending", allowableValues = {"pending", "scheduled", "confirmed", "completed", "cancelled"})
    private String status;
    
    @Schema(description = "Notes", example = "Customer prefers morning appointment")
    private String notes;
    
    // Constructors
    public AppointmentRequest() {}
    
    public AppointmentRequest(UUID customerId, String appointmentType, LocalDateTime appointmentDate) {
        this.customerId = customerId;
        this.appointmentType = appointmentType;
        this.appointmentDate = appointmentDate;
    }
    
    // Getters and Setters
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public Integer getVariantId() {
        return variantId;
    }
    
    public void setVariantId(Integer variantId) {
        this.variantId = variantId;
    }
    
    public String getAppointmentType() {
        return appointmentType;
    }
    
    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }
    
    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
