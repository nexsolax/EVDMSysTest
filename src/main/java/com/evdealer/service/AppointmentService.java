package com.evdealer.service;

import com.evdealer.entity.Appointment;
import com.evdealer.enums.AppointmentStatus;
import com.evdealer.enums.AppointmentType;
import com.evdealer.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    public List<Appointment> getAllAppointments() {
        try {
            return appointmentRepository.findAll();
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    public Optional<Appointment> getAppointmentById(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }
    
    public List<Appointment> getAppointmentsByCustomer(UUID customerId) {
        return appointmentRepository.findByCustomerId(customerId);
    }
    
    public List<Appointment> getAppointmentsByStaff(UUID staffId) {
        return appointmentRepository.findByStaffId(staffId);
    }
    
    public List<Appointment> getAppointmentsByStatus(String status) {
        // Convert string to enum for validation
        AppointmentStatus statusEnum = AppointmentStatus.fromString(status);
        return appointmentRepository.findByStatus(statusEnum);
    }
    
    public List<Appointment> getAppointmentsByType(String appointmentType) {
        // Convert string to enum for validation
        AppointmentType typeEnum = AppointmentType.fromString(appointmentType);
        return appointmentRepository.findByAppointmentType(typeEnum);
    }
    
    public List<Appointment> getAppointmentsByVariant(Integer variantId) {
        return appointmentRepository.findByVariantId(variantId);
    }
    
    public List<Appointment> getAppointmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByAppointmentDateBetween(startDate, endDate);
    }
    
    public List<Appointment> getCustomerAppointmentsByDateRange(UUID customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByCustomerIdAndAppointmentDateBetween(customerId, startDate, endDate);
    }
    
    public List<Appointment> getStaffAppointmentsByDateRange(UUID staffId, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByStaffIdAndAppointmentDateBetween(staffId, startDate, endDate);
    }
    
    public List<Appointment> getUpcomingAppointments() {
        return appointmentRepository.findUpcomingAppointments(LocalDateTime.now(), AppointmentStatus.SCHEDULED);
    }
    
    public List<Appointment> getAppointmentsByTitle(String title) {
        return appointmentRepository.findByTitleContaining(title);
    }
    
    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }
    
    public Appointment updateAppointment(UUID appointmentId, Appointment appointmentDetails) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
        
        appointment.setCustomer(appointmentDetails.getCustomer());
        appointment.setStaff(appointmentDetails.getStaff());
        appointment.setVariant(appointmentDetails.getVariant());
        appointment.setAppointmentType(appointmentDetails.getAppointmentType());
        appointment.setTitle(appointmentDetails.getTitle());
        appointment.setDescription(appointmentDetails.getDescription());
        appointment.setAppointmentDate(appointmentDetails.getAppointmentDate());
        appointment.setDurationMinutes(appointmentDetails.getDurationMinutes());
        appointment.setLocation(appointmentDetails.getLocation());
        appointment.setStatus(appointmentDetails.getStatus());
        appointment.setNotes(appointmentDetails.getNotes());
        
        return appointmentRepository.save(appointment);
    }
    
    public void deleteAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
        appointmentRepository.delete(appointment);
    }
    
    public Appointment updateAppointmentStatus(UUID appointmentId, String status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
        AppointmentStatus statusEnum = AppointmentStatus.fromString(status);
        appointment.setStatus(statusEnum);
        return appointmentRepository.save(appointment);
    }
}

