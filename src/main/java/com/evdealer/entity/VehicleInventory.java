package com.evdealer.entity;

import com.evdealer.enums.VehicleCondition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "vehicle_inventory",
    indexes = {
        @Index(name = "idx_vehicle_inventory_variant", columnList = "variant_id"),
        @Index(name = "idx_vehicle_inventory_color", columnList = "color_id"),
        @Index(name = "idx_vehicle_inventory_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_vehicle_inventory_status", columnList = "status")
    }
)
public class VehicleInventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "inventory_id")
    private UUID inventoryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VehicleVariant variant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VehicleColor color;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Warehouse warehouse;
    
    @Column(name = "warehouse_location", length = 100)
    private String warehouseLocation;
    
    @Column(name = "vin", length = 17, unique = true)
    private String vin;
    
    @Column(name = "chassis_number", length = 50)
    private String chassisNumber;
    
    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;
    
    @Column(name = "arrival_date")
    private LocalDate arrivalDate;
    
    @Column(name = "status", length = 50, nullable = false)
    private String status = "available";
    
    
    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;
    
    @Column(name = "selling_price", precision = 12, scale = 2)
    private BigDecimal sellingPrice;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vehicle_images", columnDefinition = "jsonb")
    private String vehicleImages;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "interior_images", columnDefinition = "jsonb")
    private String interiorImages;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "exterior_images", columnDefinition = "jsonb")
    private String exteriorImages;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserved_for_dealer")
    private Dealer reservedForDealer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserved_for_customer")
    private Customer reservedForCustomer;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "condition", length = 20)
    private VehicleCondition condition = VehicleCondition.NEW;
    
    @Column(name = "reserved_date")
    private LocalDateTime reservedDate;
    
    @Column(name = "reserved_expiry_date")
    private LocalDateTime reservedExpiryDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public VehicleInventory() {}
    
    public VehicleInventory(VehicleVariant variant, VehicleColor color, Warehouse warehouse, String vin) {
        this.variant = variant;
        this.color = color;
        this.warehouse = warehouse;
        this.vin = vin;
    }
    
    // Getters and Setters
    public UUID getInventoryId() {
        return inventoryId;
    }
    
    public void setInventoryId(UUID inventoryId) {
        this.inventoryId = inventoryId;
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
    
    public Warehouse getWarehouse() {
        return warehouse;
    }
    
    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }
    
    public String getWarehouseLocation() {
        return warehouseLocation;
    }
    
    public void setWarehouseLocation(String warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
    }
    
    public String getVin() {
        return vin;
    }
    
    public void setVin(String vin) {
        this.vin = vin;
    }
    
    public String getChassisNumber() {
        return chassisNumber;
    }
    
    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }
    
    public LocalDate getManufacturingDate() {
        return manufacturingDate;
    }
    
    public void setManufacturingDate(LocalDate manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
    }
    
    public LocalDate getArrivalDate() {
        return arrivalDate;
    }
    
    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    
    public BigDecimal getCostPrice() {
        return costPrice;
    }
    
    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }
    
    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }
    
    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
    
    public String getVehicleImages() {
        return vehicleImages;
    }
    
    public void setVehicleImages(String vehicleImages) {
        this.vehicleImages = vehicleImages;
    }
    
    public String getInteriorImages() {
        return interiorImages;
    }
    
    public void setInteriorImages(String interiorImages) {
        this.interiorImages = interiorImages;
    }
    
    public String getExteriorImages() {
        return exteriorImages;
    }
    
    public void setExteriorImages(String exteriorImages) {
        this.exteriorImages = exteriorImages;
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
    
    public Dealer getReservedForDealer() {
        return reservedForDealer;
    }
    
    public void setReservedForDealer(Dealer reservedForDealer) {
        this.reservedForDealer = reservedForDealer;
    }
    
    public Customer getReservedForCustomer() {
        return reservedForCustomer;
    }
    
    public void setReservedForCustomer(Customer reservedForCustomer) {
        this.reservedForCustomer = reservedForCustomer;
    }
    
    public VehicleCondition getCondition() {
        return condition;
    }
    
    public void setCondition(VehicleCondition condition) {
        this.condition = condition;
    }
    
    public LocalDateTime getReservedDate() {
        return reservedDate;
    }
    
    public void setReservedDate(LocalDateTime reservedDate) {
        this.reservedDate = reservedDate;
    }
    
    public LocalDateTime getReservedExpiryDate() {
        return reservedExpiryDate;
    }
    
    public void setReservedExpiryDate(LocalDateTime reservedExpiryDate) {
        this.reservedExpiryDate = reservedExpiryDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleInventory that = (VehicleInventory) o;
        return java.util.Objects.equals(inventoryId, that.inventoryId);
    }

    @Override
    public int hashCode() {
        return inventoryId != null ? inventoryId.hashCode() : 0;
    }
}
