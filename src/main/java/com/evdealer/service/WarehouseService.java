package com.evdealer.service;

import com.evdealer.entity.Warehouse;
import com.evdealer.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class WarehouseService {
    
    @Autowired
    private WarehouseRepository warehouseRepository;
    
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }
    
    public List<Warehouse> getActiveWarehouses() {
        return warehouseRepository.findByIsActiveTrue();
    }
    
    public List<Warehouse> getWarehousesByCity(String city) {
        return warehouseRepository.findByCity(city);
    }
    
    public List<Warehouse> getWarehousesByProvince(String province) {
        return warehouseRepository.findByProvince(province);
    }
    
    public Optional<Warehouse> getWarehouseById(UUID warehouseId) {
        return warehouseRepository.findById(warehouseId);
    }
    
    public Optional<Warehouse> getWarehouseByCode(String warehouseCode) {
        return warehouseRepository.findByWarehouseCode(warehouseCode);
    }
    
    public Warehouse createWarehouse(Warehouse warehouse) {
        if (warehouseRepository.existsByWarehouseCode(warehouse.getWarehouseCode())) {
            throw new RuntimeException("Warehouse code already exists");
        }
        return warehouseRepository.save(warehouse);
    }
    
    public Warehouse updateWarehouse(UUID warehouseId, Warehouse warehouseDetails) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));
        
        warehouse.setWarehouseName(warehouseDetails.getWarehouseName());
        warehouse.setWarehouseCode(warehouseDetails.getWarehouseCode());
        warehouse.setAddress(warehouseDetails.getAddress());
        warehouse.setCity(warehouseDetails.getCity());
        warehouse.setProvince(warehouseDetails.getProvince());
        warehouse.setPostalCode(warehouseDetails.getPostalCode());
        warehouse.setPhone(warehouseDetails.getPhone());
        warehouse.setEmail(warehouseDetails.getEmail());
        warehouse.setCapacity(warehouseDetails.getCapacity());
        warehouse.setIsActive(warehouseDetails.getIsActive());
        
        return warehouseRepository.save(warehouse);
    }
    
    public void deleteWarehouse(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + warehouseId));
        warehouseRepository.delete(warehouse);
    }
    
    public Warehouse activateWarehouse(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));
        warehouse.setIsActive(true);
        return warehouseRepository.save(warehouse);
    }
    
    public Warehouse deactivateWarehouse(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));
        warehouse.setIsActive(false);
        return warehouseRepository.save(warehouse);
    }
}
