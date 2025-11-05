package com.evdealer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Vehicle color request DTO for color management")
public class VehicleColorRequest {
    
    @Schema(description = "Color name", example = "Pearl White Multi-Coat", required = true)
    private String colorName;
    
    @Schema(description = "Color code", example = "#FFFFFF")
    private String colorCode;
    
    @Schema(description = "Color swatch URL", example = "/uploads/colors/white-swatch.jpg")
    private String colorSwatchUrl;
    
    @Schema(description = "Color swatch path", example = "colors/white-swatch.jpg")
    private String colorSwatchPath;
    
    @Schema(description = "Is active", example = "true")
    private Boolean isActive;
    
    // Constructors
    public VehicleColorRequest() {}
    
    public VehicleColorRequest(String colorName, String colorCode) {
        this.colorName = colorName;
        this.colorCode = colorCode;
    }
    
    // Getters and Setters
    public String getColorName() {
        return colorName;
    }
    
    public void setColorName(String colorName) {
        this.colorName = colorName;
    }
    
    public String getColorCode() {
        return colorCode;
    }
    
    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }
    
    public String getColorSwatchUrl() {
        return colorSwatchUrl;
    }
    
    public void setColorSwatchUrl(String colorSwatchUrl) {
        this.colorSwatchUrl = colorSwatchUrl;
    }
    
    public String getColorSwatchPath() {
        return colorSwatchPath;
    }
    
    public void setColorSwatchPath(String colorSwatchPath) {
        this.colorSwatchPath = colorSwatchPath;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
