package com.evdealer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "DTO để tạo/cập nhật phiên bản xe điện (Electric Vehicle Variant)")
public class VehicleVariantRequest {
    
    @Schema(description = "Model ID", example = "1", required = true)
    @JsonProperty("modelId")
    private Integer modelId;
    
    // Setter that accepts both Integer and String for modelId
    public void setModelId(Object modelId) {
        if (modelId == null) {
            this.modelId = null;
        } else if (modelId instanceof Integer) {
            this.modelId = (Integer) modelId;
        } else if (modelId instanceof String) {
            try {
                this.modelId = Integer.parseInt((String) modelId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid modelId format: " + modelId);
            }
        } else {
            throw new IllegalArgumentException("modelId must be Integer or String, got: " + modelId.getClass());
        }
    }
    
    @Schema(description = "Tên phiên bản xe điện", example = "Long Range", required = true)
    private String variantName;
    
    @Schema(description = "Giá bán cơ bản (VND)", example = "1200000000", required = true)
    @JsonProperty("basePrice")
    private BigDecimal basePrice;
    
    @Schema(description = "Loại động cơ (luôn là 'electric' cho xe điện)", example = "electric", defaultValue = "electric")
    private String engineType;
    
    @Schema(description = "Loại hộp số (AUTOMATIC cho xe điện)", example = "AUTOMATIC")
    private String transmission;
    
    @Schema(description = "Loại nhiên liệu (ELECTRIC cho xe điện)", example = "ELECTRIC")
    private String fuelType;
    
    @Schema(description = "Công suất động cơ điện (kW)", example = "283")
    @JsonProperty("powerKw")
    private Integer powerKw;
    
    @Schema(description = "Mô-men xoắn (Nm)", example = "440")
    private Integer torque;
    
    @Schema(description = "Thời gian tăng tốc 0-100 km/h (giây)", example = "6.1")
    @JsonProperty("acceleration0100")
    private BigDecimal acceleration0100;
    
    @Schema(description = "Tốc độ tối đa (km/h)", example = "225")
    private Integer topSpeed;
    
    @Schema(description = "Phạm vi hoạt động (km) - đặc trưng của xe điện", example = "560")
    @JsonProperty("rangeKm")
    private Integer rangeKm;
    
    @Schema(description = "Dung lượng pin (kWh) - đặc trưng của xe điện", example = "75")
    private Integer batteryCapacity;
    
    @Schema(description = "Thời gian sạc nhanh (phút) - đặc trưng của xe điện", example = "30")
    private Integer chargingTimeFast;
    
    @Schema(description = "Thời gian sạc chậm (phút) - đặc trưng của xe điện", example = "600")
    private Integer chargingTimeSlow;
    
    @Schema(description = "Weight (kg)", example = "1847")
    private Integer weight;
    
    @Schema(description = "Length (mm)", example = "4694")
    private Integer length;
    
    @Schema(description = "Width (mm)", example = "1850")
    private Integer width;
    
    @Schema(description = "Height (mm)", example = "1443")
    private Integer height;
    
    @Schema(description = "Wheelbase (mm)", example = "2875")
    private Integer wheelbase;
    
    @Schema(description = "Mô tả về phiên bản xe điện", example = "Phiên bản tầm xa với pin mở rộng 75kWh, phạm vi hoạt động 560km")
    private String description;
    
    @Schema(description = "Is active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Notes", example = "Most popular variant")
    private String notes;
    
    @Schema(description = "Variant image URL", example = "/uploads/variants/model3/image.jpg")
    private String variantImageUrl;
    
    @Schema(description = "Variant image path", example = "variants/model3/image.jpg")
    private String variantImagePath;
    
    // Constructors
    public VehicleVariantRequest() {}
    
    public VehicleVariantRequest(Integer modelId, String variantName, BigDecimal basePrice) {
        this.modelId = modelId;
        this.variantName = variantName;
        this.basePrice = basePrice;
    }
    
    // Helper method to check if required fields are present
    public boolean isValid() {
        return modelId != null && variantName != null && !variantName.trim().isEmpty() && basePrice != null;
    }
    
    // Getters and Setters
    public Integer getModelId() {
        return modelId;
    }
    
    // Removed old setModelId(Integer) - now using setModelId(Object) above
    
    public String getVariantName() {
        return variantName;
    }
    
    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    @JsonProperty("basePrice")
    public void setBasePrice(Object basePrice) {
        if (basePrice == null) {
            this.basePrice = null;
        } else if (basePrice instanceof BigDecimal) {
            this.basePrice = (BigDecimal) basePrice;
        } else if (basePrice instanceof Number) {
            this.basePrice = BigDecimal.valueOf(((Number) basePrice).doubleValue());
        } else if (basePrice instanceof String) {
            try {
                this.basePrice = new BigDecimal((String) basePrice);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid basePrice format: " + basePrice);
            }
        } else {
            throw new IllegalArgumentException("basePrice must be Number or String, got: " + basePrice.getClass());
        }
    }
    
    public String getEngineType() {
        return engineType;
    }
    
    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }
    
    public Integer getPowerKw() {
        return powerKw;
    }
    
    public void setPowerKw(Integer powerKw) {
        this.powerKw = powerKw;
    }
    
    public Integer getTorque() {
        return torque;
    }
    
    public void setTorque(Integer torque) {
        this.torque = torque;
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
    
    public Integer getRangeKm() {
        return rangeKm;
    }
    
    public void setRangeKm(Integer rangeKm) {
        this.rangeKm = rangeKm;
    }
    
    public Integer getBatteryCapacity() {
        return batteryCapacity;
    }
    
    public void setBatteryCapacity(Integer batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
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
    
    public String getTransmission() {
        return transmission;
    }
    
    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }
    
    public String getFuelType() {
        return fuelType;
    }
    
    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }
    
    public Integer getWeight() {
        return weight;
    }
    
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
    
    public Integer getLength() {
        return length;
    }
    
    public void setLength(Integer length) {
        this.length = length;
    }
    
    public Integer getWidth() {
        return width;
    }
    
    public void setWidth(Integer width) {
        this.width = width;
    }
    
    public Integer getHeight() {
        return height;
    }
    
    public void setHeight(Integer height) {
        this.height = height;
    }
    
    public Integer getWheelbase() {
        return wheelbase;
    }
    
    public void setWheelbase(Integer wheelbase) {
        this.wheelbase = wheelbase;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
}
