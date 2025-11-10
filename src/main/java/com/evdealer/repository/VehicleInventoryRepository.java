package com.evdealer.repository;

import com.evdealer.entity.VehicleInventory;
import com.evdealer.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleInventoryRepository extends JpaRepository<VehicleInventory, UUID> {
    
    @Query("SELECT DISTINCT vi FROM VehicleInventory vi LEFT JOIN FETCH vi.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH vi.color LEFT JOIN FETCH vi.warehouse")
    List<VehicleInventory> findAllWithDetails();
    
    @Query("SELECT DISTINCT vi FROM VehicleInventory vi LEFT JOIN FETCH vi.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH vi.color LEFT JOIN FETCH vi.warehouse")
    List<VehicleInventory> findAllWithRelationships();
    
    @Query("SELECT DISTINCT vi FROM VehicleInventory vi LEFT JOIN FETCH vi.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH vi.color LEFT JOIN FETCH vi.warehouse WHERE vi.inventoryId = :inventoryId")
    Optional<VehicleInventory> findByIdWithRelationships(@Param("inventoryId") UUID inventoryId);
    
    @Query("SELECT DISTINCT vi FROM VehicleInventory vi LEFT JOIN FETCH vi.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH vi.color LEFT JOIN FETCH vi.warehouse WHERE vi.vin = :vin")
    Optional<VehicleInventory> findByVin(@Param("vin") String vin);
    
    boolean existsByVin(String vin);
    
    @Query("SELECT DISTINCT vi FROM VehicleInventory vi LEFT JOIN FETCH vi.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH vi.color LEFT JOIN FETCH vi.warehouse WHERE vi.status = :status")
    List<VehicleInventory> findByStatus(@Param("status") VehicleStatus status);
    
    @Query("SELECT DISTINCT vi FROM VehicleInventory vi LEFT JOIN FETCH vi.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH vi.color LEFT JOIN FETCH vi.warehouse WHERE vi.variant.variantId = :variantId")
    List<VehicleInventory> findByVariantVariantId(@Param("variantId") Integer variantId);
    
    @Query("SELECT DISTINCT vi FROM VehicleInventory vi LEFT JOIN FETCH vi.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH vi.color LEFT JOIN FETCH vi.warehouse WHERE vi.color.colorId = :colorId")
    List<VehicleInventory> findByColorColorId(@Param("colorId") Integer colorId);
    
    @Query("SELECT DISTINCT vi FROM VehicleInventory vi LEFT JOIN FETCH vi.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH vi.color LEFT JOIN FETCH vi.warehouse WHERE vi.warehouse.warehouseId = :warehouseId")
    List<VehicleInventory> findByWarehouseWarehouseId(@Param("warehouseId") UUID warehouseId);
    
    List<VehicleInventory> findByWarehouseLocation(String warehouseLocation);
    
    @Query("SELECT vi FROM VehicleInventory vi WHERE vi.sellingPrice BETWEEN :minPrice AND :maxPrice")
    List<VehicleInventory> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    // Native query để set null reserved_for_customer (tránh foreign key constraint)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE vehicle_inventory SET reserved_for_customer = NULL WHERE reserved_for_customer = :customerId", nativeQuery = true)
    void clearReservedForCustomer(@Param("customerId") UUID customerId);
    
    @Query("SELECT vi FROM VehicleInventory vi WHERE vi.manufacturingDate BETWEEN :startDate AND :endDate")
    List<VehicleInventory> findByManufacturingDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT vi FROM VehicleInventory vi WHERE vi.arrivalDate BETWEEN :startDate AND :endDate")
    List<VehicleInventory> findByArrivalDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT DISTINCT vi FROM VehicleInventory vi LEFT JOIN FETCH vi.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH vi.color LEFT JOIN FETCH vi.warehouse WHERE vi.vin LIKE %:vin%")
    List<VehicleInventory> findByVinContaining(@Param("vin") String vin);
    
    @Query("SELECT DISTINCT vi FROM VehicleInventory vi LEFT JOIN FETCH vi.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH vi.color LEFT JOIN FETCH vi.warehouse WHERE vi.chassisNumber LIKE %:chassisNumber%")
    List<VehicleInventory> findByChassisNumberContaining(@Param("chassisNumber") String chassisNumber);
    
    // Additional method for dealer order items
    @Query("SELECT vi FROM VehicleInventory vi WHERE vi.variant.variantId = :variantId AND vi.color.colorId = :colorId AND vi.status = :status")
    List<VehicleInventory> findByVariantVariantIdAndColorColorIdAndStatus(@Param("variantId") Integer variantId, @Param("colorId") Integer colorId, @Param("status") VehicleStatus status);
    
    // Lock method for concurrent inventory reservation
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT vi FROM VehicleInventory vi WHERE vi.inventoryId = :id")
    Optional<VehicleInventory> lockById(@Param("id") UUID id);
}
