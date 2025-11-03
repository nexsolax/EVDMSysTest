package com.evdealer.service;

import com.evdealer.entity.VehicleInventory;
import com.evdealer.entity.VehicleVariant;
import com.evdealer.entity.VehicleColor;
import com.evdealer.dto.VehicleInventoryRequest;
import com.evdealer.enums.VehicleStatus;
import com.evdealer.repository.VehicleInventoryRepository;
import com.evdealer.repository.VehicleVariantRepository;
import com.evdealer.repository.VehicleColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class VehicleInventoryService {
    
    @Autowired
    private VehicleInventoryRepository vehicleInventoryRepository;
    
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    
    @Autowired
    private VehicleColorRepository vehicleColorRepository;
    
    @Autowired
    private com.evdealer.repository.WarehouseRepository warehouseRepository;
    
    public List<VehicleInventory> getAllVehicleInventory() {
        try {
            // Use JOIN FETCH to eagerly load relationships
            return vehicleInventoryRepository.findAllWithRelationships();
        } catch (Exception e) {
            // Log error and return empty list
            System.err.println("Error fetching vehicle inventory: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    public List<VehicleInventory> getInventoryByStatus(String status) {
        return vehicleInventoryRepository.findByStatus(status);
    }
    
    public List<VehicleInventory> getInventoryByVariant(Integer variantId) {
        return vehicleInventoryRepository.findByVariantVariantId(variantId);
    }
    
    public List<VehicleInventory> getInventoryByColor(Integer colorId) {
        return vehicleInventoryRepository.findByColorColorId(colorId);
    }
    
    public List<VehicleInventory> getInventoryByWarehouse(UUID warehouseId) {
        return vehicleInventoryRepository.findByWarehouseWarehouseId(warehouseId);
    }
    
    public List<VehicleInventory> getInventoryByWarehouseLocation(String warehouseLocation) {
        return vehicleInventoryRepository.findByWarehouseLocation(warehouseLocation);
    }
    
    public List<VehicleInventory> getInventoryByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return vehicleInventoryRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    public List<VehicleInventory> getInventoryByManufacturingDateRange(LocalDate startDate, LocalDate endDate) {
        return vehicleInventoryRepository.findByManufacturingDateRange(startDate, endDate);
    }
    
    public List<VehicleInventory> getInventoryByArrivalDateRange(LocalDate startDate, LocalDate endDate) {
        return vehicleInventoryRepository.findByArrivalDateRange(startDate, endDate);
    }
    
    public List<VehicleInventory> searchByVin(String vin) {
        return vehicleInventoryRepository.findByVinContaining(vin);
    }
    
    public List<VehicleInventory> searchByChassisNumber(String chassisNumber) {
        return vehicleInventoryRepository.findByChassisNumberContaining(chassisNumber);
    }
    
    public Optional<VehicleInventory> getInventoryById(UUID inventoryId) {
        // Use eager loading to fetch warehouse relationship
        return vehicleInventoryRepository.findByIdWithRelationships(inventoryId);
    }
    
    /**
     * Get inventory by ID with all relationships eagerly loaded (alias)
     */
    public Optional<VehicleInventory> getInventoryByIdWithDetails(UUID inventoryId) {
        return getInventoryById(inventoryId);
    }
    
    public Optional<VehicleInventory> getInventoryByVin(String vin) {
        return vehicleInventoryRepository.findByVin(vin);
    }
    
    public VehicleInventory createVehicleInventory(VehicleInventory vehicleInventory) {
        if (vehicleInventory.getVin() != null && 
            vehicleInventoryRepository.existsByVin(vehicleInventory.getVin())) {
            throw new RuntimeException("VIN already exists");
        }
        return vehicleInventoryRepository.save(vehicleInventory);
    }
    
    public VehicleInventory createVehicleInventoryFromRequest(VehicleInventoryRequest request) {
        // Validate required fields
        if (request.getVariantId() == null) {
            throw new RuntimeException("Variant ID is required");
        }
        if (request.getColorId() == null) {
            throw new RuntimeException("Color ID is required");
        }
        if (request.getVin() == null || request.getVin().trim().isEmpty()) {
            throw new RuntimeException("VIN is required");
        }
        
        // Check for duplicate VIN
        if (vehicleInventoryRepository.existsByVin(request.getVin())) {
            throw new RuntimeException("VIN already exists: " + request.getVin());
        }
        
        // Get VehicleVariant
        VehicleVariant variant = vehicleVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Variant not found with id: " + request.getVariantId()));
        
        // Get VehicleColor
        VehicleColor color = vehicleColorRepository.findById(request.getColorId())
                .orElseThrow(() -> new RuntimeException("Color not found with id: " + request.getColorId()));
        
        // Get Warehouse if provided
        com.evdealer.entity.Warehouse warehouse = null;
        if (request.getWarehouseId() != null) {
            warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + request.getWarehouseId()));
        }
        
        // Create VehicleInventory entity
        VehicleInventory inventory = new VehicleInventory();
        inventory.setVariant(variant);
        inventory.setColor(color);
        inventory.setWarehouse(warehouse);  // Set warehouse if provided
        inventory.setVin(request.getVin().trim());
        inventory.setChassisNumber(request.getChassisNumber());
        inventory.setManufacturingDate(request.getManufacturingDate());
        inventory.setWarehouseLocation(request.getLocation());
        
        // Set prices
        if (request.getPurchasePrice() != null) {
            inventory.setCostPrice(request.getPurchasePrice());
        }
        if (request.getSellingPrice() != null) {
            inventory.setSellingPrice(request.getSellingPrice());
        }
        
        // Normalize and set status
        if (request.getStatus() != null) {
            VehicleStatus normalizedStatus = VehicleStatus.fromString(request.getStatus());
            inventory.setStatus(normalizedStatus.getValue());
        } else {
            inventory.setStatus(VehicleStatus.AVAILABLE.getValue());
        }
        
        return vehicleInventoryRepository.save(inventory);
    }
    
    public VehicleInventory updateVehicleInventory(UUID inventoryId, VehicleInventory vehicleInventoryDetails) {
        VehicleInventory vehicleInventory = vehicleInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Vehicle inventory not found"));
        
        vehicleInventory.setVariant(vehicleInventoryDetails.getVariant());
        vehicleInventory.setColor(vehicleInventoryDetails.getColor());
        vehicleInventory.setWarehouse(vehicleInventoryDetails.getWarehouse());
        vehicleInventory.setWarehouseLocation(vehicleInventoryDetails.getWarehouseLocation());
        vehicleInventory.setVin(vehicleInventoryDetails.getVin());
        vehicleInventory.setChassisNumber(vehicleInventoryDetails.getChassisNumber());
        vehicleInventory.setManufacturingDate(vehicleInventoryDetails.getManufacturingDate());
        vehicleInventory.setArrivalDate(vehicleInventoryDetails.getArrivalDate());
        vehicleInventory.setStatus(vehicleInventoryDetails.getStatus());
        vehicleInventory.setCostPrice(vehicleInventoryDetails.getCostPrice());
        vehicleInventory.setSellingPrice(vehicleInventoryDetails.getSellingPrice());
        vehicleInventory.setVehicleImages(vehicleInventoryDetails.getVehicleImages());
        vehicleInventory.setInteriorImages(vehicleInventoryDetails.getInteriorImages());
        vehicleInventory.setExteriorImages(vehicleInventoryDetails.getExteriorImages());
        
        return vehicleInventoryRepository.save(vehicleInventory);
    }
    
    public VehicleInventory updateVehicleInventoryFromRequest(UUID inventoryId, VehicleInventoryRequest request) {
        VehicleInventory inventory = vehicleInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Vehicle inventory not found"));
        
        // Update variant if provided
        if (request.getVariantId() != null) {
            VehicleVariant variant = vehicleVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found with id: " + request.getVariantId()));
            inventory.setVariant(variant);
        }
        
        // Update color if provided
        if (request.getColorId() != null) {
            VehicleColor color = vehicleColorRepository.findById(request.getColorId())
                    .orElseThrow(() -> new RuntimeException("Color not found with id: " + request.getColorId()));
            inventory.setColor(color);
        }
        
        // Update warehouse if provided
        // Note: If warehouseId is null in request, we don't update warehouse (keep existing)
        // To clear warehouse, frontend should send a special flag or use a separate endpoint
        if (request.getWarehouseId() != null) {
            com.evdealer.entity.Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + request.getWarehouseId()));
            inventory.setWarehouse(warehouse);
        }
        // If warehouseId is not provided in request, warehouse remains unchanged
        
        // Update VIN if provided (check for duplicates)
        if (request.getVin() != null && !request.getVin().trim().isEmpty()) {
            if (!inventory.getVin().equals(request.getVin()) && 
                vehicleInventoryRepository.existsByVin(request.getVin())) {
                throw new RuntimeException("VIN already exists: " + request.getVin());
            }
            inventory.setVin(request.getVin().trim());
        }
        
        // Update other fields
        if (request.getChassisNumber() != null) {
            inventory.setChassisNumber(request.getChassisNumber());
        }
        if (request.getManufacturingDate() != null) {
            inventory.setManufacturingDate(request.getManufacturingDate());
        }
        if (request.getLocation() != null) {
            inventory.setWarehouseLocation(request.getLocation());
        }
        if (request.getPurchasePrice() != null) {
            inventory.setCostPrice(request.getPurchasePrice());
        }
        if (request.getSellingPrice() != null) {
            inventory.setSellingPrice(request.getSellingPrice());
        }
        if (request.getStatus() != null) {
            VehicleStatus normalizedStatus = VehicleStatus.fromString(request.getStatus());
            inventory.setStatus(normalizedStatus.getValue());
        }
        
        return vehicleInventoryRepository.save(inventory);
    }
    
    public void deleteVehicleInventory(UUID inventoryId) {
        if (!vehicleInventoryRepository.existsById(inventoryId)) {
            throw new RuntimeException("Vehicle inventory not found");
        }
        vehicleInventoryRepository.deleteById(inventoryId);
    }
    
    public VehicleInventory updateInventoryStatus(UUID inventoryId, String status) {
        VehicleInventory vehicleInventory = vehicleInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Vehicle inventory not found"));
        
        // Normalize status using enum
        VehicleStatus normalizedStatus = VehicleStatus.fromString(status);
        vehicleInventory.setStatus(normalizedStatus.getValue());
        
        return vehicleInventoryRepository.save(vehicleInventory);
    }
    
    public VehicleInventory createInventory(VehicleInventory inventory) {
        if (vehicleInventoryRepository.existsByVin(inventory.getVin())) {
            throw new RuntimeException("Vehicle with VIN already exists: " + inventory.getVin());
        }
        
        // Normalize status before saving
        if (inventory.getStatus() != null) {
            VehicleStatus normalizedStatus = VehicleStatus.fromString(inventory.getStatus());
            inventory.setStatus(normalizedStatus.getValue());
        } else {
            inventory.setStatus(VehicleStatus.AVAILABLE.getValue());
        }
        
        return vehicleInventoryRepository.save(inventory);
    }
    
    /**
     * Normalize all existing status values in the database
     * This method should be called once to fix existing data
     */
    public int normalizeAllStatuses() {
        List<VehicleInventory> allInventory = vehicleInventoryRepository.findAll();
        int updatedCount = 0;
        
        for (VehicleInventory inventory : allInventory) {
            String currentStatus = inventory.getStatus();
            if (currentStatus != null) {
                VehicleStatus normalizedStatus = VehicleStatus.fromString(currentStatus);
                if (!normalizedStatus.getValue().equals(currentStatus)) {
                    inventory.setStatus(normalizedStatus.getValue());
                    vehicleInventoryRepository.save(inventory);
                    updatedCount++;
                }
            }
        }
        
        return updatedCount;
    }
    
    /**
     * Get all available status values with descriptions
     */
    public java.util.Map<String, String> getAllStatusOptions() {
        return VehicleStatus.getAllWithDescriptions();
    }
    
    /**
     * Validate status value
     */
    public boolean isValidStatus(String status) {
        return VehicleStatus.isValid(status);
    }
}
