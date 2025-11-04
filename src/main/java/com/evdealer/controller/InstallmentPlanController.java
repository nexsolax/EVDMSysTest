package com.evdealer.controller;

import com.evdealer.entity.InstallmentPlan;
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
    
    @GetMapping
    @Operation(summary = "Get all installment plans", description = "Retrieve a list of all installment plans")
    public ResponseEntity<List<InstallmentPlan>> getAllInstallmentPlans() {
        List<InstallmentPlan> installmentPlans = installmentPlanService.getAllInstallmentPlans();
        return ResponseEntity.ok(installmentPlans);
    }
    
    @GetMapping("/{planId}")
    @Operation(summary = "Get installment plan by ID", description = "Retrieve a specific installment plan by its ID")
    public ResponseEntity<InstallmentPlan> getInstallmentPlanById(@PathVariable @Parameter(description = "Plan ID") UUID planId) {
        return installmentPlanService.getInstallmentPlanById(planId)
                .map(plan -> ResponseEntity.ok(plan))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/contract/{contractNumber}")
    @Operation(summary = "Get installment plan by contract number", description = "Retrieve a specific installment plan by its contract number")
    public ResponseEntity<InstallmentPlan> getInstallmentPlanByContractNumber(@PathVariable String contractNumber) {
        return installmentPlanService.getInstallmentPlanByContractNumber(contractNumber)
                .map(plan -> ResponseEntity.ok(plan))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get installment plans by status", description = "Retrieve installment plans filtered by status")
    public ResponseEntity<List<InstallmentPlan>> getInstallmentPlansByStatus(@PathVariable String status) {
        List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByStatus(status);
        return ResponseEntity.ok(installmentPlans);
    }
    
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get installment plans by customer", description = "Retrieve installment plans for a specific customer")
    public ResponseEntity<List<InstallmentPlan>> getInstallmentPlansByCustomer(@PathVariable UUID customerId) {
        List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByCustomer(customerId);
        return ResponseEntity.ok(installmentPlans);
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get installment plans by order", description = "Retrieve installment plans for a specific order")
    public ResponseEntity<List<InstallmentPlan>> getInstallmentPlansByOrder(@PathVariable UUID orderId) {
        List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByOrder(orderId);
        return ResponseEntity.ok(installmentPlans);
    }
    
    @GetMapping("/finance-company/{financeCompany}")
    @Operation(summary = "Get installment plans by finance company", description = "Retrieve installment plans from a specific finance company")
    public ResponseEntity<List<InstallmentPlan>> getInstallmentPlansByFinanceCompany(@PathVariable String financeCompany) {
        List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByFinanceCompany(financeCompany);
        return ResponseEntity.ok(installmentPlans);
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
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
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
            return ResponseEntity.ok(installmentPlans);
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
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view plans for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByDealer(dealerId);
            return ResponseEntity.ok(installmentPlans);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get plans: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/plan-type/{planType}")
    @Operation(summary = "Get installment plans by plan type", description = "Retrieve installment plans filtered by plan type")
    public ResponseEntity<List<InstallmentPlan>> getInstallmentPlansByPlanType(@PathVariable String planType) {
        List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByPlanType(planType);
        return ResponseEntity.ok(installmentPlans);
    }
    
    @GetMapping("/customer-plans")
    @Operation(summary = "Get customer installment plans", description = "Retrieve all customer installment plans")
    public ResponseEntity<List<InstallmentPlan>> getCustomerInstallmentPlans() {
        List<InstallmentPlan> installmentPlans = installmentPlanService.getInstallmentPlansByPlanType("customer");
        return ResponseEntity.ok(installmentPlans);
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
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    installmentPlans = installmentPlans.stream()
                        .filter(plan -> plan.getDealer() != null && plan.getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(installmentPlans);
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
            if (installmentPlan.getPlanType() != null && "dealer".equalsIgnoreCase(installmentPlan.getPlanType())) {
                // Dealer installment plan
                if (!securityUtils.hasAnyRole("DEALER_MANAGER", "DEALER_STAFF", "ADMIN")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only dealer users or admin can create dealer installment plans");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
                
                // Kiểm tra dealer user chỉ có thể tạo plan cho invoice của dealer mình
                if (securityUtils.isDealerUser() && !securityUtils.isAdmin() && installmentPlan.getInvoice() != null) {
                    var currentUserOpt = securityUtils.getCurrentUser();
                    if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                        UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
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
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan);
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
            if (existingPlan.getPlanType() != null && "dealer".equalsIgnoreCase(existingPlan.getPlanType())) {
                // Dealer installment plan
                if (!securityUtils.hasAnyRole("DEALER_MANAGER", "DEALER_STAFF", "ADMIN")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only dealer users or admin can update dealer installment plans");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
                
                // Kiểm tra dealer user chỉ có thể update plan của dealer mình
                if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                    var currentUserOpt = securityUtils.getCurrentUser();
                    if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                        UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
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
            return ResponseEntity.ok(updatedPlan);
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
            if (existingPlan.getPlanType() != null && "dealer".equalsIgnoreCase(existingPlan.getPlanType())) {
                // Dealer installment plan
                if (!securityUtils.hasAnyRole("DEALER_MANAGER", "DEALER_STAFF", "ADMIN")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only dealer users or admin can update dealer installment plan status");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
                
                // Kiểm tra dealer user chỉ có thể update status của plan của dealer mình
                if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                    var currentUserOpt = securityUtils.getCurrentUser();
                    if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                        UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
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
            
            InstallmentPlan updatedPlan = installmentPlanService.updateInstallmentPlanStatus(planId, status);
            return ResponseEntity.ok(updatedPlan);
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
