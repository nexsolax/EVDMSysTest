package com.evdealer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO để tạo/cập nhật mẫu xe điện (Electric Vehicle Model)")
public class VehicleModelRequest {
    
    @Schema(description = "Brand ID", example = "1", required = true)
    @JsonProperty("brandId")
    private Integer brandId;
    
    // Setter that accepts both Integer and String for brandId
    public void setBrandId(Object brandId) {
        if (brandId == null) {
            this.brandId = null;
        } else if (brandId instanceof Integer) {
            this.brandId = (Integer) brandId;
        } else if (brandId instanceof String) {
            try {
                this.brandId = Integer.parseInt((String) brandId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid brandId format: " + brandId);
            }
        } else {
            throw new IllegalArgumentException("brandId must be Integer or String, got: " + brandId.getClass());
        }
    }
    
    @Schema(description = "Model name", example = "Model 3", required = true)
    private String modelName;
    
    @Schema(description = "Model year", example = "2024")
    @JsonProperty(value = "modelYear", access = JsonProperty.Access.READ_WRITE)
    private Integer modelYear;
    
    @Schema(description = "Year (alternative to modelYear)", example = "2024")
    @JsonProperty("year")
    private Integer year;
    
    @Schema(description = "Loại xe điện (body type)", example = "SEDAN", allowableValues = {"SEDAN", "SUV", "HATCHBACK", "COUPE", "TRUCK", "MPV"}, required = false)
    private String vehicleType;
    
    @Schema(description = "Kiểu dáng thân xe", example = "4-door sedan")
    private String bodyStyle;
    
    @Schema(description = "Số chỗ ngồi", example = "5")
    private Integer seatingCapacity;
    
    @Schema(description = "Mô tả về mẫu xe điện", example = "Mẫu xe điện sedan với công nghệ tự lái và phạm vi hoạt động 468km")
    private String description;
    
    @Schema(description = "Image URL", example = "https://example.com/model3.jpg")
    private String imageUrl;
    
    @Schema(description = "Is active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Notes", example = "Best selling model")
    private String notes;
    
    // Constructors
    public VehicleModelRequest() {}
    
    public VehicleModelRequest(Integer brandId, String modelName, Integer modelYear) {
        this.brandId = brandId;
        this.modelName = modelName;
        this.modelYear = modelYear;
    }
    
    // Getters and Setters
    public Integer getBrandId() {
        return brandId;
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
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    // Helper method to get modelYear (from year if modelYear is null)
    public Integer getEffectiveModelYear() {
        return modelYear != null ? modelYear : year;
    }
    
    public String getVehicleType() {
        return vehicleType;
    }
    
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
    
    public String getBodyStyle() {
        return bodyStyle;
    }
    
    public void setBodyStyle(String bodyStyle) {
        this.bodyStyle = bodyStyle;
    }
    
    public Integer getSeatingCapacity() {
        return seatingCapacity;
    }
    
    public void setSeatingCapacity(Integer seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
}
