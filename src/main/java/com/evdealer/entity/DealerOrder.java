package com.evdealer.entity;

import com.evdealer.enums.PaymentTerms;
import com.evdealer.enums.DeliveryTerms;
import com.evdealer.enums.DealerOrderStatus;
import com.evdealer.enums.DealerOrderType;
import com.evdealer.enums.Priority;
import com.evdealer.enums.ApprovalStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    name = "dealer_orders",
    indexes = {
        @Index(name = "idx_dealer_orders_dealer", columnList = "dealer_id"),
        @Index(name = "idx_dealer_orders_staff", columnList = "evm_staff_id"),
        @Index(name = "idx_dealer_orders_order_date", columnList = "order_date"),
        @Index(name = "idx_dealer_orders_status", columnList = "status")
    }
)
public class DealerOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "dealer_order_id")
    private UUID dealerOrderId;
    
    @Column(name = "dealer_order_number", nullable = false, unique = true, length = 100)
    private String dealerOrderNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Dealer dealer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evm_staff_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "dealer"})
    private User evmStaff;
    
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;
    
    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;
    
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private DealerOrderStatus status = DealerOrderStatus.PENDING;
    
    @Convert(converter = com.evdealer.converter.PriorityConverter.class)
    @Column(name = "priority", length = 20, nullable = false)
    private Priority priority = Priority.NORMAL;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 50, nullable = false)
    private DealerOrderType orderType = DealerOrderType.PURCHASE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 50, nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "approved_by")
    private UUID approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_terms", length = 50)
    private PaymentTerms paymentTerms = PaymentTerms.NET_30;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_terms", length = 50)
    private DeliveryTerms deliveryTerms = DeliveryTerms.FOB_FACTORY;
    
    @Column(name = "discount_applied", precision = 5, scale = 2)
    private java.math.BigDecimal discountApplied = java.math.BigDecimal.ZERO;
    
    @Column(name = "discount_reason", columnDefinition = "TEXT")
    private String discountReason;
    
    @OneToMany(mappedBy = "dealerOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "dealerOrder"})
    @JsonIgnore // Ignore items trong default serialization để tránh lazy loading exception
    private List<DealerOrderItem> items;
    
    @OneToMany(mappedBy = "dealerOrder", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "dealerOrder"})
    @JsonIgnore // Ignore quotations trong default serialization để tránh lazy loading exception
    private List<DealerQuotation> quotations;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public DealerOrder() {}
    
    public DealerOrder(String dealerOrderNumber, Dealer dealer, User evmStaff, LocalDate orderDate, Integer totalQuantity, BigDecimal totalAmount) {
        this.dealerOrderNumber = dealerOrderNumber;
        this.dealer = dealer;
        this.evmStaff = evmStaff;
        this.orderDate = orderDate;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
    }
    
    // Getters and Setters
    public UUID getDealerOrderId() {
        return dealerOrderId;
    }
    
    public void setDealerOrderId(UUID dealerOrderId) {
        this.dealerOrderId = dealerOrderId;
    }
    
    public String getDealerOrderNumber() {
        return dealerOrderNumber;
    }
    
    public void setDealerOrderNumber(String dealerOrderNumber) {
        this.dealerOrderNumber = dealerOrderNumber;
    }
    
    public Dealer getDealer() {
        return dealer;
    }
    
    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
    }
    
    public User getEvmStaff() {
        return evmStaff;
    }
    
    public void setEvmStaff(User evmStaff) {
        this.evmStaff = evmStaff;
    }
    
    public LocalDate getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    
    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }
    
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }
    
    public Integer getTotalQuantity() {
        return totalQuantity;
    }
    
    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public DealerOrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(DealerOrderStatus status) {
        this.status = status;
    }
    
    /**
     * Set status from String (backward compatibility)
     */
    public void setStatus(String status) {
        this.status = DealerOrderStatus.fromString(status);
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    /**
     * Set priority from String (backward compatibility)
     */
    public void setPriority(String priority) {
        this.priority = Priority.fromString(priority);
    }
    
    public DealerOrderType getOrderType() {
        return orderType;
    }
    
    public void setOrderType(DealerOrderType orderType) {
        this.orderType = orderType;
    }
    
    /**
     * Set orderType from String (backward compatibility)
     */
    public void setOrderType(String orderType) {
        this.orderType = DealerOrderType.fromString(orderType);
    }
    
    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }
    
    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    /**
     * Set approvalStatus from String (backward compatibility)
     */
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = ApprovalStatus.fromString(approvalStatus);
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public UUID getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public List<DealerOrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<DealerOrderItem> items) {
        this.items = items;
    }
    
    public List<DealerQuotation> getQuotations() {
        return quotations;
    }
    
    public void setQuotations(List<DealerQuotation> quotations) {
        this.quotations = quotations;
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
    
    public PaymentTerms getPaymentTerms() {
        return paymentTerms;
    }
    
    public void setPaymentTerms(PaymentTerms paymentTerms) {
        this.paymentTerms = paymentTerms;
    }
    
    public DeliveryTerms getDeliveryTerms() {
        return deliveryTerms;
    }
    
    public void setDeliveryTerms(DeliveryTerms deliveryTerms) {
        this.deliveryTerms = deliveryTerms;
    }
    
    public java.math.BigDecimal getDiscountApplied() {
        return discountApplied;
    }
    
    public void setDiscountApplied(java.math.BigDecimal discountApplied) {
        this.discountApplied = discountApplied;
    }
    
    public String getDiscountReason() {
        return discountReason;
    }
    
    public void setDiscountReason(String discountReason) {
        this.discountReason = discountReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DealerOrder that = (DealerOrder) o;
        return java.util.Objects.equals(dealerOrderId, that.dealerOrderId);
    }

    @Override
    public int hashCode() {
        return dealerOrderId != null ? dealerOrderId.hashCode() : 0;
    }
}

