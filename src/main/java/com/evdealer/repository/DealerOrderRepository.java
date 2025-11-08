package com.evdealer.repository;

import com.evdealer.entity.DealerOrder;
import com.evdealer.enums.ApprovalStatus;
import com.evdealer.enums.DealerOrderStatus;
import com.evdealer.enums.DealerOrderType;
import com.evdealer.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DealerOrderRepository extends JpaRepository<DealerOrder, UUID> {
    
    @Query("SELECT do FROM DealerOrder do LEFT JOIN FETCH do.dealer")
    List<DealerOrder> findAllWithDetails();
    
    Optional<DealerOrder> findByDealerOrderNumber(String dealerOrderNumber);
    
    @Query("SELECT do FROM DealerOrder do LEFT JOIN FETCH do.dealer d LEFT JOIN FETCH do.evmStaff e WHERE do.dealerOrderId = :dealerOrderId")
    Optional<DealerOrder> findByIdWithDetails(@Param("dealerOrderId") UUID dealerOrderId);
    
    // Query to get dealer_id directly from dealer_orders table
    @Query(value = "SELECT dealer_id FROM dealer_orders WHERE dealer_order_id = :dealerOrderId", nativeQuery = true)
    Optional<UUID> findDealerIdByOrderId(@Param("dealerOrderId") UUID dealerOrderId);
    
    @Query("SELECT do FROM DealerOrder do WHERE do.evmStaff.userId = :evmStaffId")
    List<DealerOrder> findByEvmStaffUserId(@Param("evmStaffId") UUID evmStaffId);
    
    @Query("SELECT do FROM DealerOrder do LEFT JOIN FETCH do.dealer WHERE do.status = :status")
    List<DealerOrder> findByStatus(@Param("status") DealerOrderStatus status);
    
    @Query("SELECT do FROM DealerOrder do WHERE do.orderDate BETWEEN :startDate AND :endDate")
    List<DealerOrder> findByOrderDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT do FROM DealerOrder do WHERE do.evmStaff.userId = :evmStaffId AND do.status = :status")
    List<DealerOrder> findByEvmStaffAndStatus(@Param("evmStaffId") UUID evmStaffId, @Param("status") DealerOrderStatus status);
    
    boolean existsByDealerOrderNumber(String dealerOrderNumber);
    
    // New methods for improved dealer order management
    @Query("SELECT do FROM DealerOrder do LEFT JOIN FETCH do.dealer WHERE do.approvalStatus = :approvalStatus")
    List<DealerOrder> findByApprovalStatus(@Param("approvalStatus") ApprovalStatus approvalStatus);
    
    @Query("SELECT do FROM DealerOrder do LEFT JOIN FETCH do.dealer WHERE do.dealer.dealerId = :dealerId")
    List<DealerOrder> findByDealerId(@Param("dealerId") UUID dealerId);
    
    @Query("SELECT do FROM DealerOrder do WHERE do.dealer.dealerId = :dealerId AND do.approvalStatus = :approvalStatus")
    List<DealerOrder> findByDealerIdAndApprovalStatus(@Param("dealerId") UUID dealerId, @Param("approvalStatus") ApprovalStatus approvalStatus);
    
    @Query("SELECT do FROM DealerOrder do WHERE do.orderType = :orderType")
    List<DealerOrder> findByOrderType(@Param("orderType") DealerOrderType orderType);
    
    @Query("SELECT do FROM DealerOrder do WHERE do.priority = :priority")
    List<DealerOrder> findByPriority(@Param("priority") Priority priority);
    
    @Query("SELECT do FROM DealerOrder do WHERE do.approvalStatus = :approvalStatus AND do.orderDate <= :cutoffDate")
    List<DealerOrder> findOverduePendingOrders(@Param("cutoffDate") LocalDate cutoffDate, @Param("approvalStatus") ApprovalStatus approvalStatus);
    
    @Query("SELECT COUNT(do) FROM DealerOrder do WHERE do.approvalStatus = :approvalStatus")
    Long countByApprovalStatus(@Param("approvalStatus") ApprovalStatus approvalStatus);
    
    @Query("SELECT SUM(do.totalAmount) FROM DealerOrder do WHERE do.approvalStatus = :approvalStatus AND do.orderDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumApprovedAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("approvalStatus") ApprovalStatus approvalStatus);
}

