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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pricing-policies")
@CrossOrigin(origins = "*")
public class PricingPolicyController {
    
    @Autowired
    private PricingPolicyService pricingPolicyService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    private Map<String, Object> policyToMap(PricingPolicy policy) {
        Map<String, Object> map = new HashMap<>();
        map.put("policyId", policy.getPolicyId());
        map.put("policyName", policy.getPolicyName());
        map.put("description", policy.getDescription());
        map.put("policyType", policy.getPolicyType());
        map.put("basePrice", policy.getBasePrice());
        map.put("discountPercent", policy.getDiscountPercent());
        map.put("discountAmount", policy.getDiscountAmount());
        map.put("markupPercent", policy.getMarkupPercent());
        map.put("markupAmount", policy.getMarkupAmount());
        map.put("effectiveDate", policy.getEffectiveDate());
        map.put("expiryDate", policy.getExpiryDate());
        map.put("minQuantity", policy.getMinQuantity());
        map.put("maxQuantity", policy.getMaxQuantity());
        map.put("customerType", policy.getCustomerType());
        map.put("region", policy.getRegion());
        map.put("scope", policy.getScope());
        map.put("status", policy.getStatus());
        map.put("priority", policy.getPriority());
        map.put("createdAt", policy.getCreatedAt());
        map.put("updatedAt", policy.getUpdatedAt());
        if (policy.getVariant() != null) {
            map.put("variantId", policy.getVariant().getVariantId());
        }
        if (policy.getDealer() != null) {
            map.put("dealerId", policy.getDealer().getDealerId());
        }
        return map;
    }
    
    @GetMapping
    public ResponseEntity<?> getAllPricingPolicies() {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getAllPricingPolicies();
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getPricingPolicyById(@PathVariable UUID id) {
        try {
            return pricingPolicyService.getPricingPolicyById(id)
                    .map(policy -> ResponseEntity.ok(policyToMap(policy)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<?> getPricingPoliciesByVariant(@PathVariable Integer variantId) {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByVariant(variantId);
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPricingPoliciesByStatus(@PathVariable String status) {
        try {
            // Validate và convert status string to enum
            com.evdealer.enums.PricingPolicyStatus statusEnum = com.evdealer.enums.PricingPolicyStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.PricingPolicyStatus.values())
                    .map(com.evdealer.enums.PricingPolicyStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByStatus(statusEnum.getValue());
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/type/{policyType}")
    public ResponseEntity<?> getPricingPoliciesByType(@PathVariable String policyType) {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByType(policyType);
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/customer-type/{customerType}")
    public ResponseEntity<?> getPricingPoliciesByCustomerType(@PathVariable String customerType) {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByCustomerType(customerType);
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/region/{region}")
    public ResponseEntity<?> getPricingPoliciesByRegion(@PathVariable String region) {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByRegion(region);
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view pricing policies for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByDealer(dealerId);
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/scope/{scope}")
    public ResponseEntity<?> getPricingPoliciesByScope(@PathVariable String scope) {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByScope(scope);
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/global")
    public ResponseEntity<?> getGlobalPricingPolicies() {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByScope("global");
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/dealer-specific")
    public ResponseEntity<?> getDealerSpecificPricingPolicies() {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByScope("dealer");
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<?> getActivePricingPolicies() {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getActivePricingPoliciesByDate(LocalDate.now());
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/active/date/{date}")
    public ResponseEntity<?> getActivePricingPoliciesByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getActivePricingPoliciesByDate(date);
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/active/variant/{variantId}")
    public ResponseEntity<?> getActivePricingPoliciesByVariant(@PathVariable Integer variantId) {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getActivePricingPoliciesByVariantAndDate(variantId, LocalDate.now());
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/active/variant/{variantId}/date/{date}")
    public ResponseEntity<?> getActivePricingPoliciesByVariantAndDate(
            @PathVariable Integer variantId, 
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getActivePricingPoliciesByVariantAndDate(variantId, date);
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/active/variant/{variantId}/customer-type/{customerType}")
    public ResponseEntity<?> getActivePricingPoliciesByVariantAndCustomerType(
            @PathVariable Integer variantId, 
            @PathVariable String customerType) {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getActivePricingPoliciesByVariantCustomerTypeAndDate(variantId, customerType, LocalDate.now());
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> getPricingPoliciesByName(@RequestParam String policyName) {
        try {
            List<PricingPolicy> policies = pricingPolicyService.getPricingPoliciesByName(policyName);
            List<Map<String, Object>> policyList = policies.stream().map(this::policyToMap).collect(Collectors.toList());
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve pricing policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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
            return ResponseEntity.status(HttpStatus.CREATED).body(policyToMap(createdPolicy));
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
            return ResponseEntity.ok(policyToMap(updatedPolicy));
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
            
            // Validate và convert status string to enum
            com.evdealer.enums.PricingPolicyStatus statusEnum = com.evdealer.enums.PricingPolicyStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.PricingPolicyStatus.values())
                    .map(com.evdealer.enums.PricingPolicyStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            PricingPolicy updatedPolicy = pricingPolicyService.updatePricingPolicyStatus(id, statusEnum.getValue());
            return ResponseEntity.ok(policyToMap(updatedPolicy));
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

