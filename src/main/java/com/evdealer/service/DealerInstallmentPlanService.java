package com.evdealer.service;

import com.evdealer.entity.DealerInstallmentPlan;
import com.evdealer.repository.DealerInstallmentPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DealerInstallmentPlanService {
    
    @Autowired
    private DealerInstallmentPlanRepository dealerInstallmentPlanRepository;
    
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public List<DealerInstallmentPlan> getAllDealerInstallmentPlans() {
        try {
            // Try findAllWithDetails first
            List<DealerInstallmentPlan> plans = dealerInstallmentPlanRepository.findAllWithDetails();
            if (plans != null && !plans.isEmpty()) {
                return plans;
            }
        } catch (Exception e) {
            System.err.println("DealerInstallmentPlanService.getAllDealerInstallmentPlans() - findAllWithDetails failed: " + e.getMessage());
        }
        
        // Fallback to simple findAll
        try {
            return dealerInstallmentPlanRepository.findAll();
        } catch (Exception e) {
            System.err.println("DealerInstallmentPlanService.getAllDealerInstallmentPlans() - findAll failed: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<DealerInstallmentPlan> getDealerInstallmentPlanById(UUID planId) {
        return dealerInstallmentPlanRepository.findById(planId);
    }
    
    @Transactional(readOnly = true)
    public Optional<DealerInstallmentPlan> getDealerInstallmentPlanByContractNumber(String contractNumber) {
        return dealerInstallmentPlanRepository.findByContractNumber(contractNumber);
    }
    
    @Transactional(readOnly = true)
    public List<DealerInstallmentPlan> getDealerInstallmentPlansByInvoice(UUID invoiceId) {
        return dealerInstallmentPlanRepository.findByInvoiceId(invoiceId);
    }
    
    public DealerInstallmentPlan createDealerInstallmentPlan(DealerInstallmentPlan plan) {
        if (plan == null) {
            throw new RuntimeException("Dealer installment plan cannot be null");
        }
        if (plan.getTotalAmount() == null) {
            throw new RuntimeException("Total amount is required");
        }
        if (plan.getDownPaymentAmount() == null) {
            throw new RuntimeException("Down payment amount is required");
        }
        if (plan.getLoanAmount() == null) {
            throw new RuntimeException("Loan amount is required");
        }
        if (plan.getInterestRate() == null) {
            throw new RuntimeException("Interest rate is required");
        }
        if (plan.getLoanTermMonths() == null || plan.getLoanTermMonths() <= 0) {
            throw new RuntimeException("Loan term months must be greater than 0");
        }
        if (plan.getMonthlyPaymentAmount() == null) {
            throw new RuntimeException("Monthly payment amount is required");
        }
        if (plan.getPlanStatus() == null) {
            plan.setPlanStatus(com.evdealer.enums.InstallmentPlanStatus.ACTIVE);
        }
        return dealerInstallmentPlanRepository.save(plan);
    }
    
    public DealerInstallmentPlan updateDealerInstallmentPlan(UUID planId, DealerInstallmentPlan planDetails) {
        DealerInstallmentPlan existingPlan = dealerInstallmentPlanRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Dealer installment plan not found with ID: " + planId));
        
        if (planDetails.getTotalAmount() != null) {
            existingPlan.setTotalAmount(planDetails.getTotalAmount());
        }
        if (planDetails.getDownPaymentAmount() != null) {
            existingPlan.setDownPaymentAmount(planDetails.getDownPaymentAmount());
        }
        if (planDetails.getLoanAmount() != null) {
            existingPlan.setLoanAmount(planDetails.getLoanAmount());
        }
        if (planDetails.getInterestRate() != null) {
            existingPlan.setInterestRate(planDetails.getInterestRate());
        }
        if (planDetails.getLoanTermMonths() != null && planDetails.getLoanTermMonths() > 0) {
            existingPlan.setLoanTermMonths(planDetails.getLoanTermMonths());
        }
        if (planDetails.getMonthlyPaymentAmount() != null) {
            existingPlan.setMonthlyPaymentAmount(planDetails.getMonthlyPaymentAmount());
        }
        if (planDetails.getFirstPaymentDate() != null) {
            existingPlan.setFirstPaymentDate(planDetails.getFirstPaymentDate());
        }
        if (planDetails.getLastPaymentDate() != null) {
            existingPlan.setLastPaymentDate(planDetails.getLastPaymentDate());
        }
        if (planDetails.getPlanStatus() != null) {
            existingPlan.setPlanStatus(planDetails.getPlanStatus());
        }
        if (planDetails.getFinanceCompany() != null) {
            existingPlan.setFinanceCompany(planDetails.getFinanceCompany());
        }
        if (planDetails.getContractNumber() != null) {
            existingPlan.setContractNumber(planDetails.getContractNumber());
        }
        if (planDetails.getInvoice() != null) {
            existingPlan.setInvoice(planDetails.getInvoice());
        }
        
        return dealerInstallmentPlanRepository.save(existingPlan);
    }
    
    public void deleteDealerInstallmentPlan(UUID planId) {
        if (!dealerInstallmentPlanRepository.existsById(planId)) {
            throw new RuntimeException("Dealer installment plan not found with ID: " + planId);
        }
        dealerInstallmentPlanRepository.deleteById(planId);
    }
}

