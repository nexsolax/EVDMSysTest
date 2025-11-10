package com.evdealer.repository;

import com.evdealer.entity.SalesContract;
import com.evdealer.enums.SalesContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalesContractRepository extends JpaRepository<SalesContract, UUID> {
    
    // Native query để lấy tất cả sales contracts, tránh lỗi khi có foreign key null
    @Query(value = "SELECT * FROM sales_contracts ORDER BY contract_id", nativeQuery = true)
    List<SalesContract> findAllNative();
    
    @Query("SELECT sc FROM SalesContract sc")
    List<SalesContract> findAllWithDetails();
    
    Optional<SalesContract> findByContractNumber(String contractNumber);
    
    @Query("SELECT sc FROM SalesContract sc WHERE sc.order.orderId = :orderId")
    List<SalesContract> findByOrderOrderId(@Param("orderId") UUID orderId);
    
    @Query("SELECT sc FROM SalesContract sc WHERE sc.customer.customerId = :customerId")
    List<SalesContract> findByCustomerCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT sc FROM SalesContract sc WHERE sc.user.userId = :userId")
    List<SalesContract> findByUserUserId(@Param("userId") UUID userId);
    
    List<SalesContract> findByContractStatus(SalesContractStatus contractStatus);
    
    @Query("SELECT sc FROM SalesContract sc WHERE sc.contractDate BETWEEN :startDate AND :endDate")
    List<SalesContract> findByContractDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT sc FROM SalesContract sc WHERE sc.deliveryDate BETWEEN :startDate AND :endDate")
    List<SalesContract> findByDeliveryDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT sc FROM SalesContract sc WHERE sc.customer.customerId = :customerId AND sc.contractStatus = :status")
    List<SalesContract> findByCustomerAndStatus(@Param("customerId") UUID customerId, @Param("status") SalesContractStatus status);
    
    boolean existsByContractNumber(String contractNumber);
}
