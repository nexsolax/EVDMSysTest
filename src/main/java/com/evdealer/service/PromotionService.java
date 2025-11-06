package com.evdealer.service;

import com.evdealer.entity.Promotion;
import com.evdealer.entity.VehicleVariant;
import com.evdealer.repository.PromotionRepository;
import com.evdealer.repository.VehicleVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PromotionService {
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    
    public List<Promotion> getAllPromotions() {
        try {
            return promotionRepository.findAll();
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public Optional<Promotion> getPromotionById(UUID promotionId) {
        return promotionRepository.findById(promotionId);
    }
    
    public List<Promotion> getPromotionsByVariant(Integer variantId) {
        return promotionRepository.findByVariantVariantId(variantId);
    }
    
    public List<Promotion> getPromotionsByStatus(String status) {
        return promotionRepository.findByStatus(status);
    }
    
    public List<Promotion> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDate.now());
    }
    
    public List<Promotion> getActivePromotionsByDate(LocalDate date) {
        return promotionRepository.findActivePromotionsByDate(date);
    }
    
    public List<Promotion> getActivePromotionsByVariant(Integer variantId) {
        return promotionRepository.findActivePromotionsByVariantAndDate(variantId, LocalDate.now());
    }
    
    public List<Promotion> getActivePromotionsByVariantAndDate(Integer variantId, LocalDate date) {
        return promotionRepository.findActivePromotionsByVariantAndDate(variantId, date);
    }
    
    public List<Promotion> getPromotionsByTitle(String title) {
        return promotionRepository.findByTitleContaining(title);
    }
    
    public Promotion createPromotion(Promotion promotion) {
        // Validate dates
        if (promotion.getStartDate() != null && promotion.getEndDate() != null) {
            if (promotion.getEndDate().isBefore(promotion.getStartDate())) {
                throw new RuntimeException("End date must be after start date");
            }
        }
        
        // Validate discount fields
        if (promotion.getDiscountPercent() == null && promotion.getDiscountAmount() == null) {
            throw new RuntimeException("Either discount percent or discount amount must be provided");
        }
        
        // Validate discount percent range
        if (promotion.getDiscountPercent() != null) {
            if (promotion.getDiscountPercent().compareTo(java.math.BigDecimal.ZERO) < 0 ||
                promotion.getDiscountPercent().compareTo(new java.math.BigDecimal("100")) > 0) {
                throw new RuntimeException("Discount percent must be between 0 and 100");
            }
        }
        
        // Validate discount amount
        if (promotion.getDiscountAmount() != null && 
            promotion.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Discount amount must be positive");
        }
        
        // Validate foreign key: variant_id
        if (promotion.getVariant() != null && promotion.getVariant().getVariantId() != null) {
            VehicleVariant variant = vehicleVariantRepository.findById(promotion.getVariant().getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found with id: " + promotion.getVariant().getVariantId()));
            // Ensure variant is properly set
            promotion.setVariant(variant);
        }
        
        return promotionRepository.save(promotion);
    }
    
    public Promotion updatePromotion(UUID promotionId, Promotion promotionDetails) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + promotionId));
        
        // Validate dates
        if (promotionDetails.getStartDate() != null && promotionDetails.getEndDate() != null) {
            if (promotionDetails.getEndDate().isBefore(promotionDetails.getStartDate())) {
                throw new RuntimeException("End date must be after start date");
            }
        }
        
        // Validate discount fields
        if (promotionDetails.getDiscountPercent() == null && promotionDetails.getDiscountAmount() == null) {
            throw new RuntimeException("Either discount percent or discount amount must be provided");
        }
        
        // Validate discount percent range
        if (promotionDetails.getDiscountPercent() != null) {
            if (promotionDetails.getDiscountPercent().compareTo(java.math.BigDecimal.ZERO) < 0 ||
                promotionDetails.getDiscountPercent().compareTo(new java.math.BigDecimal("100")) > 0) {
                throw new RuntimeException("Discount percent must be between 0 and 100");
            }
        }
        
        // Validate discount amount
        if (promotionDetails.getDiscountAmount() != null && 
            promotionDetails.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Discount amount must be positive");
        }
        
        // Validate foreign key: variant_id
        if (promotionDetails.getVariant() != null && promotionDetails.getVariant().getVariantId() != null) {
            VehicleVariant variant = vehicleVariantRepository.findById(promotionDetails.getVariant().getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found with id: " + promotionDetails.getVariant().getVariantId()));
            promotion.setVariant(variant);
        } else {
            promotion.setVariant(null);
        }
        
        promotion.setTitle(promotionDetails.getTitle());
        promotion.setDescription(promotionDetails.getDescription());
        promotion.setDiscountPercent(promotionDetails.getDiscountPercent());
        promotion.setDiscountAmount(promotionDetails.getDiscountAmount());
        promotion.setStartDate(promotionDetails.getStartDate());
        promotion.setEndDate(promotionDetails.getEndDate());
        promotion.setStatus(promotionDetails.getStatus());
        
        return promotionRepository.save(promotion);
    }
    
    public void deletePromotion(UUID promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + promotionId));
        promotionRepository.delete(promotion);
    }
    
    public Promotion updatePromotionStatus(UUID promotionId, String status) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + promotionId));
        promotion.setStatus(status);
        return promotionRepository.save(promotion);
    }
}

