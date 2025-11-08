package com.evdealer.entity;

import com.evdealer.enums.SalesContractStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "sales_contracts",
    indexes = {
        @Index(name = "idx_sales_contracts_order", columnList = "order_id"),
        @Index(name = "idx_sales_contracts_customer", columnList = "customer_id"),
        @Index(name = "idx_sales_contracts_user", columnList = "user_id"),
        @Index(name = "idx_sales_contracts_contract_date", columnList = "contract_date"),
        @Index(name = "idx_sales_contracts_status", columnList = "contract_status")
    }
)
public class SalesContract {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "contract_id")
    private UUID contractId;
    
    @Column(name = "contract_number", nullable = false, unique = true, length = 100)
    private String contractNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    
    @Column(name = "contract_date", nullable = false)
    private LocalDate contractDate;
    
    @Column(name = "delivery_date")
    private LocalDate deliveryDate;
    
    @Column(name = "contract_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal contractValue;
    
    @Column(name = "payment_terms", columnDefinition = "TEXT")
    private String paymentTerms;
    
    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths = 24;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", length = 50, nullable = false)
    private SalesContractStatus contractStatus = SalesContractStatus.DRAFT;
    
    @Column(name = "signed_date")
    private LocalDate signedDate;
    
    @Column(name = "contract_file_url", length = 500)
    private String contractFileUrl;
    
    @Column(name = "contract_file_path", length = 500)
    private String contractFilePath;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public SalesContract() {}
    
    public SalesContract(String contractNumber, Order order, Customer customer, User user, LocalDate contractDate, BigDecimal contractValue) {
        this.contractNumber = contractNumber;
        this.order = order;
        this.customer = customer;
        this.user = user;
        this.contractDate = contractDate;
        this.contractValue = contractValue;
    }
    
    // Getters and Setters
    public UUID getContractId() {
        return contractId;
    }
    
    public void setContractId(UUID contractId) {
        this.contractId = contractId;
    }
    
    public String getContractNumber() {
        return contractNumber;
    }
    
    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDate getContractDate() {
        return contractDate;
    }
    
    public void setContractDate(LocalDate contractDate) {
        this.contractDate = contractDate;
    }
    
    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }
    
    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    public BigDecimal getContractValue() {
        return contractValue;
    }
    
    public void setContractValue(BigDecimal contractValue) {
        this.contractValue = contractValue;
    }
    
    public String getPaymentTerms() {
        return paymentTerms;
    }
    
    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }
    
    public Integer getWarrantyPeriodMonths() {
        return warrantyPeriodMonths;
    }
    
    public void setWarrantyPeriodMonths(Integer warrantyPeriodMonths) {
        this.warrantyPeriodMonths = warrantyPeriodMonths;
    }
    
    public SalesContractStatus getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(SalesContractStatus contractStatus) {
        this.contractStatus = contractStatus;
    }

    /**
     * Set contractStatus from String (backward compatibility)
     */
    public void setContractStatus(String contractStatus) {
        this.contractStatus = SalesContractStatus.fromString(contractStatus);
    }
    
    public LocalDate getSignedDate() {
        return signedDate;
    }
    
    public void setSignedDate(LocalDate signedDate) {
        this.signedDate = signedDate;
    }
    
    public String getContractFileUrl() {
        return contractFileUrl;
    }
    
    public void setContractFileUrl(String contractFileUrl) {
        this.contractFileUrl = contractFileUrl;
    }
    
    public String getContractFilePath() {
        return contractFilePath;
    }
    
    public void setContractFilePath(String contractFilePath) {
        this.contractFilePath = contractFilePath;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesContract that = (SalesContract) o;
        return java.util.Objects.equals(contractId, that.contractId);
    }

    @Override
    public int hashCode() {
        return contractId != null ? contractId.hashCode() : 0;
    }
}
