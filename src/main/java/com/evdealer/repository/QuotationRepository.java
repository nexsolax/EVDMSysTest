package com.evdealer.repository;

import com.evdealer.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, UUID> {
    
    @Query("SELECT DISTINCT q FROM Quotation q LEFT JOIN FETCH q.customer LEFT JOIN FETCH q.user LEFT JOIN FETCH q.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH q.color")
    List<Quotation> findAllWithDetails();
    
    @Query("SELECT DISTINCT q FROM Quotation q LEFT JOIN FETCH q.customer LEFT JOIN FETCH q.user LEFT JOIN FETCH q.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand LEFT JOIN FETCH q.color")
    List<Quotation> findAllWithRelationships();
    
    Optional<Quotation> findByQuotationNumber(String quotationNumber);
    
    boolean existsByQuotationNumber(String quotationNumber);
    
    List<Quotation> findByStatus(String status);
    
    @Query("SELECT q FROM Quotation q WHERE q.customer.customerId = :customerId")
    List<Quotation> findByCustomerCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT q FROM Quotation q WHERE q.user.userId = :userId")
    List<Quotation> findByUserUserId(@Param("userId") UUID userId);
    
    List<Quotation> findByQuotationDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT q FROM Quotation q WHERE q.quotationDate < :currentDate AND q.status = 'pending'")
    List<Quotation> findExpiredQuotations(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT q FROM Quotation q WHERE q.quotationDate < CURRENT_DATE AND q.status = 'pending'")
    List<Quotation> findExpiredQuotations();
    
    @Query("SELECT q FROM Quotation q WHERE q.variant.variantId = :variantId")
    List<Quotation> findByVariantVariantId(@Param("variantId") Integer variantId);
    
    @Query("SELECT q FROM Quotation q WHERE q.color.colorId = :colorId")
    List<Quotation> findByColorColorId(@Param("colorId") Integer colorId);
}
