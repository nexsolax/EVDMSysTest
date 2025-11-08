package com.evdealer.entity;

import com.evdealer.enums.DealerPaymentStatus;
import com.evdealer.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dealer_payments")
public class DealerPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items", "quotations", "evmStaff", "dealerOrder"})
    private DealerInvoice invoice;
    
    @Column(name = "payment_number", nullable = false, unique = true, length = 100)
    private String paymentNumber;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 50)
    private PaymentMethod paymentMethod;
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private DealerPaymentStatus status = DealerPaymentStatus.PENDING;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public DealerPayment() {}
    
    public DealerPayment(DealerInvoice invoice, String paymentNumber, LocalDate paymentDate, BigDecimal amount) {
        this.invoice = invoice;
        this.paymentNumber = paymentNumber;
        this.paymentDate = paymentDate;
        this.amount = amount;
    }
    
    // Getters and Setters
    public UUID getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }
    
    public DealerInvoice getInvoice() {
        return invoice;
    }
    
    public void setInvoice(DealerInvoice invoice) {
        this.invoice = invoice;
    }
    
    public String getPaymentNumber() {
        return paymentNumber;
    }
    
    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }
    
    public LocalDate getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    /**
     * Set paymentMethod from String (backward compatibility)
     */
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = PaymentMethod.fromString(paymentMethod);
    }

    /**
     * Get paymentType (backward compatibility - returns enum value)
     * @deprecated Use getPaymentMethod() instead
     */
    @Deprecated
    public String getPaymentType() {
        return paymentMethod != null ? paymentMethod.getValue() : null;
    }

    /**
     * Set paymentType (backward compatibility)
     * @deprecated Use setPaymentMethod() instead
     */
    @Deprecated
    public void setPaymentType(String paymentType) {
        this.paymentMethod = PaymentMethod.fromString(paymentType);
    }
    
    public String getReferenceNumber() {
        return referenceNumber;
    }
    
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    
    public DealerPaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(DealerPaymentStatus status) {
        this.status = status;
    }
    
    /**
     * Set status from String (backward compatibility)
     */
    public void setStatus(String status) {
        this.status = DealerPaymentStatus.fromString(status);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DealerPayment that = (DealerPayment) o;
        return java.util.Objects.equals(paymentId, that.paymentId);
    }

    @Override
    public int hashCode() {
        return paymentId != null ? paymentId.hashCode() : 0;
    }
}

