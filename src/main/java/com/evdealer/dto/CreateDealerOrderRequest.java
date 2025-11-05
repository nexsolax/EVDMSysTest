package com.evdealer.dto;

import com.evdealer.enums.PaymentTerms;
import com.evdealer.enums.DeliveryTerms;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request để tạo đơn hàng đại lý chi tiết")
public class CreateDealerOrderRequest {
    
    @Schema(description = "ID đại lý", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private UUID dealerId;
    
    @Schema(description = "ID nhân viên EVM", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID evmStaffId;
    
    @Schema(description = "Ngày đặt hàng", example = "2024-01-15", required = true)
    private LocalDate orderDate;
    
    @Schema(description = "Ngày giao hàng dự kiến", example = "2024-02-15")
    private LocalDate expectedDeliveryDate;
    
    @Schema(description = "Loại đơn hàng", example = "PURCHASE", allowableValues = {"PURCHASE", "RESERVE", "SAMPLE"})
    private String orderType = "PURCHASE";
    
    @Schema(description = "Độ ưu tiên", example = "NORMAL", allowableValues = {"LOW", "NORMAL", "HIGH", "URGENT"})
    private String priority = "NORMAL";
    
    @Schema(description = "Payment terms", example = "NET_30", allowableValues = {"NET_15", "NET_30", "NET_45", "NET_60", "CASH_ON_DELIVERY", "ADVANCE_PAYMENT"})
    private PaymentTerms paymentTerms;
    
    @Schema(description = "Delivery terms", example = "FOB_FACTORY", allowableValues = {"FOB_FACTORY", "FOB_DESTINATION", "EX_WORKS", "CIF", "DDP"})
    private DeliveryTerms deliveryTerms;
    
    @Schema(description = "Ghi chú đơn hàng")
    private String notes;
    
    @Schema(description = "Danh sách xe trong đơn hàng", required = true)
    private List<DealerOrderItemRequest> items;
    
    // Constructors
    public CreateDealerOrderRequest() {}
    
    // Getters and Setters
    public UUID getDealerId() { return dealerId; }
    public void setDealerId(UUID dealerId) { this.dealerId = dealerId; }
    
    public UUID getEvmStaffId() { return evmStaffId; }
    public void setEvmStaffId(UUID evmStaffId) { this.evmStaffId = evmStaffId; }
    
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
    
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public PaymentTerms getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(PaymentTerms paymentTerms) { this.paymentTerms = paymentTerms; }
    
    public DeliveryTerms getDeliveryTerms() { return deliveryTerms; }
    public void setDeliveryTerms(DeliveryTerms deliveryTerms) { this.deliveryTerms = deliveryTerms; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public List<DealerOrderItemRequest> getItems() { return items; }
    public void setItems(List<DealerOrderItemRequest> items) { this.items = items; }
    
    @Schema(description = "Chi tiết xe trong đơn hàng")
    public static class DealerOrderItemRequest {
        
        @Schema(description = "ID phiên bản xe", example = "1", required = true)
        private Integer variantId;
        
        @Schema(description = "ID màu xe", example = "1", required = true)
        private Integer colorId;
        
        @Schema(description = "Số lượng", example = "5", required = true)
        private Integer quantity;
        
        @Schema(description = "Giá đơn vị", example = "1500000000")
        private BigDecimal unitPrice;
        
        @Schema(description = "Phần trăm giảm giá", example = "5.0")
        private BigDecimal discountPercentage;
        
        @Schema(description = "Ghi chú xe")
        private String notes;
        
        // Constructors
        public DealerOrderItemRequest() {}
        
        // Getters and Setters
        public Integer getVariantId() { return variantId; }
        public void setVariantId(Integer variantId) { this.variantId = variantId; }
        
        public Integer getColorId() { return colorId; }
        public void setColorId(Integer colorId) { this.colorId = colorId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        
        public BigDecimal getDiscountPercentage() { return discountPercentage; }
        public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
