package com.evdealer.repository;

import com.evdealer.entity.DealerInstallmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DealerInstallmentPlanRepository extends JpaRepository<DealerInstallmentPlan, UUID> {
    
    @Query("SELECT DISTINCT dip FROM DealerInstallmentPlan dip LEFT JOIN FETCH dip.invoice i LEFT JOIN FETCH i.dealerOrder do")
    List<DealerInstallmentPlan> findAllWithDetails();
    
    @Query("SELECT dip FROM DealerInstallmentPlan dip WHERE dip.invoice.invoiceId = :invoiceId")
    List<DealerInstallmentPlan> findByInvoiceId(@Param("invoiceId") UUID invoiceId);
    
    Optional<DealerInstallmentPlan> findByContractNumber(String contractNumber);
}

