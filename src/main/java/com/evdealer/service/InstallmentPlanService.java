package com.evdealer.service;

import com.evdealer.entity.InstallmentPlan;
import com.evdealer.repository.InstallmentPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class InstallmentPlanService {
    
    @Autowired
    private InstallmentPlanRepository installmentPlanRepository;
    
    public List<InstallmentPlan> getAllInstallmentPlans() {
        try {
            return installmentPlanRepository.findAll();
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<InstallmentPlan> getInstallmentPlansByStatus(String status) {
        return installmentPlanRepository.findByPlanStatus(status);
    }
    
    public List<InstallmentPlan> getInstallmentPlansByCustomer(UUID customerId) {
        return installmentPlanRepository.findByCustomerCustomerId(customerId);
    }
    
    public List<InstallmentPlan> getInstallmentPlansByOrder(UUID orderId) {
        return installmentPlanRepository.findByOrderOrderId(orderId);
    }
    
    public List<InstallmentPlan> getInstallmentPlansByFinanceCompany(String financeCompany) {
        return installmentPlanRepository.findByFinanceCompany(financeCompany);
    }
    
    public List<InstallmentPlan> getInstallmentPlansByInvoice(UUID invoiceId) {
        return installmentPlanRepository.findByInvoiceInvoiceId(invoiceId);
    }
    
    public List<InstallmentPlan> getInstallmentPlansByDealer(UUID dealerId) {
        return installmentPlanRepository.findByDealerDealerId(dealerId);
    }
    
    public List<InstallmentPlan> getInstallmentPlansByPlanType(String planType) {
        return installmentPlanRepository.findByPlanType(planType);
    }
    
    public Optional<InstallmentPlan> getInstallmentPlanById(UUID planId) {
        return installmentPlanRepository.findById(planId);
    }
    
    public Optional<InstallmentPlan> getInstallmentPlanByContractNumber(String contractNumber) {
        return installmentPlanRepository.findByContractNumber(contractNumber);
    }
    
    public InstallmentPlan createInstallmentPlan(InstallmentPlan installmentPlan) {
        if (installmentPlan.getContractNumber() != null && 
            installmentPlanRepository.existsByContractNumber(installmentPlan.getContractNumber())) {
            throw new RuntimeException("Contract number already exists");
        }
        return installmentPlanRepository.save(installmentPlan);
    }
    
    public InstallmentPlan updateInstallmentPlan(UUID planId, InstallmentPlan installmentPlanDetails) {
        InstallmentPlan installmentPlan = installmentPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Installment plan not found"));
        
        installmentPlan.setOrder(installmentPlanDetails.getOrder());
        installmentPlan.setCustomer(installmentPlanDetails.getCustomer());
        installmentPlan.setInvoice(installmentPlanDetails.getInvoice());
        installmentPlan.setDealer(installmentPlanDetails.getDealer());
        installmentPlan.setPlanType(installmentPlanDetails.getPlanType());
        installmentPlan.setTotalAmount(installmentPlanDetails.getTotalAmount());
        installmentPlan.setDownPaymentAmount(installmentPlanDetails.getDownPaymentAmount());
        installmentPlan.setLoanAmount(installmentPlanDetails.getLoanAmount());
        installmentPlan.setInterestRate(installmentPlanDetails.getInterestRate());
        installmentPlan.setLoanTermMonths(installmentPlanDetails.getLoanTermMonths());
        installmentPlan.setMonthlyPaymentAmount(installmentPlanDetails.getMonthlyPaymentAmount());
        installmentPlan.setFirstPaymentDate(installmentPlanDetails.getFirstPaymentDate());
        installmentPlan.setLastPaymentDate(installmentPlanDetails.getLastPaymentDate());
        installmentPlan.setPlanStatus(installmentPlanDetails.getPlanStatus());
        installmentPlan.setFinanceCompany(installmentPlanDetails.getFinanceCompany());
        installmentPlan.setContractNumber(installmentPlanDetails.getContractNumber());
        
        return installmentPlanRepository.save(installmentPlan);
    }
    
    public void deleteInstallmentPlan(UUID planId) {
        InstallmentPlan plan = installmentPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Installment plan not found with id: " + planId));
        installmentPlanRepository.delete(plan);
    }
    
    public InstallmentPlan updateInstallmentPlanStatus(UUID planId, String status) {
        InstallmentPlan installmentPlan = installmentPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Installment plan not found"));
        installmentPlan.setPlanStatus(status);
        return installmentPlanRepository.save(installmentPlan);
    }
}
