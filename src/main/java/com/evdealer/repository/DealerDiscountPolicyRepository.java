package com.evdealer.repository;

import com.evdealer.entity.DealerDiscountPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DealerDiscountPolicyRepository extends JpaRepository<DealerDiscountPolicy, UUID> {
    
    @Query("SELECT DISTINCT ddp FROM DealerDiscountPolicy ddp LEFT JOIN FETCH ddp.variant v LEFT JOIN FETCH v.model m LEFT JOIN FETCH m.brand")
    List<DealerDiscountPolicy> findAllWithDetails();
    
    @Query("SELECT ddp FROM DealerDiscountPolicy ddp WHERE ddp.variant.variantId = :variantId")
    List<DealerDiscountPolicy> findByVariantId(@Param("variantId") Integer variantId);
    
    @Query("SELECT ddp FROM DealerDiscountPolicy ddp WHERE ddp.status = :status")
    List<DealerDiscountPolicy> findByStatus(@Param("status") String status);
    
    @Query("SELECT ddp FROM DealerDiscountPolicy ddp WHERE ddp.startDate <= :date AND ddp.endDate >= :date")
    List<DealerDiscountPolicy> findActivePolicies(@Param("date") LocalDate date);
}

