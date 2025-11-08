package com.evdealer.repository;

import com.evdealer.entity.PricingPolicy;
import com.evdealer.enums.PricingPolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PricingPolicyRepository extends JpaRepository<PricingPolicy, UUID> {
    
    @Query("SELECT p FROM PricingPolicy p WHERE p.variant.variantId = :variantId")
    List<PricingPolicy> findByVariantId(@Param("variantId") Integer variantId);
    
    @Query("SELECT p FROM PricingPolicy p WHERE p.status = :status")
    List<PricingPolicy> findByStatus(@Param("status") PricingPolicyStatus status);
    
    @Query("SELECT p FROM PricingPolicy p WHERE p.policyType = :policyType")
    List<PricingPolicy> findByPolicyType(@Param("policyType") String policyType);
    
    @Query("SELECT p FROM PricingPolicy p WHERE p.customerType = :customerType")
    List<PricingPolicy> findByCustomerType(@Param("customerType") String customerType);
    
    @Query("SELECT p FROM PricingPolicy p WHERE p.region = :region")
    List<PricingPolicy> findByRegion(@Param("region") String region);
    
    @Query("SELECT p FROM PricingPolicy p WHERE p.dealer.dealerId = :dealerId")
    List<PricingPolicy> findByDealerId(@Param("dealerId") UUID dealerId);
    
    @Query("SELECT p FROM PricingPolicy p WHERE p.scope = :scope")
    List<PricingPolicy> findByScope(@Param("scope") String scope);
    
    @Query("SELECT p FROM PricingPolicy p WHERE p.effectiveDate <= :date AND (p.expiryDate IS NULL OR p.expiryDate >= :date)")
    List<PricingPolicy> findActivePoliciesByDate(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM PricingPolicy p WHERE p.variant.variantId = :variantId AND p.effectiveDate <= :date AND (p.expiryDate IS NULL OR p.expiryDate >= :date) AND p.status = :status")
    List<PricingPolicy> findActivePoliciesByVariantAndDate(@Param("variantId") Integer variantId, @Param("date") LocalDate date, @Param("status") PricingPolicyStatus status);
    
    @Query("SELECT p FROM PricingPolicy p WHERE p.policyName LIKE %:policyName%")
    List<PricingPolicy> findByPolicyNameContaining(@Param("policyName") String policyName);
    
    @Query("SELECT p FROM PricingPolicy p WHERE p.effectiveDate <= :date AND (p.expiryDate IS NULL OR p.expiryDate >= :date) AND p.status = :status ORDER BY p.priority DESC")
    List<PricingPolicy> findActivePoliciesByDateOrderByPriority(@Param("date") LocalDate date, @Param("status") PricingPolicyStatus status);
    
    @Query("SELECT p FROM PricingPolicy p WHERE p.variant.variantId = :variantId AND p.customerType = :customerType AND p.effectiveDate <= :date AND (p.expiryDate IS NULL OR p.expiryDate >= :date) AND p.status = :status ORDER BY p.priority DESC")
    List<PricingPolicy> findActivePoliciesByVariantCustomerTypeAndDate(@Param("variantId") Integer variantId, @Param("customerType") String customerType, @Param("date") LocalDate date, @Param("status") PricingPolicyStatus status);
}

