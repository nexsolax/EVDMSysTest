package com.evdealer.service;

import com.evdealer.entity.PricingPolicy;
import com.evdealer.enums.PricingPolicyStatus;
import com.evdealer.repository.PricingPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PricingPolicyService {
    
    @Autowired
    private PricingPolicyRepository pricingPolicyRepository;
    
    public List<PricingPolicy> getAllPricingPolicies() {
        try {
            return pricingPolicyRepository.findAll();
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    public Optional<PricingPolicy> getPricingPolicyById(UUID policyId) {
        return pricingPolicyRepository.findById(policyId);
    }
    
    public List<PricingPolicy> getPricingPoliciesByVariant(Integer variantId) {
        return pricingPolicyRepository.findByVariantId(variantId);
    }
    
    public List<PricingPolicy> getPricingPoliciesByStatus(String status) {
        // Convert string to enum for validation
        PricingPolicyStatus statusEnum = PricingPolicyStatus.fromString(status);
        return pricingPolicyRepository.findByStatus(statusEnum);
    }
    
    public List<PricingPolicy> getPricingPoliciesByType(String policyType) {
        return pricingPolicyRepository.findByPolicyType(policyType);
    }
    
    public List<PricingPolicy> getPricingPoliciesByCustomerType(String customerType) {
        return pricingPolicyRepository.findByCustomerType(customerType);
    }
    
    public List<PricingPolicy> getPricingPoliciesByRegion(String region) {
        return pricingPolicyRepository.findByRegion(region);
    }
    
    public List<PricingPolicy> getPricingPoliciesByDealer(UUID dealerId) {
        return pricingPolicyRepository.findByDealerId(dealerId);
    }
    
    public List<PricingPolicy> getPricingPoliciesByScope(String scope) {
        return pricingPolicyRepository.findByScope(scope);
    }
    
    public List<PricingPolicy> getActivePricingPoliciesByDate(LocalDate date) {
        return pricingPolicyRepository.findActivePoliciesByDate(date);
    }
    
    public List<PricingPolicy> getActivePricingPoliciesByVariantAndDate(Integer variantId, LocalDate date) {
        return pricingPolicyRepository.findActivePoliciesByVariantAndDate(variantId, date, PricingPolicyStatus.ACTIVE);
    }
    
    public List<PricingPolicy> getPricingPoliciesByName(String policyName) {
        return pricingPolicyRepository.findByPolicyNameContaining(policyName);
    }
    
    public List<PricingPolicy> getActivePricingPoliciesByDateOrderByPriority(LocalDate date) {
        return pricingPolicyRepository.findActivePoliciesByDateOrderByPriority(date, PricingPolicyStatus.ACTIVE);
    }
    
    public List<PricingPolicy> getActivePricingPoliciesByVariantCustomerTypeAndDate(Integer variantId, String customerType, LocalDate date) {
        return pricingPolicyRepository.findActivePoliciesByVariantCustomerTypeAndDate(variantId, customerType, date, PricingPolicyStatus.ACTIVE);
    }
    
    public PricingPolicy createPricingPolicy(PricingPolicy pricingPolicy) {
        return pricingPolicyRepository.save(pricingPolicy);
    }
    
    public PricingPolicy updatePricingPolicy(UUID policyId, PricingPolicy pricingPolicyDetails) {
        PricingPolicy pricingPolicy = pricingPolicyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Pricing policy not found with id: " + policyId));
        
        pricingPolicy.setVariant(pricingPolicyDetails.getVariant());
        pricingPolicy.setDealer(pricingPolicyDetails.getDealer());
        pricingPolicy.setPolicyName(pricingPolicyDetails.getPolicyName());
        pricingPolicy.setDescription(pricingPolicyDetails.getDescription());
        pricingPolicy.setPolicyType(pricingPolicyDetails.getPolicyType());
        pricingPolicy.setBasePrice(pricingPolicyDetails.getBasePrice());
        pricingPolicy.setDiscountPercent(pricingPolicyDetails.getDiscountPercent());
        pricingPolicy.setDiscountAmount(pricingPolicyDetails.getDiscountAmount());
        pricingPolicy.setMarkupPercent(pricingPolicyDetails.getMarkupPercent());
        pricingPolicy.setMarkupAmount(pricingPolicyDetails.getMarkupAmount());
        pricingPolicy.setEffectiveDate(pricingPolicyDetails.getEffectiveDate());
        pricingPolicy.setExpiryDate(pricingPolicyDetails.getExpiryDate());
        pricingPolicy.setMinQuantity(pricingPolicyDetails.getMinQuantity());
        pricingPolicy.setMaxQuantity(pricingPolicyDetails.getMaxQuantity());
        pricingPolicy.setCustomerType(pricingPolicyDetails.getCustomerType());
        pricingPolicy.setRegion(pricingPolicyDetails.getRegion());
        pricingPolicy.setScope(pricingPolicyDetails.getScope());
        pricingPolicy.setStatus(pricingPolicyDetails.getStatus());
        pricingPolicy.setPriority(pricingPolicyDetails.getPriority());
        
        return pricingPolicyRepository.save(pricingPolicy);
    }
    
    public void deletePricingPolicy(UUID policyId) {
        PricingPolicy pricingPolicy = pricingPolicyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Pricing policy not found with id: " + policyId));
        pricingPolicyRepository.delete(pricingPolicy);
    }
    
    public PricingPolicy updatePricingPolicyStatus(UUID policyId, String status) {
        PricingPolicy pricingPolicy = pricingPolicyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Pricing policy not found with id: " + policyId));
        pricingPolicy.setStatus(status);
        return pricingPolicyRepository.save(pricingPolicy);
    }
    
    /**
     * Lấy active pricing policy cho variant và dealer tại thời điểm hiện tại
     * Ưu tiên policy dành riêng cho dealer, sau đó là policy chung
     */
    public Optional<PricingPolicy> getActivePolicyForVariantAndDealer(Integer variantId, UUID dealerId) {
        LocalDate today = LocalDate.now();
        
        // Tìm policy dành riêng cho dealer và variant
        List<PricingPolicy> dealerPolicies = pricingPolicyRepository.findActivePoliciesByVariantAndDate(
            variantId, today, PricingPolicyStatus.ACTIVE);
        
        // Ưu tiên policy có dealerId matching
        Optional<PricingPolicy> dealerSpecificPolicy = dealerPolicies.stream()
            .filter(p -> p.getDealer() != null && p.getDealer().getDealerId().equals(dealerId))
            .filter(p -> p.getEffectiveDate() != null && p.getEffectiveDate().isBefore(today.plusDays(1)))
            .filter(p -> p.getExpiryDate() == null || p.getExpiryDate().isAfter(today.minusDays(1)))
            .sorted((p1, p2) -> {
                // Sort by priority (higher priority first)
                int priority1 = p1.getPriority() != null ? p1.getPriority() : 0;
                int priority2 = p2.getPriority() != null ? p2.getPriority() : 0;
                return Integer.compare(priority2, priority1);
            })
            .findFirst();
        
        if (dealerSpecificPolicy.isPresent()) {
            return dealerSpecificPolicy;
        }
        
        // Nếu không có policy riêng cho dealer, tìm policy chung (dealer = null)
        return dealerPolicies.stream()
            .filter(p -> p.getDealer() == null)
            .filter(p -> p.getEffectiveDate() != null && p.getEffectiveDate().isBefore(today.plusDays(1)))
            .filter(p -> p.getExpiryDate() == null || p.getExpiryDate().isAfter(today.minusDays(1)))
            .sorted((p1, p2) -> {
                int priority1 = p1.getPriority() != null ? p1.getPriority() : 0;
                int priority2 = p2.getPriority() != null ? p2.getPriority() : 0;
                return Integer.compare(priority2, priority1);
            })
            .findFirst();
    }
}

