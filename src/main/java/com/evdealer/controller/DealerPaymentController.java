package com.evdealer.controller;

import com.evdealer.entity.DealerPayment;
import com.evdealer.entity.DealerInvoice;
import com.evdealer.enums.DealerPaymentStatus;
import com.evdealer.enums.DealerInvoiceStatus;
import com.evdealer.service.DealerPaymentService;
import com.evdealer.service.DealerInvoiceService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dealer-payments")
@CrossOrigin(origins = "*")
@Tag(name = "Dealer Payment Management", description = "APIs for managing dealer payments")
public class DealerPaymentController {
    
    @Autowired
    private DealerPaymentService dealerPaymentService;
    
    @Autowired
    private DealerInvoiceService dealerInvoiceService;
    
    @Autowired
    private com.evdealer.service.VehicleDeliveryService vehicleDeliveryService;
    
    @Autowired
    private com.evdealer.service.DealerOrderService dealerOrderService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Get all dealer payments", description = "Retrieve a list of all dealer payments")
    public ResponseEntity<?> getAllDealerPayments() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerPayment> payments = dealerPaymentService.getAllDealerPayments();
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    payments = payments.stream()
                        .filter(payment -> payment.getInvoice() != null
                            && payment.getInvoice().getDealerOrder() != null
                            && payment.getInvoice().getDealerOrder().getDealer() != null
                            && payment.getInvoice().getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get dealer payment by ID", description = "Retrieve a specific dealer payment by its ID")
    public ResponseEntity<?> getPaymentById(@PathVariable @Parameter(description = "Payment ID") UUID paymentId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerPayment payment = dealerPaymentService.getPaymentById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            // Kiểm tra dealer user chỉ có thể xem payment của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (payment.getInvoice() != null && payment.getInvoice().getDealerOrder() != null 
                        && payment.getInvoice().getDealerOrder().getDealer() != null) {
                        UUID paymentDealerId = payment.getInvoice().getDealerOrder().getDealer().getDealerId();
                        if (!paymentDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view payments for your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/number/{paymentNumber}")
    @Operation(summary = "Get dealer payment by number", description = "Retrieve a specific dealer payment by its number")
    public ResponseEntity<?> getPaymentByNumber(@PathVariable String paymentNumber) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerPayment payment = dealerPaymentService.getPaymentByNumber(paymentNumber)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            // Kiểm tra dealer user chỉ có thể xem payment của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (payment.getInvoice() != null && payment.getInvoice().getDealerOrder() != null 
                        && payment.getInvoice().getDealerOrder().getDealer() != null) {
                        UUID paymentDealerId = payment.getInvoice().getDealerOrder().getDealer().getDealerId();
                        if (!paymentDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view payments for your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status", description = "Retrieve dealer payments filtered by status")
    public ResponseEntity<?> getPaymentsByStatus(@PathVariable String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Validate và convert status string to enum
            com.evdealer.enums.DealerPaymentStatus statusEnum = com.evdealer.enums.DealerPaymentStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.DealerPaymentStatus.values())
                    .map(com.evdealer.enums.DealerPaymentStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<DealerPayment> payments = dealerPaymentService.getPaymentsByStatus(statusEnum.getValue());
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    payments = payments.stream()
                        .filter(payment -> payment.getInvoice() != null
                            && payment.getInvoice().getDealerOrder() != null
                            && payment.getInvoice().getDealerOrder().getDealer() != null
                            && payment.getInvoice().getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Get payments by date range", description = "Retrieve dealer payments within a date range")
    public ResponseEntity<?> getPaymentsByDateRange(
            @RequestParam @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @Parameter(description = "End date") LocalDate endDate) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerPayment> payments = dealerPaymentService.getPaymentsByDateRange(startDate, endDate);
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    payments = payments.stream()
                        .filter(payment -> payment.getInvoice() != null
                            && payment.getInvoice().getDealerOrder() != null
                            && payment.getInvoice().getDealerOrder().getDealer() != null
                            && payment.getInvoice().getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/type/{paymentType}")
    @Operation(summary = "Get payments by type", description = "Retrieve dealer payments filtered by payment type")
    public ResponseEntity<?> getPaymentsByType(@PathVariable String paymentType) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Validate và convert paymentType string to enum
            com.evdealer.enums.PaymentMethod paymentMethod = com.evdealer.enums.PaymentMethod.fromString(paymentType);
            if (paymentMethod == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid payment type: " + paymentType);
                error.put("validPaymentTypes", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.PaymentMethod.values())
                    .map(com.evdealer.enums.PaymentMethod::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<DealerPayment> payments = dealerPaymentService.getPaymentsByType(paymentMethod.getValue());
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    payments = payments.stream()
                        .filter(payment -> payment.getInvoice() != null
                            && payment.getInvoice().getDealerOrder() != null
                            && payment.getInvoice().getDealerOrder().getDealer() != null
                            && payment.getInvoice().getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/reference/{referenceNumber}")
    @Operation(summary = "Get payments by reference number", description = "Retrieve dealer payments by reference number")
    public ResponseEntity<?> getPaymentsByReferenceNumber(@PathVariable String referenceNumber) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerPayment> payments = dealerPaymentService.getPaymentsByReferenceNumber(referenceNumber);
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    payments = payments.stream()
                        .filter(payment -> payment.getInvoice() != null
                            && payment.getInvoice().getDealerOrder() != null
                            && payment.getInvoice().getDealerOrder().getDealer() != null
                            && payment.getInvoice().getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Create dealer payment", description = "Create a new dealer payment")
    public ResponseEntity<?> createDealerPayment(@RequestBody DealerPayment dealerPayment) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể tạo payment trực tiếp
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can create payments directly");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerPayment createdPayment = dealerPaymentService.createDealerPayment(dealerPayment);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PutMapping("/{paymentId}")
    @Operation(summary = "Update dealer payment", description = "Update an existing dealer payment")
    public ResponseEntity<?> updateDealerPayment(
            @PathVariable UUID paymentId, 
            @RequestBody DealerPayment dealerPaymentDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể update payment
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can update payments");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerPayment updatedPayment = dealerPaymentService.updateDealerPayment(paymentId, dealerPaymentDetails);
            return ResponseEntity.ok(updatedPayment);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PutMapping("/{paymentId}/status")
    @Operation(summary = "Update payment status", description = "Update the status of a dealer payment")
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
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể update status
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can update payment status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validate và convert status string to enum
            com.evdealer.enums.DealerPaymentStatus statusEnum = com.evdealer.enums.DealerPaymentStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.DealerPaymentStatus.values())
                    .map(com.evdealer.enums.DealerPaymentStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            DealerPayment updatedPayment = dealerPaymentService.updatePaymentStatus(paymentId, statusEnum.getValue());
            return ResponseEntity.ok(updatedPayment);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update payment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @DeleteMapping("/{paymentId}")
    @Operation(summary = "Delete dealer payment", description = "Delete a dealer payment")
    public ResponseEntity<?> deleteDealerPayment(@PathVariable UUID paymentId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ ADMIN có thể xóa payment
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete payments");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            dealerPaymentService.deleteDealerPayment(paymentId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Dealer payment deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    // ==================== NEW IMPROVED APIs ====================
    
    @PostMapping("/process-payment")
    @Operation(summary = "Xử lý thanh toán đại lý", description = "Xử lý thanh toán với validation đầy đủ")
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> paymentRequest) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: DEALER_MANAGER, ADMIN
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only dealer manager or admin can process payments");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validate required fields
            if (!paymentRequest.containsKey("invoiceId") || !paymentRequest.containsKey("amount")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing required fields: invoiceId and amount");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            UUID invoiceId = UUID.fromString(paymentRequest.get("invoiceId").toString());
            BigDecimal amount = new BigDecimal(paymentRequest.get("amount").toString());
            // Field name là paymentMethod (không phải paymentType)
            String paymentMethod = paymentRequest.getOrDefault("paymentMethod", paymentRequest.getOrDefault("paymentType", "BANK_TRANSFER")).toString();
            String referenceNumber = paymentRequest.getOrDefault("referenceNumber", "").toString();
            String notes = paymentRequest.getOrDefault("notes", "").toString();
            // Parse paymentDate nếu có
            LocalDate paymentDate = paymentRequest.containsKey("paymentDate") ? 
                LocalDate.parse(paymentRequest.get("paymentDate").toString()) : LocalDate.now();
            
            // Validate invoice exists
            DealerInvoice invoice = dealerInvoiceService.getInvoiceById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));
            
            // Kiểm tra dealer user chỉ có thể thanh toán invoice của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (invoice.getDealerOrder() != null && invoice.getDealerOrder().getDealer() != null) {
                        UUID invoiceDealerId = invoice.getDealerOrder().getDealer().getDealerId();
                        if (!invoiceDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only process payments for invoices of your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            // Validate payment amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Payment amount must be greater than zero");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Check if payment exceeds invoice balance
            BigDecimal paidAmount = dealerInvoiceService.calculatePaidAmount(invoiceId);
            BigDecimal balance = invoice.getTotalAmount().subtract(paidAmount);
            
            if (amount.compareTo(balance) > 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Payment amount exceeds invoice balance");
                error.put("invoiceBalance", balance.toString());
                error.put("paymentAmount", amount.toString());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Create payment
            DealerPayment payment = new DealerPayment();
            payment.setInvoice(invoice);
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod); // Use PaymentMethod enum
            payment.setPaymentDate(paymentDate);
            payment.setStatus(DealerPaymentStatus.COMPLETED);
            payment.setNotes(notes);
            payment.setReferenceNumber(referenceNumber);
            payment.setPaymentNumber("PAY-" + System.currentTimeMillis());
            
            // Save payment
            DealerPayment savedPayment = dealerPaymentService.createDealerPayment(payment);
            
            // Update invoice status if fully paid
            BigDecimal newPaidAmount = paidAmount.add(amount);
            boolean isFullyPaid = newPaidAmount.compareTo(invoice.getTotalAmount()) >= 0;
            
            if (isFullyPaid) {
                invoice.setStatus(DealerInvoiceStatus.PAID);
                dealerInvoiceService.updateInvoice(invoiceId, invoice);
                
                // Cập nhật DealerOrder status = READY_FOR_DELIVERY sau khi thanh toán đủ
                try {
                    if (invoice.getDealerOrder() != null) {
                        UUID dealerOrderId = invoice.getDealerOrder().getDealerOrderId();
                        dealerOrderService.updateDealerOrderStatus(dealerOrderId, com.evdealer.enums.DealerOrderStatus.READY_FOR_DELIVERY.getValue());
                    }
                } catch (Exception e) {
                    // Log error nhưng không fail payment
                    System.err.println("Failed to update dealer order status after payment: " + e.getMessage());
                }
                
                // Tự động tạo VehicleDelivery sau khi thanh toán đủ
                try {
                    if (invoice.getDealerOrder() != null) {
                        vehicleDeliveryService.createDeliveryFromDealerOrderAfterPayment(invoice.getDealerOrder().getDealerOrderId());
                    }
                } catch (Exception e) {
                    // Log error nhưng không fail payment
                    System.err.println("Failed to auto-create vehicle delivery after payment: " + e.getMessage());
                }
            } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
                invoice.setStatus(DealerInvoiceStatus.PARTIALLY_PAID);
                dealerInvoiceService.updateInvoice(invoiceId, invoice);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Payment processed successfully");
            response.put("dealerPaymentId", savedPayment.getPaymentId());
            response.put("paymentNumber", savedPayment.getPaymentNumber());
            response.put("invoiceId", invoiceId);
            response.put("amount", amount);
            response.put("paymentMethod", paymentMethod); // Trả về paymentMethod (không phải paymentType)
            response.put("paymentDate", paymentDate.toString());
            response.put("status", savedPayment.getStatus() != null ? savedPayment.getStatus().getValue() : null);
            response.put("remainingBalance", invoice.getTotalAmount().subtract(newPaidAmount));
            response.put("isFullyPaid", newPaidAmount.compareTo(invoice.getTotalAmount()) >= 0);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid payment data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/refund/{paymentId}")
    @Operation(summary = "Hoàn tiền thanh toán", description = "Hoàn tiền một thanh toán đã thực hiện")
    public ResponseEntity<?> refundPayment(@PathVariable UUID paymentId, @RequestParam(required = false) String reason) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể hoàn tiền
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can refund payments");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerPayment payment = dealerPaymentService.getDealerPaymentById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
            
            if (payment.getStatus() != DealerPaymentStatus.COMPLETED) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cannot refund payment that is not completed. Current status: " + (payment.getStatus() != null ? payment.getStatus().getValue() : "null"));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Create refund payment
            DealerPayment refund = new DealerPayment();
            refund.setInvoice(payment.getInvoice());
            refund.setAmount(payment.getAmount().negate()); // Negative amount for refund
            refund.setPaymentMethod(payment.getPaymentMethod());
            refund.setPaymentDate(LocalDate.now());
            refund.setStatus(DealerPaymentStatus.REFUNDED);
            refund.setNotes("Refund for payment " + paymentId + (reason != null ? ". Reason: " + reason : ""));
            refund.setReferenceNumber("REF-" + System.currentTimeMillis());
            refund.setPaymentNumber("REF-" + System.currentTimeMillis());
            
            DealerPayment savedRefund = dealerPaymentService.createDealerPayment(refund);
            
            // Update original payment status
            payment.setStatus(DealerPaymentStatus.REFUNDED);
            payment.setNotes(payment.getNotes() + " [REFUNDED: " + reason + "]");
            dealerPaymentService.updateDealerPayment(paymentId, payment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Payment refunded successfully");
            response.put("originalPaymentId", paymentId);
            response.put("refundPaymentId", savedRefund.getPaymentId());
            response.put("refundAmount", payment.getAmount());
            response.put("reason", reason);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to refund payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "Lấy thanh toán theo hóa đơn", description = "Lấy danh sách thanh toán của một hóa đơn")
    public ResponseEntity<?> getPaymentsByInvoice(@PathVariable UUID invoiceId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerInvoice invoice = dealerInvoiceService.getInvoiceById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));
            
            // Kiểm tra dealer user chỉ có thể xem payments của invoice của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (invoice.getDealerOrder() != null && invoice.getDealerOrder().getDealer() != null) {
                        UUID invoiceDealerId = invoice.getDealerOrder().getDealer().getDealerId();
                        if (!invoiceDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view payments for invoices of your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            List<DealerPayment> payments = dealerPaymentService.getPaymentsByInvoice(invoiceId);
            
            BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == DealerPaymentStatus.COMPLETED)
                .map(DealerPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalRefunded = payments.stream()
                .filter(p -> p.getStatus() == DealerPaymentStatus.REFUNDED)
                .map(DealerPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> response = new HashMap<>();
            response.put("invoiceId", invoiceId);
            response.put("invoiceNumber", invoice.getInvoiceNumber());
            response.put("totalAmount", invoice.getTotalAmount());
            response.put("payments", payments);
            response.put("paymentCount", payments.size());
            response.put("totalPaid", totalPaid);
            response.put("totalRefunded", totalRefunded.abs());
            response.put("netPaid", totalPaid.add(totalRefunded));
            response.put("remainingBalance", invoice.getTotalAmount().subtract(totalPaid.add(totalRefunded)));
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "Lấy thanh toán theo đại lý", description = "Lấy danh sách thanh toán của một đại lý")
    public ResponseEntity<?> getPaymentsByDealer(@PathVariable UUID dealerId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem payments của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view payments for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            List<DealerPayment> payments = dealerPaymentService.getPaymentsByDealer(dealerId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/dealer/{dealerId}/summary")
    @Operation(summary = "Tóm tắt thanh toán đại lý", description = "Lấy tóm tắt thanh toán của đại lý")
    public ResponseEntity<?> getDealerPaymentSummary(@PathVariable UUID dealerId, @RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem summary của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view payment summary for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            Map<String, Object> summary = dealerPaymentService.getDealerPaymentSummary(dealerId);
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get payment summary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Thống kê thanh toán", description = "Lấy thống kê tổng quan về thanh toán")
    public ResponseEntity<?> getPaymentStatistics(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể xem statistics
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can view payment statistics");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Map<String, Object> statistics = dealerPaymentService.getPaymentStatistics();
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get payment statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/validate-payment")
    @Operation(summary = "Validate thanh toán", description = "Kiểm tra tính hợp lệ của thanh toán trước khi xử lý")
    public ResponseEntity<?> validatePayment(@RequestBody Map<String, Object> paymentRequest) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Map<String, Object> validation = new HashMap<>();
            validation.put("valid", true);
            validation.put("errors", new java.util.ArrayList<String>());
            
            // Validate invoice exists
            if (paymentRequest.containsKey("invoiceId")) {
                try {
                    UUID invoiceId = UUID.fromString(paymentRequest.get("invoiceId").toString());
                    DealerInvoice invoice = dealerInvoiceService.getInvoiceById(invoiceId)
                        .orElseThrow(() -> new RuntimeException("Invoice not found"));
                    
                    // Kiểm tra dealer user chỉ có thể validate payment cho invoice của dealer mình
                    if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                        var currentUser = securityUtils.getCurrentUser()
                            .orElseThrow(() -> new RuntimeException("User not authenticated"));
                        if (currentUser.getDealer() != null) {
                            UUID userDealerId = currentUser.getDealer().getDealerId();
                            if (invoice.getDealerOrder() != null && invoice.getDealerOrder().getDealer() != null) {
                                UUID invoiceDealerId = invoice.getDealerOrder().getDealer().getDealerId();
                                if (!invoiceDealerId.equals(userDealerId)) {
                                    Map<String, String> error = new HashMap<>();
                                    error.put("error", "Access denied. You can only validate payments for invoices of your own dealer");
                                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                                }
                            }
                        }
                    }
                    
                    validation.put("invoice", Map.of(
                        "invoiceId", invoiceId,
                        "invoiceNumber", invoice.getInvoiceNumber(),
                        "totalAmount", invoice.getTotalAmount(),
                        "status", invoice.getStatus() != null ? invoice.getStatus().getValue() : null
                    ));
                    
                } catch (Exception e) {
                    validation.put("valid", false);
                    ((java.util.List<String>) validation.get("errors")).add("Invalid invoice ID: " + e.getMessage());
                }
            }
            
            // Validate amount
            if (paymentRequest.containsKey("amount")) {
                try {
                    BigDecimal amount = new BigDecimal(paymentRequest.get("amount").toString());
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        validation.put("valid", false);
                        ((java.util.List<String>) validation.get("errors")).add("Payment amount must be greater than zero");
                    } else {
                        validation.put("amount", amount);
                    }
                } catch (Exception e) {
                    validation.put("valid", false);
                    ((java.util.List<String>) validation.get("errors")).add("Invalid amount format: " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(validation);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to validate payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
