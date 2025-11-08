package com.evdealer.repository;

import com.evdealer.entity.Order;
import com.evdealer.enums.OrderStatus;
import com.evdealer.enums.OrderType;
import com.evdealer.enums.PaymentStatus;
import com.evdealer.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.quotation q LEFT JOIN FETCH q.customer LEFT JOIN FETCH q.variant LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.user LEFT JOIN FETCH o.inventory i LEFT JOIN FETCH i.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH i.color")
    List<Order> findAllWithDetails();
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.quotation q LEFT JOIN FETCH q.customer LEFT JOIN FETCH q.variant LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.user LEFT JOIN FETCH o.inventory i LEFT JOIN FETCH i.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH i.color")
    List<Order> findAllWithRelationships();
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    @Query("SELECT o FROM Order o WHERE o.customer.customerId = :customerId")
    List<Order> findByCustomerCustomerId(@Param("customerId") UUID customerId);
    
    
    List<Order> findByOrderType(OrderType orderType);
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);
    List<Order> findByDeliveryStatus(DeliveryStatus deliveryStatus);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatusString(@Param("status") String status);
    
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByOrderDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT o FROM Order o WHERE o.customer.customerId = :customerId AND o.status = :status")
    List<Order> findByCustomerAndStatus(@Param("customerId") UUID customerId, @Param("status") OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId")
    List<Order> findByUserId(@Param("userId") UUID userId);
    
    boolean existsByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.quotation IS NULL AND o.customer IS NOT NULL")
    List<Order> findWalkInOrders();

    @Query("""
        SELECT o FROM Order o
        WHERE o.quotation IS NULL
          AND o.customer IS NOT NULL
          AND (:startDate IS NULL OR o.orderDate >= :startDate)
          AND (:endDate   IS NULL OR o.orderDate <= :endDate)
          AND (:status    IS NULL OR o.status = :status)
    """)
    List<Order> findWalkInOrdersFiltered(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") OrderStatus status);

    @Query("""
        SELECT o FROM Order o
        WHERE o.quotation IS NULL
          AND o.customer IS NOT NULL
          AND (:startDate IS NULL OR o.orderDate >= :startDate)
          AND (:endDate   IS NULL OR o.orderDate <= :endDate)
          AND (:status    IS NULL OR o.status = :status)
    """)
    Page<Order> findWalkInOrdersFiltered(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") OrderStatus status,
            Pageable pageable);
    
    // Tìm Order theo Quotation ID
    @Query("SELECT o FROM Order o WHERE o.quotation.quotationId = :quotationId")
    Optional<Order> findByQuotationQuotationId(@Param("quotationId") UUID quotationId);
}
