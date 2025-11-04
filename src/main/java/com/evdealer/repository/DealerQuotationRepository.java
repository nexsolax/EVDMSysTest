package com.evdealer.repository;

import com.evdealer.entity.DealerQuotation;
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
    
    List<DealerQuotation> findByDealerDealerId(UUID dealerId);
    
    List<DealerQuotation> findByDealerOrderDealerOrderId(UUID dealerOrderId);
    
    List<DealerQuotation> findByStatus(String status);
    
    List<DealerQuotation> findByEvmStaffUserId(UUID evmStaffId);
    
    List<DealerQuotation> findByQuotationDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT q FROM DealerQuotation q WHERE q.expiryDate < :currentDate AND q.status IN ('pending', 'sent')")
    List<DealerQuotation> findExpiredQuotations(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT q FROM DealerQuotation q WHERE q.dealer.dealerId = :dealerId AND q.status = :status")
    List<DealerQuotation> findByDealerAndStatus(@Param("dealerId") UUID dealerId, @Param("status") String status);
    
    long countByStatus(String status);
}

