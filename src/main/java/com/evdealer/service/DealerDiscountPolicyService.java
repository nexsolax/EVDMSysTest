package com.evdealer.service;

import com.evdealer.entity.DealerDiscountPolicy;
import com.evdealer.entity.VehicleVariant;
import com.evdealer.repository.DealerDiscountPolicyRepository;
import com.evdealer.repository.VehicleVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DealerDiscountPolicyService {
    
    @Autowired
    private DealerDiscountPolicyRepository dealerDiscountPolicyRepository;
    
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    
    @Transactional(readOnly = true)
    public List<DealerDiscountPolicy> getAllPolicies() {
        try {
            return dealerDiscountPolicyRepository.findAllWithDetails();
        } catch (Exception e) {
            // Fallback to simple findAll if query fails
            return dealerDiscountPolicyRepository.findAll();
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<DealerDiscountPolicy> getPolicyById(UUID policyId) {
        return dealerDiscountPolicyRepository.findById(policyId);
    }
    
    @Transactional(readOnly = true)
    public List<DealerDiscountPolicy> getPoliciesByVariantId(Integer variantId) {
        return dealerDiscountPolicyRepository.findByVariantId(variantId);
    }
    
    @Transactional(readOnly = true)
    public List<DealerDiscountPolicy> getPoliciesByStatus(String status) {
        return dealerDiscountPolicyRepository.findByStatus(status);
    }
    
    public DealerDiscountPolicy createPolicy(DealerDiscountPolicy policy) {
        // Validate variant exists
        if (policy.getVariant() != null && policy.getVariant().getVariantId() != null) {
            VehicleVariant variant = vehicleVariantRepository.findById(policy.getVariant().getVariantId())
                .orElseThrow(() -> new RuntimeException("Variant not found with ID: " + policy.getVariant().getVariantId()));
            policy.setVariant(variant);
        }
        
        // Validate dates
        if (policy.getStartDate() != null && policy.getEndDate() != null) {
            if (policy.getEndDate().isBefore(policy.getStartDate())) {
                throw new RuntimeException("End date must be after start date");
            }
        }
        
        return dealerDiscountPolicyRepository.save(policy);
    }
    
    public DealerDiscountPolicy updatePolicy(UUID policyId, DealerDiscountPolicy policyDetails) {
        DealerDiscountPolicy policy = dealerDiscountPolicyRepository.findById(policyId)
            .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));
        
        // Update fields
        if (policyDetails.getPolicyName() != null) {
            policy.setPolicyName(policyDetails.getPolicyName());
        }
        if (policyDetails.getDescription() != null) {
            policy.setDescription(policyDetails.getDescription());
        }
        if (policyDetails.getDiscountPercent() != null) {
            policy.setDiscountPercent(policyDetails.getDiscountPercent());
        }
        if (policyDetails.getDiscountAmount() != null) {
            policy.setDiscountAmount(policyDetails.getDiscountAmount());
        }
        if (policyDetails.getStartDate() != null) {
            policy.setStartDate(policyDetails.getStartDate());
        }
        if (policyDetails.getEndDate() != null) {
            policy.setEndDate(policyDetails.getEndDate());
        }
        if (policyDetails.getStatus() != null) {
            policy.setStatus(policyDetails.getStatus());
        }
        if (policyDetails.getVariant() != null && policyDetails.getVariant().getVariantId() != null) {
            VehicleVariant variant = vehicleVariantRepository.findById(policyDetails.getVariant().getVariantId())
                .orElseThrow(() -> new RuntimeException("Variant not found with ID: " + policyDetails.getVariant().getVariantId()));
            policy.setVariant(variant);
        }
        
        // Validate dates
        if (policy.getStartDate() != null && policy.getEndDate() != null) {
            if (policy.getEndDate().isBefore(policy.getStartDate())) {
                throw new RuntimeException("End date must be after start date");
            }
        }
        
        return dealerDiscountPolicyRepository.save(policy);
    }
    
    public void deletePolicy(UUID policyId) {
        DealerDiscountPolicy policy = dealerDiscountPolicyRepository.findById(policyId)
            .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));
        dealerDiscountPolicyRepository.delete(policy);
    }
}

