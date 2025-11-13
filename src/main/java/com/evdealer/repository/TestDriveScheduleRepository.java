package com.evdealer.repository;

import com.evdealer.entity.TestDriveSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestDriveScheduleRepository extends JpaRepository<TestDriveSchedule, UUID> {
    
    @Query("SELECT DISTINCT tds FROM TestDriveSchedule tds LEFT JOIN FETCH tds.customer LEFT JOIN FETCH tds.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand")
    List<TestDriveSchedule> findAllWithDetails();
    
    @Query(value = "SELECT * FROM test_drive_schedules ORDER BY schedule_id", nativeQuery = true)
    List<TestDriveSchedule> findAllNative();
    
    @Query("SELECT tds FROM TestDriveSchedule tds WHERE tds.customer.customerId = :customerId")
    List<TestDriveSchedule> findByCustomerCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT tds FROM TestDriveSchedule tds WHERE tds.variant.variantId = :variantId")
    List<TestDriveSchedule> findByVariantVariantId(@Param("variantId") UUID variantId);
    
    List<TestDriveSchedule> findByStatus(String status);
    
    @Query("SELECT tds FROM TestDriveSchedule tds WHERE tds.preferredDate = :date")
    List<TestDriveSchedule> findByPreferredDate(@Param("date") LocalDate date);
    
    @Query("SELECT tds FROM TestDriveSchedule tds WHERE tds.preferredDate BETWEEN :startDate AND :endDate")
    List<TestDriveSchedule> findByPreferredDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT tds FROM TestDriveSchedule tds WHERE tds.preferredDate = :date AND tds.preferredTime = :time")
    List<TestDriveSchedule> findByPreferredDateAndTime(@Param("date") LocalDate date, @Param("time") LocalTime time);
}

