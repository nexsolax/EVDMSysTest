package com.evdealer.repository;

import com.evdealer.entity.Appointment;
import com.evdealer.enums.AppointmentStatus;
import com.evdealer.enums.AppointmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    
    @Query("SELECT a FROM Appointment a WHERE a.customer.customerId = :customerId")
    List<Appointment> findByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT a FROM Appointment a WHERE a.staff.userId = :staffId")
    List<Appointment> findByStaffId(@Param("staffId") UUID staffId);
    
    @Query("SELECT a FROM Appointment a WHERE a.status = :status")
    List<Appointment> findByStatus(@Param("status") AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentType = :appointmentType")
    List<Appointment> findByAppointmentType(@Param("appointmentType") AppointmentType appointmentType);
    
    @Query("SELECT a FROM Appointment a WHERE a.variant.variantId = :variantId")
    List<Appointment> findByVariantId(@Param("variantId") Integer variantId);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByAppointmentDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.customer.customerId = :customerId AND a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByCustomerIdAndAppointmentDateBetween(@Param("customerId") UUID customerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.staff.userId = :staffId AND a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByStaffIdAndAppointmentDateBetween(@Param("staffId") UUID staffId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate >= :date AND a.status = :status")
    List<Appointment> findUpcomingAppointments(@Param("date") LocalDateTime date, @Param("status") AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.title LIKE %:title%")
    List<Appointment> findByTitleContaining(@Param("title") String title);
    
    // Native query để lấy tất cả appointments, tránh lỗi khi có foreign key null
    @Query(value = "SELECT * FROM appointments ORDER BY appointment_id", nativeQuery = true)
    List<Appointment> findAllNative();
    
    // Query với DISTINCT và LEFT JOIN để tránh duplicate và lỗi khi customer null
    @Query("SELECT DISTINCT a FROM Appointment a LEFT JOIN FETCH a.customer LEFT JOIN FETCH a.staff LEFT JOIN FETCH a.variant")
    List<Appointment> findAllWithRelationships();
    
    // Query đơn giản không load relationships - để tránh lỗi khi customer null
    @Query("SELECT a FROM Appointment a")
    List<Appointment> findAllSimple();
}

