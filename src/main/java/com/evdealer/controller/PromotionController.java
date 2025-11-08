package com.evdealer.controller;

import com.evdealer.dto.PromotionDTO;
import com.evdealer.entity.Promotion;
import com.evdealer.service.PromotionService;
import com.evdealer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "*")
public class PromotionController {
    
    @Autowired
    private PromotionService promotionService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    public ResponseEntity<?> getAllPromotions() {
        try {
            List<Promotion> promotions = promotionService.getAllPromotions();
            return ResponseEntity.ok(promotions.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve promotions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{promotionId}")
    public ResponseEntity<?> getPromotionById(@PathVariable UUID promotionId) {
        try {
            return promotionService.getPromotionById(promotionId)
                    .map(promotion -> ResponseEntity.ok(toDTO(promotion)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve promotion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<?> getPromotionsByVariant(@PathVariable Integer variantId) {
        try {
            List<Promotion> promotions = promotionService.getPromotionsByVariant(variantId);
            return ResponseEntity.ok(promotions.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve promotions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPromotionsByStatus(@PathVariable String status) {
        try {
            List<Promotion> promotions = promotionService.getPromotionsByStatus(status);
            return ResponseEntity.ok(promotions.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve promotions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<?> getActivePromotions() {
        try {
            List<Promotion> promotions = promotionService.getActivePromotions();
            return ResponseEntity.ok(promotions.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve promotions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/active/date/{date}")
    public ResponseEntity<?> getActivePromotionsByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<Promotion> promotions = promotionService.getActivePromotionsByDate(date);
            return ResponseEntity.ok(promotions.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve promotions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/active/variant/{variantId}")
    public ResponseEntity<?> getActivePromotionsByVariant(@PathVariable Integer variantId) {
        try {
            List<Promotion> promotions = promotionService.getActivePromotionsByVariant(variantId);
            return ResponseEntity.ok(promotions.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve promotions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/active/variant/{variantId}/date/{date}")
    public ResponseEntity<?> getActivePromotionsByVariantAndDate(
            @PathVariable Integer variantId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<Promotion> promotions = promotionService.getActivePromotionsByVariantAndDate(variantId, date);
            return ResponseEntity.ok(promotions.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve promotions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> getPromotionsByTitle(@RequestParam String title) {
        try {
            List<Promotion> promotions = promotionService.getPromotionsByTitle(title);
            return ResponseEntity.ok(promotions.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve promotions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createPromotion(@RequestBody Promotion promotion) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo promotion
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create promotions");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Promotion createdPromotion = promotionService.createPromotion(promotion);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdPromotion));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create promotion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create promotion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{promotionId}")
    public ResponseEntity<?> updatePromotion(@PathVariable UUID promotionId, @RequestBody Promotion promotionDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update promotion
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update promotions");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Promotion updatedPromotion = promotionService.updatePromotion(promotionId, promotionDetails);
            return ResponseEntity.ok(toDTO(updatedPromotion));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update promotion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update promotion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{promotionId}/status")
    public ResponseEntity<?> updatePromotionStatus(@PathVariable UUID promotionId, @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update promotion status
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update promotion status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Promotion updatedPromotion = promotionService.updatePromotionStatus(promotionId, status);
            return ResponseEntity.ok(toDTO(updatedPromotion));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update promotion status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update promotion status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{promotionId}")
    public ResponseEntity<?> deletePromotion(@PathVariable UUID promotionId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa promotion
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete promotions");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            promotionService.deletePromotion(promotionId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Promotion deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete promotion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete promotion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private PromotionDTO toDTO(Promotion p) {
        PromotionDTO dto = new PromotionDTO();
        dto.setPromotionId(p.getPromotionId());
        dto.setVariantId(p.getVariant() != null ? p.getVariant().getVariantId() : null);
        dto.setTitle(p.getTitle());
        dto.setDiscountPercent(p.getDiscountPercent());
        dto.setDiscountAmount(p.getDiscountAmount());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        return dto;
    }
}

