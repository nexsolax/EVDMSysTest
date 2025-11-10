package com.evdealer.repository;

import com.evdealer.entity.DealerInstallmentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DealerInstallmentScheduleRepository extends JpaRepository<DealerInstallmentSchedule, UUID> {
    
    @Query("SELECT DISTINCT dis FROM DealerInstallmentSchedule dis LEFT JOIN FETCH dis.plan p LEFT JOIN FETCH p.invoice i")
    List<DealerInstallmentSchedule> findAllWithDetails();
    
    @Query("SELECT dis FROM DealerInstallmentSchedule dis WHERE dis.plan.planId = :planId")
    List<DealerInstallmentSchedule> findByPlanId(@Param("planId") UUID planId);
    
    @Query("SELECT dis FROM DealerInstallmentSchedule dis WHERE dis.status = :status")
    List<DealerInstallmentSchedule> findByStatus(@Param("status") String status);
    
    @Query("SELECT dis FROM DealerInstallmentSchedule dis WHERE dis.dueDate BETWEEN :startDate AND :endDate")
    List<DealerInstallmentSchedule> findByDueDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT dis FROM DealerInstallmentSchedule dis WHERE dis.dueDate < CURRENT_DATE AND dis.status = 'pending'")
    List<DealerInstallmentSchedule> findOverdueSchedules();
}

