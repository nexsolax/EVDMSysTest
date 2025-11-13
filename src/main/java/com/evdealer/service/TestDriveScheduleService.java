package com.evdealer.service;

import com.evdealer.entity.TestDriveSchedule;
import com.evdealer.repository.TestDriveScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TestDriveScheduleService {
    
    @Autowired
    private TestDriveScheduleRepository testDriveScheduleRepository;
    
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public List<TestDriveSchedule> getAllTestDriveSchedules() {
        try {
            // Try native query first to avoid lazy loading issues
            List<TestDriveSchedule> schedules = testDriveScheduleRepository.findAllNative();
            if (schedules != null && !schedules.isEmpty()) {
                return schedules;
            }
        } catch (Exception e) {
            System.err.println("TestDriveScheduleService.getAllTestDriveSchedules() - Native query failed: " + e.getMessage());
        }
        
        // Fallback to simple findAll
        try {
            return testDriveScheduleRepository.findAll();
        } catch (Exception e) {
            System.err.println("TestDriveScheduleService.getAllTestDriveSchedules() - findAll failed: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<TestDriveSchedule> getTestDriveScheduleById(UUID scheduleId) {
        return testDriveScheduleRepository.findById(scheduleId);
    }
    
    @Transactional(readOnly = true)
    public List<TestDriveSchedule> getSchedulesByCustomer(UUID customerId) {
        return testDriveScheduleRepository.findByCustomerCustomerId(customerId);
    }
    
    @Transactional(readOnly = true)
    public List<TestDriveSchedule> getSchedulesByVariant(UUID variantId) {
        return testDriveScheduleRepository.findByVariantVariantId(variantId);
    }
    
    @Transactional(readOnly = true)
    public List<TestDriveSchedule> getSchedulesByStatus(String status) {
        return testDriveScheduleRepository.findByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<TestDriveSchedule> getSchedulesByDate(LocalDate date) {
        return testDriveScheduleRepository.findByPreferredDate(date);
    }
    
    @Transactional(readOnly = true)
    public List<TestDriveSchedule> getSchedulesByDateRange(LocalDate startDate, LocalDate endDate) {
        return testDriveScheduleRepository.findByPreferredDateBetween(startDate, endDate);
    }
    
    public TestDriveSchedule createTestDriveSchedule(TestDriveSchedule schedule) {
        if (schedule == null) {
            throw new RuntimeException("Test drive schedule cannot be null");
        }
        if (schedule.getPreferredDate() == null) {
            throw new RuntimeException("Preferred date is required");
        }
        if (schedule.getPreferredTime() == null) {
            throw new RuntimeException("Preferred time is required");
        }
        if (schedule.getStatus() == null || schedule.getStatus().trim().isEmpty()) {
            schedule.setStatus("pending");
        }
        return testDriveScheduleRepository.save(schedule);
    }
    
    public TestDriveSchedule updateTestDriveSchedule(UUID scheduleId, TestDriveSchedule scheduleDetails) {
        TestDriveSchedule existingSchedule = testDriveScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Test drive schedule not found with ID: " + scheduleId));
        
        if (scheduleDetails.getPreferredDate() != null) {
            existingSchedule.setPreferredDate(scheduleDetails.getPreferredDate());
        }
        if (scheduleDetails.getPreferredTime() != null) {
            existingSchedule.setPreferredTime(scheduleDetails.getPreferredTime());
        }
        if (scheduleDetails.getStatus() != null && !scheduleDetails.getStatus().trim().isEmpty()) {
            existingSchedule.setStatus(scheduleDetails.getStatus());
        }
        if (scheduleDetails.getNotes() != null) {
            existingSchedule.setNotes(scheduleDetails.getNotes());
        }
        if (scheduleDetails.getCustomer() != null) {
            existingSchedule.setCustomer(scheduleDetails.getCustomer());
        }
        if (scheduleDetails.getVariant() != null) {
            existingSchedule.setVariant(scheduleDetails.getVariant());
        }
        
        return testDriveScheduleRepository.save(existingSchedule);
    }
    
    public void deleteTestDriveSchedule(UUID scheduleId) {
        if (!testDriveScheduleRepository.existsById(scheduleId)) {
            throw new RuntimeException("Test drive schedule not found with ID: " + scheduleId);
        }
        testDriveScheduleRepository.deleteById(scheduleId);
    }
}

