package com.evdealer.service;

import com.evdealer.entity.DealerInstallmentSchedule;
import com.evdealer.entity.DealerInstallmentPlan;
import com.evdealer.repository.DealerInstallmentScheduleRepository;
import com.evdealer.repository.DealerInstallmentPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DealerInstallmentScheduleService {
    
    @Autowired
    private DealerInstallmentScheduleRepository dealerInstallmentScheduleRepository;
    
    @Autowired
    private DealerInstallmentPlanRepository dealerInstallmentPlanRepository;
    
    @Transactional(readOnly = true)
    public List<DealerInstallmentSchedule> getAllSchedules() {
        try {
            return dealerInstallmentScheduleRepository.findAllWithDetails();
        } catch (Exception e) {
            // Fallback to simple findAll if query fails
            return dealerInstallmentScheduleRepository.findAll();
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<DealerInstallmentSchedule> getScheduleById(UUID scheduleId) {
        return dealerInstallmentScheduleRepository.findById(scheduleId);
    }
    
    @Transactional(readOnly = true)
    public List<DealerInstallmentSchedule> getSchedulesByPlanId(UUID planId) {
        return dealerInstallmentScheduleRepository.findByPlanId(planId);
    }
    
    @Transactional(readOnly = true)
    public List<DealerInstallmentSchedule> getSchedulesByStatus(String status) {
        return dealerInstallmentScheduleRepository.findByStatus(status);
    }
    
    public DealerInstallmentSchedule createSchedule(DealerInstallmentSchedule schedule) {
        // Validate plan exists
        if (schedule.getPlan() != null && schedule.getPlan().getPlanId() != null) {
            DealerInstallmentPlan plan = dealerInstallmentPlanRepository.findById(schedule.getPlan().getPlanId())
                .orElseThrow(() -> new RuntimeException("Dealer installment plan not found with ID: " + schedule.getPlan().getPlanId()));
            schedule.setPlan(plan);
        }
        
        return dealerInstallmentScheduleRepository.save(schedule);
    }
    
    public DealerInstallmentSchedule updateSchedule(UUID scheduleId, DealerInstallmentSchedule scheduleDetails) {
        DealerInstallmentSchedule schedule = dealerInstallmentScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + scheduleId));
        
        // Update fields
        if (scheduleDetails.getInstallmentNumber() != null) {
            schedule.setInstallmentNumber(scheduleDetails.getInstallmentNumber());
        }
        if (scheduleDetails.getDueDate() != null) {
            schedule.setDueDate(scheduleDetails.getDueDate());
        }
        if (scheduleDetails.getAmount() != null) {
            schedule.setAmount(scheduleDetails.getAmount());
        }
        if (scheduleDetails.getPrincipalAmount() != null) {
            schedule.setPrincipalAmount(scheduleDetails.getPrincipalAmount());
        }
        if (scheduleDetails.getInterestAmount() != null) {
            schedule.setInterestAmount(scheduleDetails.getInterestAmount());
        }
        if (scheduleDetails.getStatus() != null) {
            schedule.setStatus(scheduleDetails.getStatus());
        }
        if (scheduleDetails.getPaidDate() != null) {
            schedule.setPaidDate(scheduleDetails.getPaidDate());
        }
        if (scheduleDetails.getPaidAmount() != null) {
            schedule.setPaidAmount(scheduleDetails.getPaidAmount());
        }
        if (scheduleDetails.getLateFee() != null) {
            schedule.setLateFee(scheduleDetails.getLateFee());
        }
        if (scheduleDetails.getNotes() != null) {
            schedule.setNotes(scheduleDetails.getNotes());
        }
        if (scheduleDetails.getPlan() != null && scheduleDetails.getPlan().getPlanId() != null) {
            DealerInstallmentPlan plan = dealerInstallmentPlanRepository.findById(scheduleDetails.getPlan().getPlanId())
                .orElseThrow(() -> new RuntimeException("Dealer installment plan not found with ID: " + scheduleDetails.getPlan().getPlanId()));
            schedule.setPlan(plan);
        }
        
        return dealerInstallmentScheduleRepository.save(schedule);
    }
    
    public DealerInstallmentSchedule updateScheduleStatus(UUID scheduleId, String status) {
        DealerInstallmentSchedule schedule = dealerInstallmentScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + scheduleId));
        schedule.setStatus(status);
        return dealerInstallmentScheduleRepository.save(schedule);
    }
    
    public DealerInstallmentSchedule markAsPaid(UUID scheduleId, LocalDate paidDate, java.math.BigDecimal paidAmount) {
        DealerInstallmentSchedule schedule = dealerInstallmentScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + scheduleId));
        schedule.setStatus("paid");
        schedule.setPaidDate(paidDate);
        schedule.setPaidAmount(paidAmount);
        return dealerInstallmentScheduleRepository.save(schedule);
    }
    
    public void deleteSchedule(UUID scheduleId) {
        DealerInstallmentSchedule schedule = dealerInstallmentScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + scheduleId));
        dealerInstallmentScheduleRepository.delete(schedule);
    }
}

