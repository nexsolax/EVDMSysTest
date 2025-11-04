package com.evdealer.controller;

import com.evdealer.entity.PricingPolicy;
import com.evdealer.service.PricingPolicyService;
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
@RequestMapping("/api/pricing-policies")
@CrossOrigin(origins = "*")
public class PricingPolicyController {
    
    @Autowired
    private PricingPolicyService pricingPolicyService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    public ResponseEntity<List<PricingPolicy>> getAllPricingPolicies() {
        List<PricingPolicy> policies = pricingPolicyService.getAllPricingPolicies();
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PricingPolicy> getPricingPolicyById(@PathVariable UUID id) {
        return pricingPolicyService.getPricingPolicyById(id)
                .map(policy -> ResponseEntity.ok(policy))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<PricingPolicy>> getPricingPoliciesByVariant(@PathVariable Integer variantId) {
        List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByVariant(variantId);
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PricingPolicy>> getPricingPoliciesByStatus(@PathVariable String status) {
        List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByStatus(status);
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/type/{policyType}")
    public ResponseEntity<List<PricingPolicy>> getPricingPoliciesByType(@PathVariable String policyType) {
        List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByType(policyType);
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/customer-type/{customerType}")
    public ResponseEntity<List<PricingPolicy>> getPricingPoliciesByCustomerType(@PathVariable String customerType) {
        List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByCustomerType(customerType);
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/region/{region}")
    public ResponseEntity<List<PricingPolicy>> getPricingPoliciesByRegion(@PathVariable String region) {
        List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByRegion(region);
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/dealer/{dealerId}")
    public ResponseEntity<?> getPricingPoliciesByDealer(@PathVariable UUID dealerId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem policies của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view pricing policies for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByDealer(dealerId);
            return ResponseEntity.ok(policies);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/scope/{scope}")
    public ResponseEntity<List<PricingPolicy>> getPricingPoliciesByScope(@PathVariable String scope) {
        List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByScope(scope);
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/global")
    public ResponseEntity<List<PricingPolicy>> getGlobalPricingPolicies() {
        List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByScope("global");
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/dealer-specific")
    public ResponseEntity<List<PricingPolicy>> getDealerSpecificPricingPolicies() {
        List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByScope("dealer");
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<PricingPolicy>> getActivePricingPolicies() {
        List<PricingPolicy> policies = pricingPolicyService.getActivePricingPoliciesByDate(LocalDate.now());
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/active/date/{date}")
    public ResponseEntity<List<PricingPolicy>> getActivePricingPoliciesByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<PricingPolicy> policies = pricingPolicyService.getActivePricingPoliciesByDate(date);
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/active/variant/{variantId}")
    public ResponseEntity<List<PricingPolicy>> getActivePricingPoliciesByVariant(@PathVariable Integer variantId) {
        List<PricingPolicy> policies = pricingPolicyService.getActivePricingPoliciesByVariantAndDate(variantId, LocalDate.now());
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/active/variant/{variantId}/date/{date}")
    public ResponseEntity<List<PricingPolicy>> getActivePricingPoliciesByVariantAndDate(
            @PathVariable Integer variantId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<PricingPolicy> policies = pricingPolicyService.getActivePricingPoliciesByVariantAndDate(variantId, date);
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/active/variant/{variantId}/customer-type/{customerType}")
    public ResponseEntity<List<PricingPolicy>> getActivePricingPoliciesByVariantAndCustomerType(
            @PathVariable Integer variantId, 
            @PathVariable String customerType) {
        List<PricingPolicy> policies = pricingPolicyService.getActivePricingPoliciesByVariantCustomerTypeAndDate(variantId, customerType, LocalDate.now());
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<PricingPolicy>> getPricingPoliciesByName(@RequestParam String policyName) {
        List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByName(policyName);
        return ResponseEntity.ok(policies);
    }
    
    @PostMapping
    public ResponseEntity<?> createPricingPolicy(@RequestBody PricingPolicy pricingPolicy) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo pricing policy
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create pricing policies");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            PricingPolicy createdPolicy = pricingPolicyService.createPricingPolicy(pricingPolicy);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPolicy);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create pricing policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create pricing policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePricingPolicy(@PathVariable UUID id, @RequestBody PricingPolicy pricingPolicyDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update pricing policy
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update pricing policies");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            PricingPolicy updatedPolicy = pricingPolicyService.updatePricingPolicy(id, pricingPolicyDetails);
            return ResponseEntity.ok(updatedPolicy);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update pricing policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update pricing policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePricingPolicyStatus(@PathVariable UUID id, @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update pricing policy status
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update pricing policy status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            PricingPolicy updatedPolicy = pricingPolicyService.updatePricingPolicyStatus(id, status);
            return ResponseEntity.ok(updatedPolicy);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update pricing policy status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update pricing policy status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePricingPolicy(@PathVariable UUID id) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa pricing policy
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete pricing policies");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            pricingPolicyService.deletePricingPolicy(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Pricing policy deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete pricing policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete pricing policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

