package com.evdealer.entity;

import com.evdealer.enums.DealerInvoiceStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dealer_invoices")
public class DealerInvoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "invoice_id")
    private UUID invoiceId;
    
    @Column(name = "invoice_number", nullable = false, unique = true, length = 100)
    private String invoiceNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_order_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items", "quotations", "evmStaff"})
    private DealerOrder dealerOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evm_staff_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User evmStaff;
    
    @Column(name = "quotation_id")
    private UUID quotationId; // Reference to DealerQuotation if created from quotation
    
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;
    
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private DealerInvoiceStatus status = DealerInvoiceStatus.ISSUED;
    
    @Column(name = "payment_terms_days")
    private Integer paymentTermsDays = 30;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public DealerInvoice() {}
    
    public DealerInvoice(String invoiceNumber, DealerOrder dealerOrder, User evmStaff, LocalDate invoiceDate, LocalDate dueDate, BigDecimal subtotal, BigDecimal totalAmount) {
        this.invoiceNumber = invoiceNumber;
        this.dealerOrder = dealerOrder;
        this.evmStaff = evmStaff;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.subtotal = subtotal;
        this.totalAmount = totalAmount;
    }
    
    // Getters and Setters
    public UUID getInvoiceId() {
        return invoiceId;
    }
    
    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public DealerOrder getDealerOrder() {
        return dealerOrder;
    }
    
    public void setDealerOrder(DealerOrder dealerOrder) {
        this.dealerOrder = dealerOrder;
    }
    
    public User getEvmStaff() {
        return evmStaff;
    }
    
    public void setEvmStaff(User evmStaff) {
        this.evmStaff = evmStaff;
    }
    
    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }
    
    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public DealerInvoiceStatus getStatus() {
        return status;
    }
    
    public void setStatus(DealerInvoiceStatus status) {
        this.status = status;
    }
    
    /**
     * Set status from String (backward compatibility)
     */
    public void setStatus(String status) {
        this.status = DealerInvoiceStatus.fromString(status);
    }
    
    public Integer getPaymentTermsDays() {
        return paymentTermsDays;
    }
    
    public void setPaymentTermsDays(Integer paymentTermsDays) {
        this.paymentTermsDays = paymentTermsDays;
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
    
    public UUID getQuotationId() {
        return quotationId;
    }
    
    public void setQuotationId(UUID quotationId) {
        this.quotationId = quotationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DealerInvoice that = (DealerInvoice) o;
        return java.util.Objects.equals(invoiceId, that.invoiceId);
    }

    @Override
    public int hashCode() {
        return invoiceId != null ? invoiceId.hashCode() : 0;
    }
}

