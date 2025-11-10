package com.evdealer.controller;

import com.evdealer.entity.DealerDiscountPolicy;
import com.evdealer.service.DealerDiscountPolicyService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dealer-discount-policies")
@CrossOrigin(origins = "*")
@Tag(name = "Dealer Discount Policy Management", description = "APIs quản lý chính sách giảm giá đại lý")
public class DealerDiscountPolicyController {
    
    @Autowired
    private DealerDiscountPolicyService dealerDiscountPolicyService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy tất cả chính sách giảm giá", description = "Lấy danh sách tất cả chính sách giảm giá đại lý")
    public ResponseEntity<?> getAllPolicies() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerDiscountPolicy> policies = dealerDiscountPolicyService.getAllPolicies();
            List<Map<String, Object>> policyList = policies.stream()
                    .map(this::policyToMap)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{policyId}")
    @Operation(summary = "Lấy chính sách giảm giá theo ID", description = "Lấy thông tin chi tiết của một chính sách giảm giá")
    public ResponseEntity<?> getPolicyById(@PathVariable UUID policyId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            return dealerDiscountPolicyService.getPolicyById(policyId)
                    .map(policy -> ResponseEntity.ok(policyToMap(policy)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/variant/{variantId}")
    @Operation(summary = "Lấy chính sách giảm giá theo variant", description = "Lấy danh sách chính sách giảm giá cho một variant cụ thể")
    public ResponseEntity<?> getPoliciesByVariantId(@PathVariable Integer variantId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerDiscountPolicy> policies = dealerDiscountPolicyService.getPoliciesByVariantId(variantId);
            List<Map<String, Object>> policyList = policies.stream()
                    .map(this::policyToMap)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Lấy chính sách giảm giá theo trạng thái", description = "Lấy danh sách chính sách giảm giá theo trạng thái")
    public ResponseEntity<?> getPoliciesByStatus(@PathVariable String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerDiscountPolicy> policies = dealerDiscountPolicyService.getPoliciesByStatus(status);
            List<Map<String, Object>> policyList = policies.stream()
                    .map(this::policyToMap)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(policyList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve policies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Tạo chính sách giảm giá mới", description = "Tạo một chính sách giảm giá mới")
    public ResponseEntity<?> createPolicy(@RequestBody DealerDiscountPolicy policy) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo policy
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create discount policies");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerDiscountPolicy createdPolicy = dealerDiscountPolicyService.createPolicy(policy);
            return ResponseEntity.status(HttpStatus.CREATED).body(policyToMap(createdPolicy));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{policyId}")
    @Operation(summary = "Cập nhật chính sách giảm giá", description = "Cập nhật thông tin của một chính sách giảm giá")
    public ResponseEntity<?> updatePolicy(@PathVariable UUID policyId, @RequestBody DealerDiscountPolicy policyDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update policy
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update discount policies");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerDiscountPolicy updatedPolicy = dealerDiscountPolicyService.updatePolicy(policyId, policyDetails);
            return ResponseEntity.ok(policyToMap(updatedPolicy));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{policyId}")
    @Operation(summary = "Xóa chính sách giảm giá", description = "Xóa một chính sách giảm giá")
    public ResponseEntity<?> deletePolicy(@PathVariable UUID policyId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa policy
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete discount policies");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            dealerDiscountPolicyService.deletePolicy(policyId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Discount policy deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete policy: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    private Map<String, Object> policyToMap(DealerDiscountPolicy policy) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("policyId", policy.getPolicyId());
            map.put("policyName", policy.getPolicyName());
            map.put("description", policy.getDescription());
            map.put("discountPercent", policy.getDiscountPercent());
            map.put("discountAmount", policy.getDiscountAmount());
            map.put("startDate", policy.getStartDate());
            map.put("endDate", policy.getEndDate());
            map.put("status", policy.getStatus());
            map.put("createdAt", policy.getCreatedAt());
            map.put("updatedAt", policy.getUpdatedAt());
            
            if (policy.getVariant() != null) {
                try {
                    Map<String, Object> variantMap = new HashMap<>();
                    variantMap.put("variantId", policy.getVariant().getVariantId());
                    variantMap.put("variantName", policy.getVariant().getVariantName());
                    map.put("variant", variantMap);
                } catch (Exception e) {
                    map.put("variant", null);
                }
            } else {
                map.put("variant", null);
            }
        } catch (Exception e) {
            // Return partial data on error
        }
        return map;
    }
}

