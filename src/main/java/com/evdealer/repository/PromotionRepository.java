package com.evdealer.repository;

import com.evdealer.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    
    @Query("SELECT p FROM Promotion p")
    List<Promotion> findAllWithDetails();
    
    @Query("SELECT p FROM Promotion p WHERE p.variant.variantId = :variantId")
    List<Promotion> findByVariantVariantId(@Param("variantId") Integer variantId);
    
    @Query("SELECT p FROM Promotion p WHERE p.status = :status")
    List<Promotion> findByStatus(@Param("status") String status);
    
    @Query("SELECT p FROM Promotion p WHERE p.startDate <= :date AND p.endDate >= :date")
    List<Promotion> findActivePromotions(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM Promotion p WHERE p.startDate <= :date AND p.endDate >= :date AND p.status = 'active'")
    List<Promotion> findActivePromotionsByDate(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM Promotion p WHERE p.variant.variantId = :variantId AND p.startDate <= :date AND p.endDate >= :date AND p.status = 'active'")
    List<Promotion> findActivePromotionsByVariantAndDate(@Param("variantId") Integer variantId, @Param("date") LocalDate date);
    
    @Query("SELECT p FROM Promotion p WHERE p.title LIKE CONCAT('%', :title, '%')")
    List<Promotion> findByTitleContaining(@Param("title") String title);
}

