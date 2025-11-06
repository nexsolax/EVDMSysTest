package com.evdealer.controller;

import com.evdealer.entity.CustomerPayment;
import com.evdealer.service.CustomerPaymentService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/customer-payments")
@CrossOrigin(origins = "*")
@Tag(name = "Customer Payment Management", description = "APIs for managing customer payments")
public class CustomerPaymentController {
    
    @Autowired
    private CustomerPaymentService customerPaymentService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Get all customer payments", description = "Retrieve a list of all customer payments")
    public ResponseEntity<?> getAllCustomerPayments() {
        try {
            List<CustomerPayment> payments = customerPaymentService.getAllCustomerPayments();
            
            List<Map<String, Object>> paymentList = payments.stream().map(this::paymentToMap).collect(Collectors.toList());
            
            return ResponseEntity.ok(paymentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    private Map<String, Object> paymentToMap(CustomerPayment payment) {
        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("paymentId", payment.getPaymentId());
        paymentMap.put("paymentNumber", payment.getPaymentNumber());
        paymentMap.put("paymentDate", payment.getPaymentDate());
        paymentMap.put("amount", payment.getAmount());
        paymentMap.put("paymentType", payment.getPaymentType());
        paymentMap.put("paymentMethod", payment.getPaymentMethod());
        paymentMap.put("referenceNumber", payment.getReferenceNumber());
        paymentMap.put("status", payment.getStatus());
        paymentMap.put("notes", payment.getNotes());
        paymentMap.put("createdAt", payment.getCreatedAt());
        
        if (payment.getOrder() != null) {
            paymentMap.put("orderId", payment.getOrder().getOrderId());
        }
        if (payment.getCustomer() != null) {
            paymentMap.put("customerId", payment.getCustomer().getCustomerId());
        }
        if (payment.getProcessedBy() != null) {
            paymentMap.put("processedBy", payment.getProcessedBy().getUserId());
        }
        
        return paymentMap;
    }
    
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID", description = "Retrieve a specific customer payment by its ID")
    public ResponseEntity<?> getPaymentById(@PathVariable @Parameter(description = "Payment ID") UUID paymentId) {
        try {
            return customerPaymentService.getPaymentById(paymentId)
                    .map(payment -> ResponseEntity.ok(paymentToMap(payment)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/number/{paymentNumber}")
    @Operation(summary = "Get payment by number", description = "Retrieve a specific customer payment by its number")
    public ResponseEntity<?> getPaymentByNumber(@PathVariable String paymentNumber) {
        try {
            return customerPaymentService.getPaymentByNumber(paymentNumber)
                    .map(payment -> ResponseEntity.ok(paymentToMap(payment)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status", description = "Retrieve customer payments filtered by status")
    public ResponseEntity<?> getPaymentsByStatus(@PathVariable String status) {
        try {
            List<CustomerPayment> payments = customerPaymentService.getPaymentsByStatus(status);
            List<Map<String, Object>> paymentList = payments.stream().map(this::paymentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(paymentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get payments by customer", description = "Retrieve customer payments for a specific customer")
    public ResponseEntity<?> getPaymentsByCustomer(@PathVariable UUID customerId) {
        try {
            List<CustomerPayment> payments = customerPaymentService.getPaymentsByCustomer(customerId);
            List<Map<String, Object>> paymentList = payments.stream().map(this::paymentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(paymentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payments by order", description = "Retrieve customer payments for a specific order")
    public ResponseEntity<?> getPaymentsByOrder(@PathVariable UUID orderId) {
        try {
            List<CustomerPayment> payments = customerPaymentService.getPaymentsByOrder(orderId);
            List<Map<String, Object>> paymentList = payments.stream().map(this::paymentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(paymentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Get payments by date range", description = "Retrieve customer payments within a date range")
    public ResponseEntity<?> getPaymentsByDateRange(
            @RequestParam @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @Parameter(description = "End date") LocalDate endDate) {
        try {
            List<CustomerPayment> payments = customerPaymentService.getPaymentsByDateRange(startDate, endDate);
            List<Map<String, Object>> paymentList = payments.stream().map(this::paymentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(paymentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/type/{paymentType}")
    @Operation(summary = "Get payments by type", description = "Retrieve customer payments filtered by payment type")
    public ResponseEntity<?> getPaymentsByType(@PathVariable String paymentType) {
        try {
            List<CustomerPayment> payments = customerPaymentService.getPaymentsByType(paymentType);
            List<Map<String, Object>> paymentList = payments.stream().map(this::paymentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(paymentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/method/{paymentMethod}")
    @Operation(summary = "Get payments by method", description = "Retrieve customer payments filtered by payment method")
    public ResponseEntity<?> getPaymentsByMethod(@PathVariable String paymentMethod) {
        try {
            List<CustomerPayment> payments = customerPaymentService.getPaymentsByMethod(paymentMethod);
            List<Map<String, Object>> paymentList = payments.stream().map(this::paymentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(paymentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/processed-by/{userId}")
    @Operation(summary = "Get payments by processed by", description = "Retrieve customer payments processed by a specific user")
    public ResponseEntity<?> getPaymentsByProcessedBy(@PathVariable UUID userId) {
        try {
            List<CustomerPayment> payments = customerPaymentService.getPaymentsByProcessedBy(userId);
            List<Map<String, Object>> paymentList = payments.stream().map(this::paymentToMap).collect(Collectors.toList());
            return ResponseEntity.ok(paymentList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Create customer payment", description = "Create a new customer payment")
    public ResponseEntity<?> createCustomerPayment(@RequestBody CustomerPayment customerPayment) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo customer payment
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create customer payments");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            CustomerPayment createdPayment = customerPaymentService.createCustomerPayment(customerPayment);
            return ResponseEntity.status(HttpStatus.CREATED).body(paymentToMap(createdPayment));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create customer payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create customer payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{paymentId}")
    @Operation(summary = "Update customer payment", description = "Update an existing customer payment")
    public ResponseEntity<?> updateCustomerPayment(
            @PathVariable UUID paymentId, 
            @RequestBody CustomerPayment customerPaymentDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update customer payment
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update customer payments");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            CustomerPayment updatedPayment = customerPaymentService.updateCustomerPayment(paymentId, customerPaymentDetails);
            return ResponseEntity.ok(paymentToMap(updatedPayment));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update customer payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update customer payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{paymentId}/status")
    @Operation(summary = "Update payment status", description = "Update the status of a customer payment")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable UUID paymentId, 
            @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update payment status
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update payment status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            CustomerPayment updatedPayment = customerPaymentService.updatePaymentStatus(paymentId, status);
            return ResponseEntity.ok(paymentToMap(updatedPayment));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update payment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update payment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{paymentId}")
    @Operation(summary = "Delete customer payment", description = "Delete a customer payment")
    public ResponseEntity<?> deleteCustomerPayment(@PathVariable UUID paymentId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa customer payment
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete customer payments");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            customerPaymentService.deleteCustomerPayment(paymentId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Customer payment deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete customer payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete customer payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
