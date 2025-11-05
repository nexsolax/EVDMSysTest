package com.evdealer.repository;

import com.evdealer.entity.DealerInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DealerInvoiceRepository extends JpaRepository<DealerInvoice, UUID> {
    
    @Query("SELECT DISTINCT di FROM DealerInvoice di LEFT JOIN FETCH di.dealerOrder do LEFT JOIN FETCH do.dealer LEFT JOIN FETCH di.evmStaff")
    List<DealerInvoice> findAllWithDetails();
    
    Optional<DealerInvoice> findByInvoiceNumber(String invoiceNumber);
    
    boolean existsByInvoiceNumber(String invoiceNumber);
    
    List<DealerInvoice> findByStatus(String status);
    
    @Query("SELECT DISTINCT di FROM DealerInvoice di LEFT JOIN FETCH di.dealerOrder do LEFT JOIN FETCH do.dealer LEFT JOIN FETCH di.evmStaff WHERE di.dealerOrder.dealerOrderId = :dealerOrderId")
    List<DealerInvoice> findByDealerOrderDealerOrderId(@Param("dealerOrderId") UUID dealerOrderId);
    
    @Query("SELECT DISTINCT di FROM DealerInvoice di LEFT JOIN FETCH di.dealerOrder do LEFT JOIN FETCH do.dealer LEFT JOIN FETCH di.evmStaff WHERE di.invoiceId = :invoiceId")
    Optional<DealerInvoice> findByIdWithDetails(@Param("invoiceId") UUID invoiceId);
    
    @Query("SELECT di FROM DealerInvoice di WHERE di.evmStaff.userId = :evmStaffId")
    List<DealerInvoice> findByEvmStaffUserId(@Param("evmStaffId") UUID evmStaffId);
    
    List<DealerInvoice> findByInvoiceDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT di FROM DealerInvoice di WHERE di.dueDate < :currentDate AND di.status IN ('issued', 'partially_paid')")
    List<DealerInvoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT di FROM DealerInvoice di WHERE di.dueDate < CURRENT_DATE AND di.status IN ('issued', 'partially_paid')")
    List<DealerInvoice> findOverdueInvoices();
    
    List<DealerInvoice> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Additional methods for new APIs
    @Query("SELECT DISTINCT di FROM DealerInvoice di LEFT JOIN FETCH di.dealerOrder do LEFT JOIN FETCH do.dealer LEFT JOIN FETCH di.evmStaff WHERE do.dealer.dealerId = :dealerId")
    List<DealerInvoice> findByDealerOrderDealerDealerId(@Param("dealerId") UUID dealerId);
    
    @Query("SELECT di FROM DealerInvoice di WHERE di.dealerOrder.dealer.dealerId = :dealerId AND di.status = :status")
    List<DealerInvoice> findByDealerOrderDealerDealerIdAndStatus(@Param("dealerId") UUID dealerId, @Param("status") String status);
    
    long countByStatus(String status);
}