package com.evdealer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class QuotationDTO {
    private UUID quotationId;
    private String quotationNumber;
    private UUID customerId;
    private UUID userId;
    private Integer variantId;
    private Integer colorId;
    private LocalDate quotationDate;
    private BigDecimal totalPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
    private Integer validityDays;
    private LocalDate expiryDate;
    private String status;
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private String notes;

    public UUID getQuotationId() { return quotationId; }
    public void setQuotationId(UUID quotationId) { this.quotationId = quotationId; }
    public String getQuotationNumber() { return quotationNumber; }
    public void setQuotationNumber(String quotationNumber) { this.quotationNumber = quotationNumber; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public Integer getColorId() { return colorId; }
    public void setColorId(Integer colorId) { this.colorId = colorId; }
    public LocalDate getQuotationDate() { return quotationDate; }
    public void setQuotationDate(LocalDate quotationDate) { this.quotationDate = quotationDate; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    
    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

