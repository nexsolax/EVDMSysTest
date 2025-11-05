package com.evdealer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Quotation request DTO")
public class QuotationRequest {
    
    @Schema(description = "Customer ID", example = "acba6a37-29d9-46f6-8c94-9dfa730d0d89")
    private UUID customerId;
    
    @Schema(description = "User ID", example = "6f2431b7-10c9-4d61-b612-33e11b923752")
    private UUID userId;
    
    @Schema(description = "Vehicle variant ID", example = "1")
    private Integer variantId;
    
    @Schema(description = "Vehicle color ID", example = "1")
    private Integer colorId;
    
    @Schema(description = "Quotation date", example = "2025-10-23")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate quotationDate;
    
    @Schema(description = "Total price", example = "1200000000")
    private BigDecimal totalPrice;
    
    @Schema(description = "Discount amount", example = "0")
    private BigDecimal discountAmount;
    
    @Schema(description = "Final price", example = "1200000000")
    private BigDecimal finalPrice;
    
    @Schema(description = "Validity days", example = "7")
    private Integer validityDays;
    
    @Schema(description = "Status", example = "accepted", allowableValues = {"pending", "accepted", "rejected", "expired"})
    private String status;
    
    @Schema(description = "Notes", example = "test")
    private String notes;
    
    // Constructors
    public QuotationRequest() {}
    
    // Getters and Setters
    public UUID getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public Integer getVariantId() {
        return variantId;
    }
    
    public void setVariantId(Integer variantId) {
        this.variantId = variantId;
    }
    
    public Integer getColorId() {
        return colorId;
    }
    
    public void setColorId(Integer colorId) {
        this.colorId = colorId;
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
}
