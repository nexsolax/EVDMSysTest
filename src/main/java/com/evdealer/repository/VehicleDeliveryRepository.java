package com.evdealer.repository;

import com.evdealer.entity.VehicleDelivery;
import com.evdealer.enums.VehicleDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface VehicleDeliveryRepository extends JpaRepository<VehicleDelivery, UUID> {
    
    // Native query để lấy tất cả deliveries, tránh lỗi khi có foreign key null
    @Query(value = "SELECT * FROM vehicle_deliveries ORDER BY delivery_id", nativeQuery = true)
    List<VehicleDelivery> findAllNative();
    
    @Query("SELECT vd FROM VehicleDelivery vd")
    List<VehicleDelivery> findAllWithDetails();
    
    List<VehicleDelivery> findByOrderOrderId(UUID orderId);
    
    List<VehicleDelivery> findByInventoryInventoryId(UUID inventoryId);
    
    List<VehicleDelivery> findByCustomerCustomerId(UUID customerId);
    
    List<VehicleDelivery> findByDeliveryStatus(VehicleDeliveryStatus deliveryStatus);
    
    @Query("SELECT vd FROM VehicleDelivery vd WHERE vd.deliveryDate = :date")
    List<VehicleDelivery> findByDeliveryDate(@Param("date") LocalDate date);
    
    @Query("SELECT vd FROM VehicleDelivery vd WHERE vd.deliveryDate BETWEEN :startDate AND :endDate")
    List<VehicleDelivery> findByDeliveryDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT vd FROM VehicleDelivery vd WHERE vd.deliveredBy.userId = :userId")
    List<VehicleDelivery> findByDeliveredBy(@Param("userId") UUID userId);
    
    @Query("SELECT vd FROM VehicleDelivery vd WHERE vd.customer.customerId = :customerId AND vd.deliveryStatus = :status")
    List<VehicleDelivery> findByCustomerAndStatus(@Param("customerId") UUID customerId, @Param("status") VehicleDeliveryStatus status);
    
    @Query("SELECT vd FROM VehicleDelivery vd WHERE vd.deliveryDate < :date AND vd.deliveryStatus != :deliveredStatus")
    List<VehicleDelivery> findOverdueDeliveries(@Param("date") LocalDate date, @Param("deliveredStatus") VehicleDeliveryStatus deliveredStatus);
    
    @Query("SELECT vd FROM VehicleDelivery vd WHERE CONCAT(vd.customer.firstName, ' ', vd.customer.lastName) LIKE %:customerName%")
    List<VehicleDelivery> findByCustomerNameContainingIgnoreCase(@Param("customerName") String customerName);
    
    // Additional methods for new APIs
    long countByDeliveryStatus(VehicleDeliveryStatus deliveryStatus);
    
    @Query("SELECT vd FROM VehicleDelivery vd WHERE vd.dealerOrder.dealerOrderId = :dealerOrderId")
    List<VehicleDelivery> findByDealerOrderDealerOrderId(@Param("dealerOrderId") UUID dealerOrderId);
}
