package com.evdealer.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_models", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"brand_id", "model_name", "model_year"}))
public class VehicleModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Integer modelId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VehicleBrand brand;
    
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;
    
    @Column(name = "model_year", nullable = false)
    private Integer modelYear;
    
    @Column(name = "vehicle_type", length = 50)
    private String vehicleType;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specifications", columnDefinition = "jsonb")
    private String specifications;
    
    @Column(name = "model_image_url", length = 500)
    private String modelImageUrl;
    
    @Column(name = "model_image_path", length = 500)
    private String modelImagePath;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public VehicleModel() {}
    
    public VehicleModel(VehicleBrand brand, String modelName, Integer modelYear, String vehicleType) {
        this.brand = brand;
        this.modelName = modelName;
        this.modelYear = modelYear;
        this.vehicleType = vehicleType;
    }
    
    // Getters and Setters
    public Integer getModelId() {
        return modelId;
    }
    
    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }
    
    public VehicleBrand getBrand() {
        return brand;
    }
    
    public void setBrand(VehicleBrand brand) {
        this.brand = brand;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public Integer getModelYear() {
        return modelYear;
    }
    
    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }
    
    public String getVehicleType() {
        return vehicleType;
    }
    
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSpecifications() {
        return specifications;
    }
    
    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }
    
    public String getModelImageUrl() {
        return modelImageUrl;
    }
    
    public void setModelImageUrl(String modelImageUrl) {
        this.modelImageUrl = modelImageUrl;
    }
    
    public String getModelImagePath() {
        return modelImagePath;
    }
    
    public void setModelImagePath(String modelImagePath) {
        this.modelImagePath = modelImagePath;
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
        VehicleModel that = (VehicleModel) o;
        return java.util.Objects.equals(modelId, that.modelId);
    }

    @Override
    public int hashCode() {
        return modelId != null ? modelId.hashCode() : 0;
    }
}

