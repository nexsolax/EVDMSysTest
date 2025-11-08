package com.evdealer.repository;

import com.evdealer.entity.DealerQuotation;
import com.evdealer.enums.DealerQuotationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DealerQuotationRepository extends JpaRepository<DealerQuotation, UUID> {
    
    Optional<DealerQuotation> findByQuotationNumber(String quotationNumber);
    
    boolean existsByQuotationNumber(String quotationNumber);
    
    @Query("SELECT DISTINCT q FROM DealerQuotation q LEFT JOIN FETCH q.dealer LEFT JOIN FETCH q.evmStaff LEFT JOIN FETCH q.dealerOrder do LEFT JOIN FETCH do.dealer WHERE q.dealer.dealerId = :dealerId")
    List<DealerQuotation> findByDealerDealerId(@Param("dealerId") UUID dealerId);
    
    @Query("SELECT DISTINCT q FROM DealerQuotation q LEFT JOIN FETCH q.dealer LEFT JOIN FETCH q.evmStaff LEFT JOIN FETCH q.dealerOrder do LEFT JOIN FETCH do.dealer WHERE q.dealerOrder.dealerOrderId = :dealerOrderId")
    List<DealerQuotation> findByDealerOrderDealerOrderId(@Param("dealerOrderId") UUID dealerOrderId);
    
    List<DealerQuotation> findByStatus(DealerQuotationStatus status);
    
    List<DealerQuotation> findByEvmStaffUserId(UUID evmStaffId);
    
    List<DealerQuotation> findByQuotationDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT q FROM DealerQuotation q WHERE q.expiryDate < :currentDate AND q.status IN (:statuses)")
    List<DealerQuotation> findExpiredQuotations(@Param("currentDate") LocalDate currentDate, @Param("statuses") List<DealerQuotationStatus> statuses);
    
    @Query("SELECT q FROM DealerQuotation q WHERE q.dealer.dealerId = :dealerId AND q.status = :status")
    List<DealerQuotation> findByDealerAndStatus(@Param("dealerId") UUID dealerId, @Param("status") DealerQuotationStatus status);
    
    @Query("SELECT DISTINCT q FROM DealerQuotation q LEFT JOIN FETCH q.dealer LEFT JOIN FETCH q.evmStaff LEFT JOIN FETCH q.dealerOrder do LEFT JOIN FETCH do.dealer")
    List<DealerQuotation> findAllWithDetails();
    
    @Query("SELECT DISTINCT q FROM DealerQuotation q LEFT JOIN FETCH q.dealer LEFT JOIN FETCH q.evmStaff LEFT JOIN FETCH q.dealerOrder do LEFT JOIN FETCH do.dealer WHERE q.quotationId = :quotationId")
    Optional<DealerQuotation> findByIdWithDetails(@Param("quotationId") UUID quotationId);
    
    long countByStatus(DealerQuotationStatus status);
}

