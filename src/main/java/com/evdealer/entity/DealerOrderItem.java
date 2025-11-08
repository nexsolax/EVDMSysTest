package com.evdealer.entity;

import com.evdealer.enums.DealerOrderItemStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "dealer_order_items",
    indexes = {
        @Index(name = "idx_dealer_order_items_order", columnList = "dealer_order_id"),
        @Index(name = "idx_dealer_order_items_variant", columnList = "variant_id"),
        @Index(name = "idx_dealer_order_items_color", columnList = "color_id")
    }
)
public class DealerOrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_id")
    private UUID itemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items", "quotations", "dealer", "evmStaff"})
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DealerOrder dealerOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VehicleVariant variant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VehicleColor color;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "final_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private DealerOrderItemStatus status = DealerOrderItemStatus.PENDING;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public DealerOrderItem() {}
    
    public DealerOrderItem(DealerOrder dealerOrder, VehicleVariant variant, VehicleColor color, 
                          Integer quantity, BigDecimal unitPrice) {
        this.dealerOrder = dealerOrder;
        this.variant = variant;
        this.color = color;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.finalPrice = this.totalPrice;
    }
    
    // Helper method to calculate prices
    public void calculatePrices() {
        if (this.unitPrice == null || this.quantity == null) {
            return;
        }
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        if (this.discountPercentage != null && this.discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = this.totalPrice.multiply(
                this.discountPercentage.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP)
            ).setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            this.discountAmount = BigDecimal.ZERO;
        }
        this.finalPrice = this.totalPrice.subtract(this.discountAmount);
    }

    @PrePersist
    @PreUpdate
    private void onPersistOrUpdate() {
        calculatePrices();
    }
    
    // Getters and Setters
    public UUID getItemId() {
        return itemId;
    }
    
    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }
    
    public DealerOrder getDealerOrder() {
        return dealerOrder;
    }
    
    public void setDealerOrder(DealerOrder dealerOrder) {
        this.dealerOrder = dealerOrder;
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
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
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
    
    public DealerOrderItemStatus getStatus() {
        return status;
    }
    
    public void setStatus(DealerOrderItemStatus status) {
        this.status = status;
    }
    
    /**
     * Set status from String (backward compatibility)
     */
    public void setStatus(String status) {
        this.status = DealerOrderItemStatus.fromString(status);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DealerOrderItem that = (DealerOrderItem) o;
        return java.util.Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return itemId != null ? itemId.hashCode() : 0;
    }
}
