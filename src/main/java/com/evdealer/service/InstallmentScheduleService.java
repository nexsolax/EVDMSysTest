package com.evdealer.service;

import com.evdealer.entity.InstallmentSchedule;
import com.evdealer.repository.InstallmentScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class InstallmentScheduleService {
    
    @Autowired
    private InstallmentScheduleRepository installmentScheduleRepository;
    
    public List<InstallmentSchedule> getAllInstallmentSchedules() {
        return installmentScheduleRepository.findAll();
    }
    
    public List<InstallmentSchedule> getSchedulesByPlan(UUID planId) {
        return installmentScheduleRepository.findByPlanPlanId(planId);
    }
    
    public List<InstallmentSchedule> getSchedulesByStatus(String status) {
        return installmentScheduleRepository.findByStatus(status);
    }
    
    public List<InstallmentSchedule> getSchedulesByDueDateRange(LocalDate startDate, LocalDate endDate) {
        return installmentScheduleRepository.findByDueDateBetween(startDate, endDate);
    }
    
    public List<InstallmentSchedule> getOverdueSchedules() {
        return installmentScheduleRepository.findOverdueSchedules();
    }
    
    public List<InstallmentSchedule> getDueSoonSchedules() {
        LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
        return installmentScheduleRepository.findDueSoonSchedules(sevenDaysFromNow);
    }
    
    public List<InstallmentSchedule> getSchedulesByInstallmentNumber(Integer installmentNumber) {
        return installmentScheduleRepository.findByInstallmentNumber(installmentNumber);
    }
    
    public List<InstallmentSchedule> getSchedulesByPaidDateRange(LocalDate startDate, LocalDate endDate) {
        return installmentScheduleRepository.findByPaidDateBetween(startDate, endDate);
    }
    
    public Optional<InstallmentSchedule> getScheduleById(UUID scheduleId) {
        return installmentScheduleRepository.findById(scheduleId);
    }
    
    public InstallmentSchedule createInstallmentSchedule(InstallmentSchedule installmentSchedule) {
        return installmentScheduleRepository.save(installmentSchedule);
    }
    
    public InstallmentSchedule updateInstallmentSchedule(UUID scheduleId, InstallmentSchedule installmentScheduleDetails) {
        InstallmentSchedule installmentSchedule = installmentScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Installment schedule not found"));
        
        installmentSchedule.setPlan(installmentScheduleDetails.getPlan());
        installmentSchedule.setInstallmentNumber(installmentScheduleDetails.getInstallmentNumber());
        installmentSchedule.setDueDate(installmentScheduleDetails.getDueDate());
        installmentSchedule.setAmount(installmentScheduleDetails.getAmount());
        installmentSchedule.setPrincipalAmount(installmentScheduleDetails.getPrincipalAmount());
        installmentSchedule.setInterestAmount(installmentScheduleDetails.getInterestAmount());
        installmentSchedule.setStatus(installmentScheduleDetails.getStatus());
        installmentSchedule.setPaidDate(installmentScheduleDetails.getPaidDate());
        installmentSchedule.setPaidAmount(installmentScheduleDetails.getPaidAmount());
        installmentSchedule.setLateFee(installmentScheduleDetails.getLateFee());
        installmentSchedule.setNotes(installmentScheduleDetails.getNotes());
        
        return installmentScheduleRepository.save(installmentSchedule);
    }
    
    public void deleteInstallmentSchedule(UUID scheduleId) {
        InstallmentSchedule schedule = installmentScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Installment schedule not found with id: " + scheduleId));
        installmentScheduleRepository.delete(schedule);
    }
    
    public InstallmentSchedule updateScheduleStatus(UUID scheduleId, String status) {
        InstallmentSchedule installmentSchedule = installmentScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Installment schedule not found"));
        installmentSchedule.setStatus(status);
        return installmentScheduleRepository.save(installmentSchedule);
    }
    
    public InstallmentSchedule markAsPaid(UUID scheduleId, LocalDate paidDate, java.math.BigDecimal paidAmount) {
        InstallmentSchedule installmentSchedule = installmentScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Installment schedule not found"));
        installmentSchedule.setStatus("paid");
        installmentSchedule.setPaidDate(paidDate);
        installmentSchedule.setPaidAmount(paidAmount);
        return installmentScheduleRepository.save(installmentSchedule);
    }
}
