package com.evdealer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Pricing policy request DTO for pricing management")
public class PricingPolicyRequest {
    
    @Schema(description = "Variant ID", example = "1", required = true)
    private Integer variantId;
    
    @Schema(description = "Dealer ID", example = "78fe7eb0-ceb8-4793-a8af-187a3fe26f67")
    private UUID dealerId;
    
    @Schema(description = "Policy name", example = "Q1 2024 Promotion", required = true)
    private String policyName;
    
    @Schema(description = "Description", example = "Special pricing for Q1 2024")
    private String description;
    
    @Schema(description = "Policy type", example = "discount", allowableValues = {"discount", "markup", "fixed", "percentage"})
    private String policyType;
    
    @Schema(description = "Scope", example = "variant", allowableValues = {"variant", "model", "brand", "dealer", "global"})
    private String scope;
    
    @Schema(description = "Base price", example = "1200000000")
    private BigDecimal basePrice;
    
    @Schema(description = "Discount percentage", example = "10.0")
    private BigDecimal discountPercent;
    
    @Schema(description = "Discount amount", example = "120000000")
    private BigDecimal discountAmount;
    
    @Schema(description = "Markup percentage", example = "5.0")
    private BigDecimal markupPercent;
    
    @Schema(description = "Markup amount", example = "60000000")
    private BigDecimal markupAmount;
    
    @Schema(description = "Effective date", example = "2024-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;
    
    @Schema(description = "Expiry date", example = "2024-03-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    @Schema(description = "Minimum quantity", example = "1")
    private Integer minQuantity;
    
    @Schema(description = "Maximum quantity", example = "10")
    private Integer maxQuantity;
    
    @Schema(description = "Customer type", example = "retail")
    private String customerType;
    
    @Schema(description = "Region", example = "North")
    private String region;
    
    @Schema(description = "Status", example = "active", allowableValues = {"active", "inactive", "expired"}, defaultValue = "active")
    private String status;
    
    @Schema(description = "Priority", example = "0", defaultValue = "0")
    private Integer priority;
    
    // Constructors
    public PricingPolicyRequest() {}
    
    public PricingPolicyRequest(Integer variantId, String policyName, String policyType) {
        this.variantId = variantId;
        this.policyName = policyName;
        this.policyType = policyType;
    }
    
    // Getters and Setters
    public Integer getVariantId() {
        return variantId;
    }
    
    public void setVariantId(Integer variantId) {
        this.variantId = variantId;
    }
    
    public UUID getDealerId() {
        return dealerId;
    }
    
    public void setDealerId(UUID dealerId) {
        this.dealerId = dealerId;
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
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
