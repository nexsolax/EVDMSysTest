package com.evdealer.entity;

import com.evdealer.enums.PricingPolicyStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pricing_policies")
public class PricingPolicy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "policy_id")
    private UUID policyId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = true)
    private VehicleVariant variant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = true)
    private Dealer dealer;
    
    @Column(name = "policy_name", nullable = false, length = 255)
    private String policyName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "policy_type", length = 50, nullable = false)
    private String policyType = "standard";
    
    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;
    
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "markup_percent", precision = 5, scale = 2)
    private BigDecimal markupPercent;
    
    @Column(name = "markup_amount", precision = 15, scale = 2)
    private BigDecimal markupAmount;
    
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(name = "min_quantity")
    private Integer minQuantity;
    
    @Column(name = "max_quantity")
    private Integer maxQuantity;
    
    @Column(name = "customer_type", length = 50)
    private String customerType;
    
    @Column(name = "region", length = 100)
    private String region;
    
    @Column(name = "scope", length = 50, nullable = false)
    private String scope = "global";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private PricingPolicyStatus status = PricingPolicyStatus.ACTIVE;
    
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public PricingPolicy() {}
    
    public PricingPolicy(String policyName, String policyType, LocalDate effectiveDate) {
        this.policyName = policyName;
        this.policyType = policyType;
        this.effectiveDate = effectiveDate;
    }
    
    // Getters and Setters
    public UUID getPolicyId() {
        return policyId;
    }
    
    public void setPolicyId(UUID policyId) {
        this.policyId = policyId;
    }
    
    public VehicleVariant getVariant() {
        return variant;
    }
    
    public void setVariant(VehicleVariant variant) {
        this.variant = variant;
    }
    
    public Dealer getDealer() {
        return dealer;
    }
    
    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
    }
    
    public String getPolicyName() {
        return policyName;
    }
    
    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPolicyType() {
        return policyType;
    }
    
    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    
    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }
    
    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getMarkupPercent() {
        return markupPercent;
    }
    
    public void setMarkupPercent(BigDecimal markupPercent) {
        this.markupPercent = markupPercent;
    }
    
    public BigDecimal getMarkupAmount() {
        return markupAmount;
    }
    
    public void setMarkupAmount(BigDecimal markupAmount) {
        this.markupAmount = markupAmount;
    }
    
    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }
    
    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public Integer getMinQuantity() {
        return minQuantity;
    }
    
    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }
    
    public Integer getMaxQuantity() {
        return maxQuantity;
    }
    
    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }
    
    public String getCustomerType() {
        return customerType;
    }
    
    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public PricingPolicyStatus getStatus() {
        return status;
    }

    public void setStatus(PricingPolicyStatus status) {
        this.status = status;
    }

    /**
     * Set status from String (backward compatibility)
     */
    public void setStatus(String status) {
        this.status = PricingPolicyStatus.fromString(status);
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
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
        PricingPolicy that = (PricingPolicy) o;
        return java.util.Objects.equals(policyId, that.policyId);
    }

    @Override
    public int hashCode() {
        return policyId != null ? policyId.hashCode() : 0;
    }
}

