package com.evdealer.controller;

import com.evdealer.entity.InstallmentPlan;
import com.evdealer.enums.PlanType;
import com.evdealer.entity.DealerInvoice;
import com.evdealer.service.InstallmentPlanService;
import com.evdealer.service.DealerInvoiceService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/installment-plans")
@CrossOrigin(origins = "*")
@Tag(name = "Installment Plan Management", description = "APIs for managing installment plans")
public class InstallmentPlanController {
    
    @Autowired
    private InstallmentPlanService installmentPlanService;
    
    @Autowired
    private DealerInvoiceService dealerInvoiceService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    private Map<String, Object> planToMap(InstallmentPlan plan) {
        Map<String, Object> map = new HashMap<>();
        map.put("planId", plan.getPlanId());
        map.put("planType", plan.getPlanType());
        map.put("totalAmount", plan.getTotalAmount());
        map.put("downPaymentAmount", plan.getDownPaymentAmount());
        map.put("loanAmount", plan.getLoanAmount());
        map.put("interestRate", plan.getInterestRate());
        map.put("loanTermMonths", plan.getLoanTermMonths());
        map.put("monthlyPaymentAmount", plan.getMonthlyPaymentAmount());
        map.put("firstPaymentDate", plan.getFirstPaymentDate());
        map.put("lastPaymentDate", plan.getLastPaymentDate());
        map.put("planStatus", plan.getPlanStatus());
        map.put("financeCompany", plan.getFinanceCompany());
        map.put("contractNumber", plan.getContractNumber());
        map.put("createdAt", plan.getCreatedAt());
        if (plan.getOrder() != null) {
            map.put("orderId", plan.getOrder().getOrderId());
        }
        if (plan.getCustomer() != null) {
            map.put("customerId", plan.getCustomer().getCustomerId());
        }
        if (plan.getInvoice() != null) {
            map.put("invoiceId", plan.getInvoice().getInvoiceId());
        }
        if (plan.getDealer() != null) {
            map.put("dealerId", plan.getDealer().getDealerId());
        }
        return map;
    }
    
    @GetMapping
    @Operation(summary = "Get all installment plans", description = "Retrieve a list of all installment plans")
    public ResponseEntity<?> getAllInstallmentPlans() {
        try {
            List<InstallmentPlan> installmentPlans = installmentPlanService.getAllInstallmentPlans();
            List<Map<String, Object>> planList = installmentPlans.stream().map(this::planToMap).collect(Collectors.toList());
            return ResponseEntity.ok(planList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve installment plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{planId}")
    @Operation(summary = "Get installment plan by ID", description = "Retrieve a specific installment plan by its ID")
    public ResponseEntity<?> getInstallmentPlanById(@PathVariable @Parameter(description = "Plan ID") UUID planId) {
        try {
            return installmentPlanService.getInstallmentPlanById(planId)
                    .map(plan -> ResponseEntity.ok(planToMap(plan)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve installment plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/contract/{contractNumber}")
    @Operation(summary = "Get installment plan by contract number", description = "Retrieve a specific installment plan by its contract number")
    public ResponseEntity<?> getInstallmentPlanByContractNumber(@PathVariable String contractNumber) {
        try {
            return installmentPlanService.getInstallmentPlanByContractNumber(contractNumber)
                    .map(plan -> ResponseEntity.ok(planToMap(plan)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve installment plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get installment plans by status", description = "Retrieve installment plans filtered by status")
    public ResponseEntity<?> getInstallmentPlansByStatus(@PathVariable String status) {
        try {
            // Validate và convert status string to enum
            com.evdealer.enums.InstallmentPlanStatus statusEnum = com.evdealer.enums.InstallmentPlanStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.InstallmentPlanStatus.values())
                    .map(com.evdealer.enums.InstallmentPlanStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByStatus(statusEnum.getValue());
            List<Map<String, Object>> planList = installmentPlans.stream().map(this::planToMap).collect(Collectors.toList());
            return ResponseEntity.ok(planList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve installment plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get installment plans by customer", description = "Retrieve installment plans for a specific customer")
    public ResponseEntity<?> getInstallmentPlansByCustomer(@PathVariable UUID customerId) {
        try {
            List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByCustomer(customerId);
            List<Map<String, Object>> planList = installmentPlans.stream().map(this::planToMap).collect(Collectors.toList());
            return ResponseEntity.ok(planList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve installment plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get installment plans by order", description = "Retrieve installment plans for a specific order")
    public ResponseEntity<?> getInstallmentPlansByOrder(@PathVariable UUID orderId) {
        try {
            List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByOrder(orderId);
            List<Map<String, Object>> planList = installmentPlans.stream().map(this::planToMap).collect(Collectors.toList());
            return ResponseEntity.ok(planList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve installment plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/finance-company/{financeCompany}")
    @Operation(summary = "Get installment plans by finance company", description = "Retrieve installment plans from a specific finance company")
    public ResponseEntity<?> getInstallmentPlansByFinanceCompany(@PathVariable String financeCompany) {
        try {
            List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByFinanceCompany(financeCompany);
            List<Map<String, Object>> planList = installmentPlans.stream().map(this::planToMap).collect(Collectors.toList());
            return ResponseEntity.ok(planList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve installment plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "Get installment plans by invoice", description = "Retrieve installment plans for a specific dealer invoice")
    public ResponseEntity<?> getInstallmentPlansByInvoice(@PathVariable UUID invoiceId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem plans của invoice của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    DealerInvoice invoice = dealerInvoiceService.getInvoiceById(invoiceId)
                        .orElseThrow(() -> new RuntimeException("Invoice not found"));
                    if (invoice.getDealerOrder() != null && invoice.getDealerOrder().getDealer() != null) {
                        UUID invoiceDealerId = invoice.getDealerOrder().getDealer().getDealerId();
                        if (!invoiceDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view plans for invoices of your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByInvoice(invoiceId);
            List<Map<String, Object>> planList = installmentPlans.stream().map(this::planToMap).collect(Collectors.toList());
            return ResponseEntity.ok(planList);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "Get installment plans by dealer", description = "Retrieve installment plans for a specific dealer")
    public ResponseEntity<?> getInstallmentPlansByDealer(@PathVariable UUID dealerId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem plans của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view plans for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByDealer(dealerId);
            List<Map<String, Object>> planList = installmentPlans.stream().map(this::planToMap).collect(Collectors.toList());
            return ResponseEntity.ok(planList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/plan-type/{planType}")
    @Operation(summary = "Get installment plans by plan type", description = "Retrieve installment plans filtered by plan type")
    public ResponseEntity<?> getInstallmentPlansByPlanType(@PathVariable String planType) {
        try {
            List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByPlanType(planType);
            List<Map<String, Object>> planList = installmentPlans.stream().map(this::planToMap).collect(Collectors.toList());
            return ResponseEntity.ok(planList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve installment plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/customer-plans")
    @Operation(summary = "Get customer installment plans", description = "Retrieve all customer installment plans")
    public ResponseEntity<?> getCustomerInstallmentPlans() {
        try {
            List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByPlanType("customer");
            List<Map<String, Object>> planList = installmentPlans.stream().map(this::planToMap).collect(Collectors.toList());
            return ResponseEntity.ok(planList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve installment plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/dealer-plans")
    @Operation(summary = "Get dealer installment plans", description = "Retrieve all dealer installment plans")
    public ResponseEntity<?> getDealerInstallmentPlans() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByPlanType("dealer");
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    installmentPlans = installmentPlans.stream()
                        .filter(plan -> plan.getDealer() != null && plan.getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            List<Map<String, Object>> planList = installmentPlans.stream().map(this::planToMap).collect(Collectors.toList());
            return ResponseEntity.ok(planList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get dealer plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Create installment plan", description = "Create a new installment plan")
    public ResponseEntity<?> createInstallmentPlan(@RequestBody InstallmentPlan installmentPlan) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: DEALER_MANAGER, DEALER_STAFF, ADMIN (cho dealer plans)
            // Hoặc EVM_STAFF, ADMIN (cho customer plans)
            PlanType planTypeEnum = installmentPlan.getPlanType() != null ? PlanType.fromString(installmentPlan.getPlanType()) : PlanType.CUSTOMER;
            if (planTypeEnum == PlanType.DEALER) {
                // Dealer installment plan
                if (!securityUtils.hasAnyRole("DEALER_MANAGER", "DEALER_STAFF", "ADMIN")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only dealer users or admin can create dealer installment plans");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
                
                // Kiểm tra dealer user chỉ có thể tạo plan cho invoice của dealer mình
                if (securityUtils.isDealerUser() && !securityUtils.isAdmin() && installmentPlan.getInvoice() != null) {
                    var currentUser = securityUtils.getCurrentUser()
                        .orElseThrow(() -> new RuntimeException("User not authenticated"));
                    if (currentUser.getDealer() != null) {
                        UUID userDealerId = currentUser.getDealer().getDealerId();
                        DealerInvoice invoice = dealerInvoiceService.getInvoiceById(installmentPlan.getInvoice().getInvoiceId())
                            .orElseThrow(() -> new RuntimeException("Invoice not found"));
                        if (invoice.getDealerOrder() != null && invoice.getDealerOrder().getDealer() != null) {
                            UUID invoiceDealerId = invoice.getDealerOrder().getDealer().getDealerId();
                            if (!invoiceDealerId.equals(userDealerId)) {
                                Map<String, String> error = new HashMap<>();
                                error.put("error", "Access denied. You can only create plans for invoices of your own dealer");
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                            }
                        }
                    }
                }
            } else {
                // Customer installment plan - chỉ EVM_STAFF hoặc ADMIN
                if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only EVM staff or admin can create customer installment plans");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            InstallmentPlan createdPlan = installmentPlanService.createInstallmentPlan(installmentPlan);
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
    @Operation(summary = "Update installment plan", description = "Update an existing installment plan")
    public ResponseEntity<?> updateInstallmentPlan(
            @PathVariable UUID planId, 
            @RequestBody InstallmentPlan installmentPlanDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Lấy plan hiện tại để kiểm tra
            InstallmentPlan existingPlan = installmentPlanService.getInstallmentPlanById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
            
            // Kiểm tra phân quyền và ownership
            PlanType existingPlanType = existingPlan.getPlanType() != null ? PlanType.fromString(existingPlan.getPlanType()) : PlanType.CUSTOMER;
            if (existingPlanType == PlanType.DEALER) {
                // Dealer installment plan
                if (!securityUtils.hasAnyRole("DEALER_MANAGER", "DEALER_STAFF", "ADMIN")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only dealer users or admin can update dealer installment plans");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
                
                // Kiểm tra dealer user chỉ có thể update plan của dealer mình
                if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                    var currentUser = securityUtils.getCurrentUser()
                        .orElseThrow(() -> new RuntimeException("User not authenticated"));
                    if (currentUser.getDealer() != null) {
                        UUID userDealerId = currentUser.getDealer().getDealerId();
                        if (existingPlan.getDealer() != null && !existingPlan.getDealer().getDealerId().equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only update plans for your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            } else {
                // Customer installment plan - chỉ EVM_STAFF hoặc ADMIN
                if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only EVM staff or admin can update customer installment plans");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            InstallmentPlan updatedPlan = installmentPlanService.updateInstallmentPlan(planId, installmentPlanDetails);
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
    
    @PutMapping("/{planId}/status")
    @Operation(summary = "Update installment plan status", description = "Update the status of an installment plan")
    public ResponseEntity<?> updateInstallmentPlanStatus(
            @PathVariable UUID planId, 
            @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Lấy plan hiện tại để kiểm tra
            InstallmentPlan existingPlan = installmentPlanService.getInstallmentPlanById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
            
            // Kiểm tra phân quyền và ownership
            PlanType existingPlanType = existingPlan.getPlanType() != null ? PlanType.fromString(existingPlan.getPlanType()) : PlanType.CUSTOMER;
            if (existingPlanType == PlanType.DEALER) {
                // Dealer installment plan
                if (!securityUtils.hasAnyRole("DEALER_MANAGER", "DEALER_STAFF", "ADMIN")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only dealer users or admin can update dealer installment plan status");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
                
                // Kiểm tra dealer user chỉ có thể update status của plan của dealer mình
                if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                    var currentUser = securityUtils.getCurrentUser()
                        .orElseThrow(() -> new RuntimeException("User not authenticated"));
                    if (currentUser.getDealer() != null) {
                        UUID userDealerId = currentUser.getDealer().getDealerId();
                        if (existingPlan.getDealer() != null && !existingPlan.getDealer().getDealerId().equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only update status of plans for your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            } else {
                // Customer installment plan - chỉ EVM_STAFF hoặc ADMIN
                if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only EVM staff or admin can update customer installment plan status");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            // Validate và convert status string to enum
            com.evdealer.enums.InstallmentPlanStatus statusEnum = com.evdealer.enums.InstallmentPlanStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.InstallmentPlanStatus.values())
                    .map(com.evdealer.enums.InstallmentPlanStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            InstallmentPlan updatedPlan = installmentPlanService.updateInstallmentPlanStatus(planId, statusEnum.getValue());
            return ResponseEntity.ok(planToMap(updatedPlan));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update plan status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update plan status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{planId}")
    @Operation(summary = "Delete installment plan", description = "Delete an installment plan")
    public ResponseEntity<?> deleteInstallmentPlan(@PathVariable UUID planId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa installment plan
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete installment plans");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            installmentPlanService.deleteInstallmentPlan(planId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Installment plan deleted successfully");
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
}
