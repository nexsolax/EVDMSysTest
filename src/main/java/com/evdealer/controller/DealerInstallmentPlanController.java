package com.evdealer.controller;

import com.evdealer.entity.DealerInstallmentPlan;
import com.evdealer.service.DealerInstallmentPlanService;
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
@RequestMapping("/api/dealer-installment-plans")
@CrossOrigin(origins = "*")
@Tag(name = "Dealer Installment Plan Management", description = "APIs quản lý kế hoạch trả góp đại lý")
public class DealerInstallmentPlanController {
    
    @Autowired
    private DealerInstallmentPlanService dealerInstallmentPlanService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy tất cả kế hoạch trả góp đại lý", description = "Lấy danh sách tất cả kế hoạch trả góp đại lý")
    public ResponseEntity<?> getAllDealerInstallmentPlans() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerInstallmentPlan> plans = dealerInstallmentPlanService.getAllDealerInstallmentPlans();
            List<Map<String, Object>> planList = plans.stream()
                .map(this::planToMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(planList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve dealer installment plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{planId}")
    @Operation(summary = "Lấy kế hoạch trả góp theo ID", description = "Lấy thông tin chi tiết của một kế hoạch trả góp đại lý")
    public ResponseEntity<?> getDealerInstallmentPlanById(@PathVariable UUID planId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            return dealerInstallmentPlanService.getDealerInstallmentPlanById(planId)
                .map(plan -> ResponseEntity.ok(planToMap(plan)))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve dealer installment plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/contract/{contractNumber}")
    @Operation(summary = "Lấy kế hoạch trả góp theo số hợp đồng", description = "Lấy thông tin kế hoạch trả góp theo số hợp đồng")
    public ResponseEntity<?> getDealerInstallmentPlanByContractNumber(@PathVariable String contractNumber) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            return dealerInstallmentPlanService.getDealerInstallmentPlanByContractNumber(contractNumber)
                .map(plan -> ResponseEntity.ok(planToMap(plan)))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve dealer installment plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "Lấy kế hoạch trả góp theo invoice", description = "Lấy danh sách kế hoạch trả góp cho một invoice cụ thể")
    public ResponseEntity<?> getDealerInstallmentPlansByInvoice(@PathVariable UUID invoiceId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerInstallmentPlan> plans = dealerInstallmentPlanService.getDealerInstallmentPlansByInvoice(invoiceId);
            List<Map<String, Object>> planList = plans.stream()
                .map(this::planToMap)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(planList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve dealer installment plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Tạo kế hoạch trả góp đại lý mới", description = "Tạo một kế hoạch trả góp đại lý mới")
    public ResponseEntity<?> createDealerInstallmentPlan(@RequestBody DealerInstallmentPlan plan) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: DEALER_MANAGER, DEALER_STAFF, ADMIN
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "DEALER_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only dealer users or admin can create dealer installment plans");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerInstallmentPlan createdPlan = dealerInstallmentPlanService.createDealerInstallmentPlan(plan);
            return ResponseEntity.status(HttpStatus.CREATED).body(planToMap(createdPlan));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{planId}")
    @Operation(summary = "Cập nhật kế hoạch trả góp đại lý", description = "Cập nhật thông tin của một kế hoạch trả góp đại lý")
    public ResponseEntity<?> updateDealerInstallmentPlan(
            @PathVariable UUID planId,
            @RequestBody DealerInstallmentPlan planDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: DEALER_MANAGER, DEALER_STAFF, ADMIN
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "DEALER_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only dealer users or admin can update dealer installment plans");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerInstallmentPlan updatedPlan = dealerInstallmentPlanService.updateDealerInstallmentPlan(planId, planDetails);
            return ResponseEntity.ok(planToMap(updatedPlan));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{planId}")
    @Operation(summary = "Xóa kế hoạch trả góp đại lý", description = "Xóa một kế hoạch trả góp đại lý")
    public ResponseEntity<?> deleteDealerInstallmentPlan(@PathVariable UUID planId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete dealer installment plans");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            dealerInstallmentPlanService.deleteDealerInstallmentPlan(planId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Dealer installment plan deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    private Map<String, Object> planToMap(DealerInstallmentPlan plan) {
        Map<String, Object> map = new HashMap<>();
        map.put("planId", plan.getPlanId());
        map.put("totalAmount", plan.getTotalAmount());
        map.put("downPaymentAmount", plan.getDownPaymentAmount());
        map.put("loanAmount", plan.getLoanAmount());
        map.put("interestRate", plan.getInterestRate());
        map.put("loanTermMonths", plan.getLoanTermMonths());
        map.put("monthlyPaymentAmount", plan.getMonthlyPaymentAmount());
        map.put("firstPaymentDate", plan.getFirstPaymentDate());
        map.put("lastPaymentDate", plan.getLastPaymentDate());
        
        // Map enum values safely with try-catch fallback
        try {
            map.put("planStatus", plan.getPlanStatus() != null ? plan.getPlanStatus().getValue() : null);
        } catch (Exception e) {
            map.put("planStatus", plan.getPlanStatus() != null ? plan.getPlanStatus().name() : null);
        }
        
        map.put("financeCompany", plan.getFinanceCompany());
        map.put("contractNumber", plan.getContractNumber());
        map.put("createdAt", plan.getCreatedAt());
        
        if (plan.getInvoice() != null) {
            map.put("invoiceId", plan.getInvoice().getInvoiceId());
        }
        
        return map;
    }
}

