package com.evdealer.entity;

import com.evdealer.enums.DealerQuotationStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "dealer_quotations",
    indexes = {
        @Index(name = "idx_dealer_quotations_dealer", columnList = "dealer_id"),
        @Index(name = "idx_dealer_quotations_order", columnList = "dealer_order_id"),
        @Index(name = "idx_dealer_quotations_status", columnList = "status"),
        @Index(name = "idx_dealer_quotations_date", columnList = "quotation_date")
    }
)
public class DealerQuotation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "quotation_id")
    private UUID quotationId;
    
    @Column(name = "quotation_number", nullable = false, unique = true, length = 100)
    private String quotationNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Dealer dealer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_order_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items", "quotations", "evmStaff"})
    private DealerOrder dealerOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evm_staff_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User evmStaff;
    
    @Column(name = "quotation_date", nullable = false)
    private LocalDate quotationDate;
    
    @Column(name = "validity_days", nullable = false)
    private Integer validityDays = 30;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Convert(converter = com.evdealer.converter.DealerQuotationStatusConverter.class)
    @Column(name = "status", length = 50, nullable = false)
    private DealerQuotationStatus status = DealerQuotationStatus.PENDING;
    
    @Column(name = "payment_terms", length = 100)
    private String paymentTerms; // FULL_PAYMENT, INSTALLMENT, NET_30, NET_60, etc.
    
    @Column(name = "delivery_terms", length = 255)
    private String deliveryTerms;
    
    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "quotation"})
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<DealerQuotationItem> items;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public DealerQuotation() {}
    
    public DealerQuotation(String quotationNumber, Dealer dealer, DealerOrder dealerOrder, LocalDate quotationDate, BigDecimal totalAmount) {
        this.quotationNumber = quotationNumber;
        this.dealer = dealer;
        this.dealerOrder = dealerOrder;
        this.quotationDate = quotationDate;
        this.totalAmount = totalAmount;
        this.subtotal = totalAmount;
    }
    
    // Helper method to calculate expiry date
    @PrePersist
    @PreUpdate
    public void calculateExpiryDate() {
        if (quotationDate != null && validityDays != null) {
            this.expiryDate = quotationDate.plusDays(validityDays);
        }
    }
    
    // Getters and Setters
    public UUID getQuotationId() {
        return quotationId;
    }
    
    public void setQuotationId(UUID quotationId) {
        this.quotationId = quotationId;
    }
    
    public String getQuotationNumber() {
        return quotationNumber;
    }
    
    public void setQuotationNumber(String quotationNumber) {
        this.quotationNumber = quotationNumber;
    }
    
    public Dealer getDealer() {
        return dealer;
    }
    
    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
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
    
    public LocalDate getQuotationDate() {
        return quotationDate;
    }
    
    public void setQuotationDate(LocalDate quotationDate) {
        this.quotationDate = quotationDate;
        calculateExpiryDate();
    }
    
    public Integer getValidityDays() {
        return validityDays;
    }
    
    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
        calculateExpiryDate();
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
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
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public DealerQuotationStatus getStatus() {
        return status;
    }
    
    public void setStatus(DealerQuotationStatus status) {
        this.status = status;
    }
    
    /**
     * Set status from String (backward compatibility)
     */
    public void setStatus(String status) {
        this.status = DealerQuotationStatus.fromString(status);
    }
    
    public String getPaymentTerms() {
        return paymentTerms;
    }
    
    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }
    
    public String getDeliveryTerms() {
        return deliveryTerms;
    }
    
    public void setDeliveryTerms(String deliveryTerms) {
        this.deliveryTerms = deliveryTerms;
    }
    
    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }
    
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }
    
    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }
    
    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
    
    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }
    
    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<DealerQuotationItem> getItems() {
        return items;
    }
    
    public void setItems(List<DealerQuotationItem> items) {
        this.items = items;
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
        DealerQuotation that = (DealerQuotation) o;
        return java.util.Objects.equals(quotationId, that.quotationId);
    }

    @Override
    public int hashCode() {
        return quotationId != null ? quotationId.hashCode() : 0;
    }
}

