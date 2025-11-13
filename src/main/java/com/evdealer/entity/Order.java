package com.evdealer.entity;

import com.evdealer.enums.OrderType;
import com.evdealer.enums.PaymentStatus;
import com.evdealer.enums.DeliveryStatus;
import com.evdealer.enums.OrderStatus;
import com.evdealer.enums.PaymentMethod;
import com.evdealer.enums.FulfillmentStatus;
import com.evdealer.converter.OrderStatusConverter;
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
    name = "orders",
    indexes = {
        @Index(name = "idx_orders_customer", columnList = "customer_id"),
        @Index(name = "idx_orders_user", columnList = "user_id"),
        @Index(name = "idx_orders_inventory", columnList = "inventory_id"),
        @Index(name = "idx_orders_order_date", columnList = "order_date"),
        @Index(name = "idx_orders_status", columnList = "status")
    }
)
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID orderId;
    
    @Column(name = "order_number", nullable = false, unique = true, length = 100)
    private String orderNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Quotation quotation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VehicleInventory inventory;
    
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;
    
    @Convert(converter = OrderStatusConverter.class)
    @Column(name = "status", length = 50, nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 20, nullable = false)
    private OrderType orderType = OrderType.RETAIL;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20, nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", length = 20, nullable = false)
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", length = 50)
    private FulfillmentStatus fulfillmentStatus = FulfillmentStatus.PENDING;
    
    @Column(name = "fulfillment_method", length = 50)
    private String fulfillmentMethod;
    
    @Column(name = "fulfillment_reference_id")
    private UUID fulfillmentReferenceId;
    
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "deposit_amount", precision = 15, scale = 2)
    private BigDecimal depositAmount;
    
    @Column(name = "balance_amount", precision = 15, scale = 2)
    private BigDecimal balanceAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "delivery_date")
    private LocalDate deliveryDate;
    
    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Order() {}
    
    public Order(String orderNumber, Customer customer, User user, LocalDate orderDate) {
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.user = user;
        this.orderDate = orderDate;
    }
    
    // Getters and Setters
    public UUID getOrderId() {
        return orderId;
    }
    
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public Quotation getQuotation() {
        return quotation;
    }
    
    public void setQuotation(Quotation quotation) {
        this.quotation = quotation;
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
    
    public VehicleInventory getInventory() {
        return inventory;
    }
    
    public void setInventory(VehicleInventory inventory) {
        this.inventory = inventory;
    }
    
    public LocalDate getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    /**
     * Set status from String (backward compatibility)
     */
    public void setStatus(String status) {
        this.status = OrderStatus.fromString(status);
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
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    /**
     * Set paymentMethod from String (backward compatibility)
     */
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = PaymentMethod.fromString(paymentMethod);
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
    
    public FulfillmentStatus getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(FulfillmentStatus fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }

    /**
     * Set fulfillmentStatus from String (backward compatibility)
     */
    public void setFulfillmentStatus(String fulfillmentStatus) {
        this.fulfillmentStatus = FulfillmentStatus.fromString(fulfillmentStatus);
    }
    
    public String getFulfillmentMethod() {
        return fulfillmentMethod;
    }
    
    public void setFulfillmentMethod(String fulfillmentMethod) {
        this.fulfillmentMethod = fulfillmentMethod;
    }
    
    public UUID getFulfillmentReferenceId() {
        return fulfillmentReferenceId;
    }
    
    public void setFulfillmentReferenceId(UUID fulfillmentReferenceId) {
        this.fulfillmentReferenceId = fulfillmentReferenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order that = (Order) o;
        return java.util.Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return orderId != null ? orderId.hashCode() : 0;
    }
}
