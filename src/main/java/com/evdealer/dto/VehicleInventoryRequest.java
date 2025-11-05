package com.evdealer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Vehicle inventory request DTO for inventory management")
public class VehicleInventoryRequest {
    
    @Schema(description = "Variant ID", example = "1", required = true)
    private Integer variantId;
    
    @Schema(description = "Color ID", example = "1", required = true)
    private Integer colorId;
    
    @Schema(description = "Warehouse ID", example = "a69250cf-3bc0-4dc5-934f-f5bebba77444")
    private UUID warehouseId;
    
    @Schema(description = "VIN", example = "1HGBH41JXMN109186", required = true)
    private String vin;
    
    @Schema(description = "Chassis number", example = "CH123456789")
    private String chassisNumber;
    
    @Schema(description = "Manufacturing date", example = "2024-01-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate manufacturingDate;
    
    @Schema(description = "Purchase price (maps to costPrice in entity)", example = "1000000000")
    private BigDecimal purchasePrice;
    
    @Schema(description = "Selling price", example = "1200000000")
    private BigDecimal sellingPrice;
    
    @Schema(description = "Status", example = "available", allowableValues = {"available", "sold", "reserved", "maintenance", "damaged"})
    private String status;
    
    @Schema(description = "Warehouse location (maps to warehouseLocation in entity)", example = "Warehouse A, Bay 1")
    private String location;
    
    // Constructors
    public VehicleInventoryRequest() {}
    
    public VehicleInventoryRequest(Integer variantId, Integer colorId, String vin, String status) {
        this.variantId = variantId;
        this.colorId = colorId;
        this.vin = vin;
        this.status = status;
    }
    
    // Getters and Setters
    public Integer getVariantId() {
        return variantId;
    }
    
    public void setVariantId(Integer variantId) {
        this.variantId = variantId;
    }
    
    public Integer getColorId() {
        return colorId;
    }
    
    public void setColorId(Integer colorId) {
        this.colorId = colorId;
    }
    
    public UUID getWarehouseId() {
        return warehouseId;
    }
    
    public void setWarehouseId(UUID warehouseId) {
        this.warehouseId = warehouseId;
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
    
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
    
    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
    
    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }
    
    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
}
