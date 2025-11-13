package com.evdealer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class VehicleInventoryDTO {
    private UUID inventoryId;
    private Integer variantId;
    private Integer colorId;
    private UUID warehouseId;
    private String status;
    private String vin;
    private LocalDate arrivalDate;
    private BigDecimal sellingPrice;
    private String vehicleImages;
    private String interiorImages;
    private String exteriorImages;
    // Thông tin variant để frontend có thể lấy variantImageUrl
    private String variantName;
    private String variantImageUrl;
    private BigDecimal priceBase;

    public UUID getInventoryId() { return inventoryId; }
    public void setInventoryId(UUID inventoryId) { this.inventoryId = inventoryId; }
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }
    public Integer getColorId() { return colorId; }
    public void setColorId(Integer colorId) { this.colorId = colorId; }
    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public LocalDate getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate = arrivalDate; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public String getVehicleImages() { return vehicleImages; }
    public void setVehicleImages(String vehicleImages) { this.vehicleImages = vehicleImages; }
    public String getInteriorImages() { return interiorImages; }
    public void setInteriorImages(String interiorImages) { this.interiorImages = interiorImages; }
    public String getExteriorImages() { return exteriorImages; }
    public void setExteriorImages(String exteriorImages) { this.exteriorImages = exteriorImages; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public String getVariantImageUrl() { return variantImageUrl; }
    public void setVariantImageUrl(String variantImageUrl) { this.variantImageUrl = variantImageUrl; }
    public BigDecimal getPriceBase() { return priceBase; }
    public void setPriceBase(BigDecimal priceBase) { this.priceBase = priceBase; }
}


