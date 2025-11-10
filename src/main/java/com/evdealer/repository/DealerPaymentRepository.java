package com.evdealer.repository;

import com.evdealer.entity.DealerPayment;
import com.evdealer.enums.DealerPaymentStatus;
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
public interface DealerPaymentRepository extends JpaRepository<DealerPayment, UUID> {
    
    @Query("SELECT DISTINCT dp FROM DealerPayment dp LEFT JOIN FETCH dp.invoice i LEFT JOIN FETCH i.dealerOrder do LEFT JOIN FETCH do.dealer")
    List<DealerPayment> findAllWithDetails();
    
    @Query("SELECT DISTINCT dp FROM DealerPayment dp LEFT JOIN FETCH dp.invoice i LEFT JOIN FETCH i.dealerOrder do LEFT JOIN FETCH do.dealer WHERE dp.paymentNumber = :paymentNumber")
    Optional<DealerPayment> findByPaymentNumber(@Param("paymentNumber") String paymentNumber);
    
    boolean existsByPaymentNumber(String paymentNumber);
    
    @Query("SELECT DISTINCT dp FROM DealerPayment dp LEFT JOIN FETCH dp.invoice i LEFT JOIN FETCH i.dealerOrder do LEFT JOIN FETCH do.dealer WHERE dp.status = :status")
    List<DealerPayment> findByStatus(@Param("status") DealerPaymentStatus status);
    
    @Query("SELECT DISTINCT dp FROM DealerPayment dp LEFT JOIN FETCH dp.invoice i LEFT JOIN FETCH i.dealerOrder do LEFT JOIN FETCH do.dealer WHERE dp.invoice.invoiceId = :invoiceId")
    List<DealerPayment> findByInvoiceInvoiceId(@Param("invoiceId") UUID invoiceId);
    
    @Query("SELECT DISTINCT dp FROM DealerPayment dp LEFT JOIN FETCH dp.invoice i LEFT JOIN FETCH i.dealerOrder do LEFT JOIN FETCH do.dealer WHERE dp.paymentDate BETWEEN :startDate AND :endDate")
    List<DealerPayment> findByPaymentDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("""
           SELECT DISTINCT dp
           FROM DealerPayment dp
           LEFT JOIN FETCH dp.invoice i
           LEFT JOIN FETCH i.dealerOrder do
           LEFT JOIN FETCH do.dealer
           WHERE dp.paymentMethod = :paymentMethod
           """)
    List<DealerPayment> findByPaymentMethod(@Param("paymentMethod") PaymentMethod paymentMethod);
    
    // ✅ Backward compatibility (nhận String, convert sang enum)
    default List<DealerPayment> findByPaymentType(String paymentType) {
        return findByPaymentMethod(PaymentMethod.fromString(paymentType));
    }
    
    @Query("SELECT DISTINCT dp FROM DealerPayment dp LEFT JOIN FETCH dp.invoice i LEFT JOIN FETCH i.dealerOrder do LEFT JOIN FETCH do.dealer WHERE dp.referenceNumber = :referenceNumber")
    List<DealerPayment> findByReferenceNumber(@Param("referenceNumber") String referenceNumber);
    
    // Additional methods for new APIs
    @Query("SELECT DISTINCT dp FROM DealerPayment dp LEFT JOIN FETCH dp.invoice i LEFT JOIN FETCH i.dealerOrder do LEFT JOIN FETCH do.dealer WHERE do.dealer.dealerId = :dealerId")
    List<DealerPayment> findByInvoiceDealerOrderDealerDealerId(@Param("dealerId") UUID dealerId);
    
    @Query("SELECT DISTINCT dp FROM DealerPayment dp LEFT JOIN FETCH dp.invoice i LEFT JOIN FETCH i.dealerOrder do LEFT JOIN FETCH do.dealer WHERE dp.paymentId = :paymentId")
    Optional<DealerPayment> findByIdWithDetails(@Param("paymentId") UUID paymentId);
    
    long countByStatus(DealerPaymentStatus status);
}
