package com.evdealer.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "quotations",
    indexes = {
        @Index(name = "idx_quotations_customer", columnList = "customer_id"),
        @Index(name = "idx_quotations_user", columnList = "user_id"),
        @Index(name = "idx_quotations_variant", columnList = "variant_id"),
        @Index(name = "idx_quotations_color", columnList = "color_id")
    }
)
public class Quotation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "quotation_id")
    private UUID quotationId;
    
    @Column(name = "quotation_number", nullable = false, unique = true, length = 100)
    private String quotationNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VehicleVariant variant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VehicleColor color;
    
    @Column(name = "quotation_date", nullable = false)
    private LocalDate quotationDate;
    
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "final_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalPrice;
    
    @Column(name = "validity_days")
    private Integer validityDays = 7;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(name = "status", length = 50, nullable = false)
    private String status = "pending";
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Quotation() {}
    
    public Quotation(String quotationNumber, Customer customer, User user, VehicleVariant variant, VehicleColor color, LocalDate quotationDate, BigDecimal totalPrice, BigDecimal finalPrice) {
        this.quotationNumber = quotationNumber;
        this.customer = customer;
        this.user = user;
        this.variant = variant;
        this.color = color;
        this.quotationDate = quotationDate;
        this.totalPrice = totalPrice;
        this.finalPrice = finalPrice;
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
    
    public VehicleVariant getVariant() {
        return variant;
    }
    
    public void setVariant(VehicleVariant variant) {
        this.variant = variant;
    }
    
    public VehicleColor getColor() {
        return color;
    }
    
    public void setColor(VehicleColor color) {
        this.color = color;
    }
    
    public LocalDate getQuotationDate() {
        return quotationDate;
    }
    
    public void setQuotationDate(LocalDate quotationDate) {
        this.quotationDate = quotationDate;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getFinalPrice() {
        return finalPrice;
    }
    
    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }
    
    public Integer getValidityDays() {
        return validityDays;
    }
    
    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
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
    
    // Helper method to calculate expiry date
    @PrePersist
    @PreUpdate
    public void calculateExpiryDate() {
        if (quotationDate != null && validityDays != null) {
            this.expiryDate = quotationDate.plusDays(validityDays);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quotation that = (Quotation) o;
        return java.util.Objects.equals(quotationId, that.quotationId);
    }

    @Override
    public int hashCode() {
        return quotationId != null ? quotationId.hashCode() : 0;
    }
}
