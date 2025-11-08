package com.evdealer.repository;

import com.evdealer.entity.CustomerPayment;
import com.evdealer.enums.CustomerPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, UUID> {
    
    @Query("SELECT cp FROM CustomerPayment cp")
    List<CustomerPayment> findAllWithDetails();
    
    Optional<CustomerPayment> findByPaymentNumber(String paymentNumber);
    
    boolean existsByPaymentNumber(String paymentNumber);
    
    List<CustomerPayment> findByStatus(CustomerPaymentStatus status);
    
    @Query("SELECT cp FROM CustomerPayment cp WHERE cp.customer.customerId = :customerId")
    List<CustomerPayment> findByCustomerCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT cp FROM CustomerPayment cp WHERE cp.order.orderId = :orderId")
    List<CustomerPayment> findByOrderOrderId(@Param("orderId") UUID orderId);
    
    List<CustomerPayment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<CustomerPayment> findByPaymentType(String paymentType);
    
    List<CustomerPayment> findByPaymentMethod(String paymentMethod);
    
    @Query("SELECT cp FROM CustomerPayment cp WHERE cp.processedBy.userId = :userId")
    List<CustomerPayment> findByProcessedByUserId(@Param("userId") UUID userId);
}