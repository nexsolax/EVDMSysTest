package com.evdealer.dto;

import com.evdealer.enums.OrderType;
import com.evdealer.enums.PaymentStatus;
import com.evdealer.enums.DeliveryStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Order request DTO")
public class OrderRequest {
    
    @Schema(description = "Quotation ID", example = "e913b770-4755-4375-9744-ff97ff827c7a")
    private UUID quotationId;
    
    @Schema(description = "Customer ID", example = "78fe7eb0-ceb8-4793-a8af-187a3fe26f67")
    private UUID customerId;
    
    @Schema(description = "User ID", example = "6f2431b7-10c9-4d61-b612-33e11b923752")
    private UUID userId;
    
    @Schema(description = "Vehicle inventory ID", example = "902ddb7a-b06b-44da-913b-8f13621789a8")
    private UUID inventoryId;
    
    @Schema(description = "Order date", example = "2025-10-23")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;
    
    @Schema(description = "Order type", example = "RETAIL", allowableValues = {"RETAIL", "WHOLESALE", "DEMO", "TEST_DRIVE"})
    private OrderType orderType;
    
    @Schema(description = "Payment status", example = "PENDING", allowableValues = {"PENDING", "PARTIAL", "PAID", "OVERDUE", "REFUNDED"})
    private PaymentStatus paymentStatus;
    
    @Schema(description = "Delivery status", example = "PENDING", allowableValues = {"PENDING", "SCHEDULED", "IN_TRANSIT", "DELIVERED", "CANCELLED"})
    private DeliveryStatus deliveryStatus;
    
    @Schema(description = "Order status", example = "pending", allowableValues = {"pending", "quoted", "confirmed", "paid", "delivered", "completed", "rejected", "cancelled"})
    private String status;
    
    @Schema(description = "Fulfillment status", example = "pending")
    private String fulfillmentStatus;
    
    @Schema(description = "Fulfillment method", example = "direct_shipment")
    private String fulfillmentMethod;
    
    @Schema(description = "Total amount", example = "1200000000")
    private BigDecimal totalAmount;
    
    @Schema(description = "Deposit amount", example = "120000000")
    private BigDecimal depositAmount;
    
    @Schema(description = "Balance amount", example = "1080000000")
    private BigDecimal balanceAmount;
    
    @Schema(description = "Payment method", example = "cash", allowableValues = {"cash", "bank_transfer", "credit_card", "installment"})
    private String paymentMethod;
    
    @Schema(description = "Notes", example = "test")
    private String notes;
    
    @Schema(description = "Delivery date", example = "2025-10-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryDate;
    
    @Schema(description = "Special requests", example = "test")
    private String specialRequests;
    
    // Constructors
    public OrderRequest() {}
    
    // Getters and Setters
    public UUID getQuotationId() {
        return quotationId;
    }
    
    public void setQuotationId(UUID quotationId) {
        this.quotationId = quotationId;
    }
    
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
    
    public UUID getInventoryId() {
        return inventoryId;
    }
    
    public void setInventoryId(UUID inventoryId) {
        this.inventoryId = inventoryId;
    }
    
    public LocalDate getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    
    public OrderType getOrderType() {
        return orderType;
    }
    
    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }
    
    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getFulfillmentStatus() {
        return fulfillmentStatus;
    }
    
    public void setFulfillmentStatus(String fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }
    
    public String getFulfillmentMethod() {
        return fulfillmentMethod;
    }
    
    public void setFulfillmentMethod(String fulfillmentMethod) {
        this.fulfillmentMethod = fulfillmentMethod;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getDepositAmount() {
        return depositAmount;
    }
    
    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }
    
    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }
    
    public void setBalanceAmount(BigDecimal balanceAmount) {
        this.balanceAmount = balanceAmount;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }
    
    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    public String getSpecialRequests() {
        return specialRequests;
    }
    
    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }
}
