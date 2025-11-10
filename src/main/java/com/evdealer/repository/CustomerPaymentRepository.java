package com.evdealer.repository;

import com.evdealer.entity.CustomerPayment;
import com.evdealer.enums.CustomerPaymentStatus;
import com.evdealer.enums.PaymentMethod;
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
    
    // Native query để lấy tất cả customer payments, tránh lỗi khi có foreign key null
    @Query(value = "SELECT * FROM customer_payments ORDER BY payment_id", nativeQuery = true)
    List<CustomerPayment> findAllNative();
    
    Optional<CustomerPayment> findByPaymentNumber(String paymentNumber);
    
    boolean existsByPaymentNumber(String paymentNumber);
    
    List<CustomerPayment> findByStatus(CustomerPaymentStatus status);
    
    @Query("SELECT cp FROM CustomerPayment cp WHERE cp.customer.customerId = :customerId")
    List<CustomerPayment> findByCustomerCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT cp FROM CustomerPayment cp WHERE cp.order.orderId = :orderId")
    List<CustomerPayment> findByOrderOrderId(@Param("orderId") UUID orderId);
    
    List<CustomerPayment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
    
    // paymentType là String field trong entity, nên giữ nguyên
    List<CustomerPayment> findByPaymentType(String paymentType);
    
    // paymentMethod là enum PaymentMethod trong entity, cần nhận enum
    List<CustomerPayment> findByPaymentMethod(PaymentMethod paymentMethod);
    
    // ✅ Backward compatibility (nhận String, convert sang enum)
    default List<CustomerPayment> findByPaymentMethodString(String paymentMethod) {
        return findByPaymentMethod(PaymentMethod.fromString(paymentMethod));
    }
    
    @Query("SELECT cp FROM CustomerPayment cp WHERE cp.processedBy.userId = :userId")
    List<CustomerPayment> findByProcessedByUserId(@Param("userId") UUID userId);
}