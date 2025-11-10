package com.evdealer.service;

import com.evdealer.entity.Appointment;
import com.evdealer.enums.AppointmentStatus;
import com.evdealer.enums.AppointmentType;
import com.evdealer.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<Appointment> getAllAppointments() {
        System.out.println("=== AppointmentService.getAllAppointments() START ===");
        
        // Thử tất cả các cách, đảm bảo luôn trả về dữ liệu nếu có
        List<Appointment> result = null;
        
        // Method 1: Dùng findAllSimple() - tránh load lazy relationships
        try {
            System.out.println("Trying findAllSimple() first (no lazy loading)...");
            List<Appointment> appointments = appointmentRepository.findAllSimple();
            System.out.println("findAllSimple() returned " + appointments.size() + " appointments");
            if (appointments != null && appointments.size() > 0) {
                result = appointments;
                System.out.println("Using findAllSimple() result - SUCCESS!");
            }
        } catch (Exception e) {
            System.err.println("findAllSimple() failed: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
        
        // Method 2: EntityManager native query với SELECT cụ thể từng cột
        try {
            System.out.println("Trying EntityManager native query (raw with specific columns)...");
            String sql = "SELECT appointment_id, customer_id, staff_id, appointment_type, title, description, " +
                        "appointment_date, duration_minutes, location, status, notes, created_at, updated_at, variant_id " +
                        "FROM appointments ORDER BY appointment_id";
            Query nativeQuery = entityManager.createNativeQuery(sql);
            @SuppressWarnings("unchecked")
            List<Object[]> rawResults = nativeQuery.getResultList();
            System.out.println("EntityManager native query (raw) returned " + rawResults.size() + " rows");
            
            if (rawResults != null && rawResults.size() > 0) {
                // Map raw results to entities manually
                List<Appointment> appointments = new java.util.ArrayList<>();
                for (Object[] row : rawResults) {
                    try {
                        Appointment appointment = new Appointment();
                        int colIndex = 0;
                        appointment.setAppointmentId((UUID) row[colIndex++]); // appointment_id
                        // Skip customer_id (row[colIndex++]) - sẽ null, không set
                        colIndex++;
                        // Skip staff_id (row[colIndex++]) - có thể null, không set
                        colIndex++;
                        if (row[colIndex] != null) {
                            appointment.setAppointmentType(AppointmentType.fromString(row[colIndex].toString()));
                        }
                        colIndex++; // appointment_type
                        appointment.setTitle((String) row[colIndex++]); // title
                        appointment.setDescription((String) row[colIndex++]); // description
                        appointment.setAppointmentDate((java.time.LocalDateTime) row[colIndex++]); // appointment_date
                        appointment.setDurationMinutes((Integer) row[colIndex++]); // duration_minutes
                        appointment.setLocation((String) row[colIndex++]); // location
                        if (row[colIndex] != null) {
                            appointment.setStatus(AppointmentStatus.fromString(row[colIndex].toString()));
                        }
                        colIndex++; // status
                        appointment.setNotes((String) row[colIndex++]); // notes
                        appointment.setCreatedAt((java.time.LocalDateTime) row[colIndex++]); // created_at
                        appointment.setUpdatedAt((java.time.LocalDateTime) row[colIndex++]); // updated_at
                        // Skip variant_id (row[colIndex++]) - có thể null, không set
                        appointments.add(appointment);
                    } catch (Exception e) {
                        System.err.println("Error mapping row to Appointment: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                System.out.println("Mapped " + appointments.size() + " appointments from raw results");
                if (appointments.size() > 0) {
                    result = appointments;
                    System.out.println("Using EntityManager native query (raw) result");
                }
            }
        } catch (Exception e) {
            System.err.println("EntityManager native query (raw) failed: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
        
        // Method 3: EntityManager native query với entity mapping
        if (result == null || result.isEmpty()) {
            try {
                System.out.println("Trying EntityManager native query (with entity mapping)...");
                Query nativeQuery = entityManager.createNativeQuery("SELECT * FROM appointments ORDER BY appointment_id", Appointment.class);
                @SuppressWarnings("unchecked")
                List<Appointment> appointments = nativeQuery.getResultList();
                System.out.println("EntityManager native query (entity) returned " + appointments.size() + " appointments");
                if (appointments != null && appointments.size() > 0) {
                    result = appointments;
                    System.out.println("Using EntityManager native query (entity) result");
                }
            } catch (Exception e) {
                System.err.println("EntityManager native query (entity) failed: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Method 4: Repository native query
        if (result == null || result.isEmpty()) {
            try {
                System.out.println("Trying Repository native query...");
                List<Appointment> appointments = appointmentRepository.findAllNative();
                System.out.println("Repository native query returned " + appointments.size() + " appointments");
                if (appointments != null && appointments.size() > 0) {
                    result = appointments;
                    System.out.println("Using Repository native query result");
                }
            } catch (Exception e) {
                System.err.println("Repository native query failed: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Method 5: findAll() - thử cuối cùng vì có thể gặp lỗi lazy loading
        if (result == null || result.isEmpty()) {
            try {
                System.out.println("Trying findAll() as last resort...");
                List<Appointment> appointments = appointmentRepository.findAll();
                System.out.println("findAll() returned " + appointments.size() + " appointments");
                if (appointments != null && appointments.size() > 0) {
                    result = appointments;
                    System.out.println("Using findAll() result");
                }
            } catch (Exception e) {
                System.err.println("findAll() failed: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Xử lý kết quả
        if (result == null) {
            System.out.println("All methods returned null or empty, returning empty list");
            return new java.util.ArrayList<>();
        }
        
        System.out.println("Final result size: " + result.size());
        System.out.println("=== AppointmentService.getAllAppointments() END ===");
        return result;
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

