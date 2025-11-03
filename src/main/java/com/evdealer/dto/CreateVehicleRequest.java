package com.evdealer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Request để tạo xe điện mới hoàn chỉnh trong 1 thao tác (chỉ hỗ trợ xe điện)")
public class CreateVehicleRequest {
    
    // ==================== BRAND INFORMATION ====================
    @Schema(description = "Tên thương hiệu xe", example = "Tesla", required = true)
    private String brandName;
    
    @Schema(description = "Quốc gia của thương hiệu", example = "USA")
    private String brandCountry;
    
    @Schema(description = "Logo thương hiệu (file upload)")
    private MultipartFile brandLogo;
    
    // ==================== MODEL INFORMATION ====================
    @Schema(description = "Tên mẫu xe", example = "Model 3", required = true)
    private String modelName;
    
    @Schema(description = "Loại xe điện (SEDAN, SUV, HATCHBACK, COUPE, TRUCK, MPV)", example = "SEDAN")
    private String vehicleType;
    
    @Schema(description = "Năm sản xuất", example = "2024")
    private Integer modelYear;
    
    @Schema(description = "Mô tả mẫu xe")
    private String modelDescription;
    
    @Schema(description = "Hình ảnh mẫu xe (file upload)")
    private MultipartFile modelImage;
    
    // ==================== VARIANT INFORMATION ====================
    @Schema(description = "Tên phiên bản xe điện", example = "Standard Range Plus", required = true)
    private String variantName;
    
    @Schema(description = "Giá bán cơ bản", example = "1500000000", required = true)
    private BigDecimal priceBase;
    
    @Schema(description = "Dung lượng pin (kWh)", example = "75")
    private Integer batteryCapacity;
    
    @Schema(description = "Tầm hoạt động (km)", example = "468")
    private Integer rangeKm;
    
    @Schema(description = "Công suất động cơ (kW)", example = "283")
    private Integer powerKw;
    
    @Schema(description = "Thời gian tăng tốc 0-100km/h (giây)", example = "5.3")
    private Double acceleration0100;
    
    @Schema(description = "Tốc độ tối đa (km/h)", example = "225")
    private Integer topSpeed;
    
    @Schema(description = "Thời gian sạc nhanh (phút)", example = "30")
    private Integer chargingTimeFast;
    
    @Schema(description = "Thời gian sạc chậm (giờ)", example = "8")
    private Integer chargingTimeSlow;
    
    @Schema(description = "Mô tả phiên bản")
    private String variantDescription;
    
    @Schema(description = "Hình ảnh phiên bản xe (file upload)")
    private MultipartFile variantImage;
    
    // ==================== COLOR INFORMATION ====================
    @Schema(description = "Tên màu xe", example = "Pearl White Multi-Coat", required = true)
    private String colorName;
    
    @Schema(description = "Mã màu", example = "#FFFFFF")
    private String colorCode;
    
    @Schema(description = "Mẫu màu xe (file upload)")
    private MultipartFile colorSwatch;
    
    // ==================== INVENTORY INFORMATION ====================
    @Schema(description = "Số VIN của xe", example = "1HGBH41JXMN109186", required = true)
    private String vin;
    
    @Schema(description = "Số khung xe", example = "CHASSIS123456")
    private String chassisNumber;
    
    @Schema(description = "Giá bán thực tế", example = "1550000000")
    private BigDecimal sellingPrice;
    
    @Schema(description = "Trạng thái xe", example = "AVAILABLE", allowableValues = {"AVAILABLE", "SOLD", "RESERVED", "MAINTENANCE"})
    private String status;
    
    @Schema(description = "Ghi chú về xe")
    private String notes;
    
    // ==================== IMAGES ====================
    @Schema(description = "Hình ảnh chính của xe (file upload)")
    private List<MultipartFile> mainImages;
    
    @Schema(description = "Hình ảnh nội thất (file upload)")
    private List<MultipartFile> interiorImages;
    
    @Schema(description = "Hình ảnh ngoại thất (file upload)")
    private List<MultipartFile> exteriorImages;
    
    // ==================== WAREHOUSE INFORMATION ====================
    @Schema(description = "ID kho chứa xe")
    private String warehouseId;
    
    @Schema(description = "Vị trí trong kho", example = "A-01-15")
    private String warehouseLocation;
    
    // ==================== CONSTRUCTORS ====================
    
    public CreateVehicleRequest() {}
    
    // ==================== GETTERS AND SETTERS ====================
    
    // Brand
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    
    public String getBrandCountry() { return brandCountry; }
    public void setBrandCountry(String brandCountry) { this.brandCountry = brandCountry; }
    
    public MultipartFile getBrandLogo() { return brandLogo; }
    public void setBrandLogo(MultipartFile brandLogo) { this.brandLogo = brandLogo; }
    
    // Model
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    
    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }
    
    public String getModelDescription() { return modelDescription; }
    public void setModelDescription(String modelDescription) { this.modelDescription = modelDescription; }
    
    public MultipartFile getModelImage() { return modelImage; }
    public void setModelImage(MultipartFile modelImage) { this.modelImage = modelImage; }
    
    // Variant
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    
    public BigDecimal getPriceBase() { return priceBase; }
    public void setPriceBase(BigDecimal priceBase) { this.priceBase = priceBase; }
    
    public Integer getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(Integer batteryCapacity) { this.batteryCapacity = batteryCapacity; }
    
    public Integer getRangeKm() { return rangeKm; }
    public void setRangeKm(Integer rangeKm) { this.rangeKm = rangeKm; }
    
    public Integer getPowerKw() { return powerKw; }
    public void setPowerKw(Integer powerKw) { this.powerKw = powerKw; }
    
    public Double getAcceleration0100() { return acceleration0100; }
    public void setAcceleration0100(Double acceleration0100) { this.acceleration0100 = acceleration0100; }
    
    public Integer getTopSpeed() { return topSpeed; }
    public void setTopSpeed(Integer topSpeed) { this.topSpeed = topSpeed; }
    
    public Integer getChargingTimeFast() { return chargingTimeFast; }
    public void setChargingTimeFast(Integer chargingTimeFast) { this.chargingTimeFast = chargingTimeFast; }
    
    public Integer getChargingTimeSlow() { return chargingTimeSlow; }
    public void setChargingTimeSlow(Integer chargingTimeSlow) { this.chargingTimeSlow = chargingTimeSlow; }
    
    public String getVariantDescription() { return variantDescription; }
    public void setVariantDescription(String variantDescription) { this.variantDescription = variantDescription; }
    
    public MultipartFile getVariantImage() { return variantImage; }
    public void setVariantImage(MultipartFile variantImage) { this.variantImage = variantImage; }
    
    // Color
    public String getColorName() { return colorName; }
    public void setColorName(String colorName) { this.colorName = colorName; }
    
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    
    public MultipartFile getColorSwatch() { return colorSwatch; }
    public void setColorSwatch(MultipartFile colorSwatch) { this.colorSwatch = colorSwatch; }
    
    // Inventory
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    
    public String getChassisNumber() { return chassisNumber; }
    public void setChassisNumber(String chassisNumber) { this.chassisNumber = chassisNumber; }
    
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Images
    public List<MultipartFile> getMainImages() { return mainImages; }
    public void setMainImages(List<MultipartFile> mainImages) { this.mainImages = mainImages; }
    
    public List<MultipartFile> getInteriorImages() { return interiorImages; }
    public void setInteriorImages(List<MultipartFile> interiorImages) { this.interiorImages = interiorImages; }
    
    public List<MultipartFile> getExteriorImages() { return exteriorImages; }
    public void setExteriorImages(List<MultipartFile> exteriorImages) { this.exteriorImages = exteriorImages; }
    
    // Warehouse
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    
    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }
}
