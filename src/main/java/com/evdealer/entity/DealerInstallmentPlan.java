package com.evdealer.entity;

import com.evdealer.enums.InstallmentPlanStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dealer_installment_plans")
public class DealerInstallmentPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "plan_id")
    private UUID planId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = true)
    private DealerInvoice invoice;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "down_payment_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal downPaymentAmount;
    
    @Column(name = "loan_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmount;
    
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;
    
    @Column(name = "loan_term_months", nullable = false)
    private Integer loanTermMonths;
    
    @Column(name = "monthly_payment_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPaymentAmount;
    
    @Column(name = "first_payment_date")
    private LocalDate firstPaymentDate;
    
    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_status", length = 50, nullable = false)
    private InstallmentPlanStatus planStatus = InstallmentPlanStatus.ACTIVE;
    
    @Column(name = "finance_company", length = 255)
    private String financeCompany;
    
    @Column(name = "contract_number", length = 100)
    private String contractNumber;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public DealerInstallmentPlan() {}
    
    public DealerInstallmentPlan(DealerInvoice invoice, BigDecimal totalAmount, BigDecimal downPaymentAmount, BigDecimal loanAmount, BigDecimal interestRate, Integer loanTermMonths, BigDecimal monthlyPaymentAmount) {
        this.invoice = invoice;
        this.totalAmount = totalAmount;
        this.downPaymentAmount = downPaymentAmount;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.loanTermMonths = loanTermMonths;
        this.monthlyPaymentAmount = monthlyPaymentAmount;
    }
    
    // Getters and Setters
    public UUID getPlanId() {
        return planId;
    }
    
    public void setPlanId(UUID planId) {
        this.planId = planId;
    }
    
    public DealerInvoice getInvoice() {
        return invoice;
    }
    
    public void setInvoice(DealerInvoice invoice) {
        this.invoice = invoice;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getDownPaymentAmount() {
        return downPaymentAmount;
    }
    
    public void setDownPaymentAmount(BigDecimal downPaymentAmount) {
        this.downPaymentAmount = downPaymentAmount;
    }
    
    public BigDecimal getLoanAmount() {
        return loanAmount;
    }
    
    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }
    
    public BigDecimal getInterestRate() {
        return interestRate;
    }
    
    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }
    
    public Integer getLoanTermMonths() {
        return loanTermMonths;
    }
    
    public void setLoanTermMonths(Integer loanTermMonths) {
        this.loanTermMonths = loanTermMonths;
    }
    
    public BigDecimal getMonthlyPaymentAmount() {
        return monthlyPaymentAmount;
    }
    
    public void setMonthlyPaymentAmount(BigDecimal monthlyPaymentAmount) {
        this.monthlyPaymentAmount = monthlyPaymentAmount;
    }
    
    public LocalDate getFirstPaymentDate() {
        return firstPaymentDate;
    }
    
    public void setFirstPaymentDate(LocalDate firstPaymentDate) {
        this.firstPaymentDate = firstPaymentDate;
    }
    
    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }
    
    public void setLastPaymentDate(LocalDate lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }
    
    public InstallmentPlanStatus getPlanStatus() {
        return planStatus;
    }

    public void setPlanStatus(InstallmentPlanStatus planStatus) {
        this.planStatus = planStatus;
    }

    /**
     * Set planStatus from String (backward compatibility)
     */
    public void setPlanStatus(String planStatus) {
        this.planStatus = InstallmentPlanStatus.fromString(planStatus);
    }
    
    public String getFinanceCompany() {
        return financeCompany;
    }
    
    public void setFinanceCompany(String financeCompany) {
        this.financeCompany = financeCompany;
    }
    
    public String getContractNumber() {
        return contractNumber;
    }
    
    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DealerInstallmentPlan that = (DealerInstallmentPlan) o;
        return java.util.Objects.equals(planId, that.planId);
    }

    @Override
    public int hashCode() {
        return planId != null ? planId.hashCode() : 0;
    }
}

