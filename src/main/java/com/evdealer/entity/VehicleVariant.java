package com.evdealer.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_variants")
public class VehicleVariant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Integer variantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VehicleModel model;
    
    @Column(name = "variant_name", nullable = false, length = 100)
    private String variantName;
    
    @Column(name = "battery_capacity", precision = 8, scale = 2)
    private BigDecimal batteryCapacity;
    
    @Column(name = "range_km")
    private Integer rangeKm;
    
    @Column(name = "power_kw", precision = 8, scale = 2)
    private BigDecimal powerKw;
    
    @Column(name = "acceleration_0_100", precision = 4, scale = 2)
    private BigDecimal acceleration0100;
    
    @Column(name = "top_speed")
    private Integer topSpeed;
    
    @Column(name = "charging_time_fast")
    private Integer chargingTimeFast;
    
    @Column(name = "charging_time_slow")
    private Integer chargingTimeSlow;
    
    @Column(name = "price_base", precision = 15, scale = 2)
    private BigDecimal priceBase;
    
    @Column(name = "variant_image_url", length = 500)
    private String variantImageUrl;
    
    @Column(name = "variant_image_path", length = 500)
    private String variantImagePath;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public VehicleVariant() {}
    
    public VehicleVariant(VehicleModel model, String variantName, BigDecimal priceBase) {
        this.model = model;
        this.variantName = variantName;
        this.priceBase = priceBase;
    }
    
    // Getters and Setters
    public Integer getVariantId() {
        return variantId;
    }
    
    public void setVariantId(Integer variantId) {
        this.variantId = variantId;
    }
    
    public VehicleModel getModel() {
        return model;
    }
    
    public void setModel(VehicleModel model) {
        this.model = model;
    }
    
    public String getVariantName() {
        return variantName;
    }
    
    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }
    
    public BigDecimal getBatteryCapacity() {
        return batteryCapacity;
    }
    
    public void setBatteryCapacity(BigDecimal batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }
    
    public Integer getRangeKm() {
        return rangeKm;
    }
    
    public void setRangeKm(Integer rangeKm) {
        this.rangeKm = rangeKm;
    }
    
    public BigDecimal getPowerKw() {
        return powerKw;
    }
    
    public void setPowerKw(BigDecimal powerKw) {
        this.powerKw = powerKw;
    }
    
    public BigDecimal getAcceleration0100() {
        return acceleration0100;
    }
    
    public void setAcceleration0100(BigDecimal acceleration0100) {
        this.acceleration0100 = acceleration0100;
    }
    
    public Integer getTopSpeed() {
        return topSpeed;
    }
    
    public void setTopSpeed(Integer topSpeed) {
        this.topSpeed = topSpeed;
    }
    
    public Integer getChargingTimeFast() {
        return chargingTimeFast;
    }
    
    public void setChargingTimeFast(Integer chargingTimeFast) {
        this.chargingTimeFast = chargingTimeFast;
    }
    
    public Integer getChargingTimeSlow() {
        return chargingTimeSlow;
    }
    
    public void setChargingTimeSlow(Integer chargingTimeSlow) {
        this.chargingTimeSlow = chargingTimeSlow;
    }
    
    public BigDecimal getPriceBase() {
        return priceBase;
    }
    
    public void setPriceBase(BigDecimal priceBase) {
        this.priceBase = priceBase;
    }
    
    public String getVariantImageUrl() {
        return variantImageUrl;
    }
    
    public void setVariantImageUrl(String variantImageUrl) {
        this.variantImageUrl = variantImageUrl;
    }
    
    public String getVariantImagePath() {
        return variantImagePath;
    }
    
    public void setVariantImagePath(String variantImagePath) {
        this.variantImagePath = variantImagePath;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleVariant that = (VehicleVariant) o;
        return java.util.Objects.equals(variantId, that.variantId);
    }

    @Override
    public int hashCode() {
        return variantId != null ? variantId.hashCode() : 0;
    }
}

