package com.evdealer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Vehicle brand request DTO for brand management")
public class VehicleBrandRequest {
    
    @Schema(description = "Brand name", example = "Tesla", required = true)
    private String brandName;
    
    @Schema(description = "Country of origin", example = "USA")
    private String country;
    
    @Schema(description = "Founded year", example = "2003")
    private Integer foundedYear;
    
    @Schema(description = "Brand logo URL", example = "/uploads/brands/tesla-logo.png")
    private String brandLogoUrl;
    
    @Schema(description = "Brand logo path", example = "brands/tesla-logo.png")
    private String brandLogoPath;
    
    @Schema(description = "Is active", example = "true")
    private Boolean isActive;
    
    // Constructors
    public VehicleBrandRequest() {}
    
    public VehicleBrandRequest(String brandName, String country) {
        this.brandName = brandName;
        this.country = country;
    }
    
    // Getters and Setters
    public String getBrandName() {
        return brandName;
    }
    
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public Integer getFoundedYear() {
        return foundedYear;
    }
    
    public void setFoundedYear(Integer foundedYear) {
        this.foundedYear = foundedYear;
    }
    
    public String getBrandLogoUrl() {
        return brandLogoUrl;
    }
    
    public void setBrandLogoUrl(String brandLogoUrl) {
        this.brandLogoUrl = brandLogoUrl;
    }
    
    public String getBrandLogoPath() {
        return brandLogoPath;
    }
    
    public void setBrandLogoPath(String brandLogoPath) {
        this.brandLogoPath = brandLogoPath;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
