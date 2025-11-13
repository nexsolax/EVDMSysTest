package com.evdealer.entity;

import com.evdealer.enums.AppointmentStatus;
import com.evdealer.enums.AppointmentType;
import com.evdealer.converter.AppointmentTypeConverter;
import com.evdealer.converter.AppointmentStatusConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "appointment_id")
    private UUID appointmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = true)
    private User staff;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = true)
    private VehicleVariant variant;
    
    @Convert(converter = AppointmentTypeConverter.class)
    @Column(name = "appointment_type", length = 50, nullable = false)
    private AppointmentType appointmentType = AppointmentType.CONSULTATION;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "appointment_date", nullable = false)
    private LocalDateTime appointmentDate;
    
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 60;
    
    @Column(name = "location", length = 255)
    private String location;
    
    @Convert(converter = AppointmentStatusConverter.class)
    @Column(name = "status", length = 50, nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Appointment() {}
    
    public Appointment(Customer customer, String title, LocalDateTime appointmentDate) {
        this.customer = customer;
        this.title = title;
        this.appointmentDate = appointmentDate;
    }
    
    // Getters and Setters
    public UUID getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public User getStaff() {
        return staff;
    }
    
    public void setStaff(User staff) {
        this.staff = staff;
    }
    
    public VehicleVariant getVariant() {
        return variant;
    }
    
    public void setVariant(VehicleVariant variant) {
        this.variant = variant;
    }
    
    public AppointmentType getAppointmentType() {
        return appointmentType;
    }
    
    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }
    
    /**
     * Set appointmentType from String (backward compatibility)
     */
    public void setAppointmentType(String appointmentType) {
        this.appointmentType = AppointmentType.fromString(appointmentType);
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
    
    public AppointmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
    
    /**
     * Set status from String (backward compatibility)
     */
    public void setStatus(String status) {
        this.status = AppointmentStatus.fromString(status);
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return java.util.Objects.equals(appointmentId, that.appointmentId);
    }

    @Override
    public int hashCode() {
        return appointmentId != null ? appointmentId.hashCode() : 0;
    }
}

