package com.evdealer.entity;

import com.evdealer.enums.VehicleDeliveryStatus;
import com.evdealer.enums.VehicleCondition;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
    name = "vehicle_deliveries",
    indexes = {
        @Index(name = "idx_vehicle_delivery_order", columnList = "order_id"),
        @Index(name = "idx_vehicle_delivery_inventory", columnList = "inventory_id"),
        @Index(name = "idx_vehicle_delivery_customer", columnList = "customer_id"),
        @Index(name = "idx_vehicle_delivery_delivered_by", columnList = "delivered_by"),
        @Index(name = "idx_vehicle_delivery_dealer_order", columnList = "dealer_order_id"),
        @Index(name = "idx_vehicle_delivery_order_item", columnList = "dealer_order_item_id"),
        @Index(name = "idx_vehicle_delivery_date", columnList = "delivery_date"),
        @Index(name = "idx_vehicle_delivery_status", columnList = "delivery_status")
    }
)
public class VehicleDelivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id")
    private UUID deliveryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = true)
    private VehicleInventory inventory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;
    
    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;
    
    @Column(name = "delivery_time")
    private LocalTime deliveryTime;
    
    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;
    
    @Column(name = "delivery_contact_name", length = 100)
    private String deliveryContactName;
    
    @Column(name = "delivery_contact_phone", length = 20)
    private String deliveryContactPhone;
    
    @Convert(converter = com.evdealer.converter.VehicleDeliveryStatusConverter.class)
    @Column(name = "delivery_status", length = 50, nullable = false)
    private VehicleDeliveryStatus deliveryStatus = VehicleDeliveryStatus.SCHEDULED;
    
    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivered_by")
    private User deliveredBy;
    
    @Column(name = "delivery_confirmation_date")
    private LocalDateTime deliveryConfirmationDate;
    
    @Column(name = "customer_signature_url", length = 500)
    private String customerSignatureUrl;
    
    @Column(name = "customer_signature_path", length = 500)
    private String customerSignaturePath;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_order_id")
    private DealerOrder dealerOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_order_item_id")
    private DealerOrderItem dealerOrderItem;
    
    @Column(name = "scheduled_delivery_date")
    private LocalDate scheduledDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "condition", length = 100)
    private VehicleCondition condition;
    
    @Column(name = "is_early_delivery")
    private Boolean isEarlyDelivery = false;
    
    @Column(name = "early_delivery_reason", columnDefinition = "TEXT")
    private String earlyDeliveryReason;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public VehicleDelivery() {}
    
    public VehicleDelivery(Order order, VehicleInventory inventory, Customer customer, LocalDate deliveryDate, String deliveryAddress) {
        this.order = order;
        this.inventory = inventory;
        this.customer = customer;
        this.deliveryDate = deliveryDate;
        this.deliveryAddress = deliveryAddress;
    }
    
    // Constructor for dealer order deliveries (without order_id and inventory_id)
    public VehicleDelivery(DealerOrder dealerOrder, DealerOrderItem dealerOrderItem, Customer customer, LocalDate deliveryDate, String deliveryAddress) {
        this.dealerOrder = dealerOrder;
        this.dealerOrderItem = dealerOrderItem;
        this.customer = customer;
        this.deliveryDate = deliveryDate;
        this.deliveryAddress = deliveryAddress;
    }
    
    // Getters and Setters
    public UUID getDeliveryId() {
        return deliveryId;
    }
    
    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public VehicleInventory getInventory() {
        return inventory;
    }
    
    public void setInventory(VehicleInventory inventory) {
        this.inventory = inventory;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }
    
    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    public LocalTime getDeliveryTime() {
        return deliveryTime;
    }
    
    public void setDeliveryTime(LocalTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public String getDeliveryContactName() {
        return deliveryContactName;
    }
    
    public void setDeliveryContactName(String deliveryContactName) {
        this.deliveryContactName = deliveryContactName;
    }
    
    public String getDeliveryContactPhone() {
        return deliveryContactPhone;
    }
    
    public void setDeliveryContactPhone(String deliveryContactPhone) {
        this.deliveryContactPhone = deliveryContactPhone;
    }
    
    public VehicleDeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }
    
    public void setDeliveryStatus(VehicleDeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
    
    /**
     * Set deliveryStatus from String (backward compatibility)
     */
    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = VehicleDeliveryStatus.fromString(deliveryStatus);
    }
    
    public String getDeliveryNotes() {
        return deliveryNotes;
    }
    
    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }
    
    public User getDeliveredBy() {
        return deliveredBy;
    }
    
    public void setDeliveredBy(User deliveredBy) {
        this.deliveredBy = deliveredBy;
    }
    
    public LocalDateTime getDeliveryConfirmationDate() {
        return deliveryConfirmationDate;
    }
    
    public void setDeliveryConfirmationDate(LocalDateTime deliveryConfirmationDate) {
        this.deliveryConfirmationDate = deliveryConfirmationDate;
    }
    
    public String getCustomerSignatureUrl() {
        return customerSignatureUrl;
    }
    
    public void setCustomerSignatureUrl(String customerSignatureUrl) {
        this.customerSignatureUrl = customerSignatureUrl;
    }
    
    public String getCustomerSignaturePath() {
        return customerSignaturePath;
    }
    
    public void setCustomerSignaturePath(String customerSignaturePath) {
        this.customerSignaturePath = customerSignaturePath;
    }
    
    public DealerOrder getDealerOrder() {
        return dealerOrder;
    }
    
    public void setDealerOrder(DealerOrder dealerOrder) {
        this.dealerOrder = dealerOrder;
    }
    
    public DealerOrderItem getDealerOrderItem() {
        return dealerOrderItem;
    }
    
    public void setDealerOrderItem(DealerOrderItem dealerOrderItem) {
        this.dealerOrderItem = dealerOrderItem;
    }
    
    public LocalDate getScheduledDeliveryDate() {
        return scheduledDeliveryDate;
    }
    
    public void setScheduledDeliveryDate(LocalDate scheduledDeliveryDate) {
        this.scheduledDeliveryDate = scheduledDeliveryDate;
    }
    
    public LocalDate getActualDeliveryDate() {
        return actualDeliveryDate;
    }
    
    public void setActualDeliveryDate(LocalDate actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public VehicleCondition getCondition() {
        return condition;
    }
    
    public void setCondition(VehicleCondition condition) {
        this.condition = condition;
    }
    
    /**
     * Set condition from String (backward compatibility)
     */
    public void setCondition(String condition) {
        this.condition = VehicleCondition.fromString(condition);
    }
    
    public Boolean getIsEarlyDelivery() {
        return isEarlyDelivery;
    }
    
    public void setIsEarlyDelivery(Boolean isEarlyDelivery) {
        this.isEarlyDelivery = isEarlyDelivery;
    }
    
    public String getEarlyDeliveryReason() {
        return earlyDeliveryReason;
    }
    
    public void setEarlyDeliveryReason(String earlyDeliveryReason) {
        this.earlyDeliveryReason = earlyDeliveryReason;
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
        VehicleDelivery that = (VehicleDelivery) o;
        return java.util.Objects.equals(deliveryId, that.deliveryId);
    }

    @Override
    public int hashCode() {
        return deliveryId != null ? deliveryId.hashCode() : 0;
    }
}
