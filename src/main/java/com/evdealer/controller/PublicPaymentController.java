package com.evdealer.controller;

import com.evdealer.entity.CustomerPayment;
import com.evdealer.enums.CustomerPaymentStatus;
import com.evdealer.service.CustomerPaymentService;
import com.evdealer.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/payments")
@CrossOrigin(origins = "*")
@Tag(name = "Public Payment Management", description = "APIs thanh toán cho khách vãng lai - không cần đăng nhập")
public class PublicPaymentController {
    
    @Autowired
    private CustomerPaymentService customerPaymentService;
    
    @Autowired
    private OrderService orderService;
    
    @PostMapping("/deposit")
    @Operation(summary = "Đặt cọc", description = "Khách vãng lai có thể đặt cọc cho đơn hàng")
    public ResponseEntity<?> createDepositPayment(@RequestBody Map<String, Object> request) {
        try {
            UUID orderId = UUID.fromString(request.get("orderId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String paymentMethod = (String) request.get("paymentMethod");
            String notes = (String) request.get("notes");
            
            // Verify order exists
            var order = orderService.getOrderById(orderId);
            if (order.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Order not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Create deposit payment
            CustomerPayment payment = new CustomerPayment();
            payment.setOrder(order.get());
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentType("deposit");
            payment.setPaymentDate(LocalDateTime.now().toLocalDate());
            payment.setStatus(CustomerPaymentStatus.PENDING);
            payment.setNotes(notes);
            payment.setPaymentNumber("PAY-" + System.currentTimeMillis());
            
            CustomerPayment createdPayment = customerPaymentService.createCustomerPayment(payment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Deposit payment created successfully");
            response.put("paymentId", createdPayment.getPaymentId());
            response.put("amount", amount);
            response.put("paymentMethod", paymentMethod);
            response.put("status", createdPayment.getStatus() != null ? createdPayment.getStatus().getValue() : "pending");
            response.put("orderId", orderId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Deposit payment failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/full")
    @Operation(summary = "Thanh toán toàn bộ", description = "Khách vãng lai có thể thanh toán toàn bộ đơn hàng")
    public ResponseEntity<?> createFullPayment(@RequestBody Map<String, Object> request) {
        try {
            UUID orderId = UUID.fromString(request.get("orderId").toString());
            String paymentMethod = (String) request.get("paymentMethod");
            String notes = (String) request.get("notes");
            
            // Verify order exists
            var order = orderService.getOrderById(orderId);
            if (order.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Order not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Create full payment
            CustomerPayment payment = new CustomerPayment();
            payment.setOrder(order.get());
            payment.setAmount(order.get().getTotalAmount());
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentType("full");
            payment.setPaymentDate(LocalDateTime.now().toLocalDate());
            payment.setStatus(CustomerPaymentStatus.PENDING);
            payment.setNotes(notes);
            payment.setPaymentNumber("PAY-" + System.currentTimeMillis());
            
            CustomerPayment createdPayment = customerPaymentService.createCustomerPayment(payment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Full payment created successfully");
            response.put("paymentId", createdPayment.getPaymentId());
            response.put("amount", order.get().getTotalAmount());
            response.put("paymentMethod", paymentMethod);
            response.put("status", createdPayment.getStatus() != null ? createdPayment.getStatus().getValue() : "pending");
            response.put("orderId", orderId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Full payment failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{paymentId}")
    @Operation(summary = "Xem trạng thái thanh toán", description = "Khách vãng lai có thể xem trạng thái thanh toán")
    public ResponseEntity<?> getPaymentStatus(@PathVariable UUID paymentId) {
        try {
            return customerPaymentService.getPaymentById(paymentId)
                    .map(payment -> {
                        Map<String, Object> status = new HashMap<>();
                        status.put("paymentId", payment.getPaymentId());
                        status.put("amount", payment.getAmount());
                        status.put("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod().getValue() : null);
                        status.put("paymentType", payment.getPaymentType());
                        status.put("status", payment.getStatus() != null ? payment.getStatus().getValue() : null);
                        status.put("paymentDate", payment.getPaymentDate());
                        status.put("orderId", payment.getOrder().getOrderId());
                        status.put("orderNumber", payment.getOrder().getOrderNumber());
                        return ResponseEntity.ok(status);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Hoàn tiền", description = "Khách vãng lai có thể yêu cầu hoàn tiền")
    public ResponseEntity<?> requestRefund(
            @PathVariable UUID paymentId,
            @RequestParam(required = false) String reason) {
        try {
            var payment = customerPaymentService.getPaymentById(paymentId);
            if (payment.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Payment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Update payment status to refund requested
            CustomerPayment paymentEntity = payment.get();
            // Note: CustomerPaymentStatus doesn't have REFUND_REQUESTED, using PENDING for now
            // Consider adding REFUND_REQUESTED to enum if needed
            paymentEntity.setStatus(CustomerPaymentStatus.PENDING);
            if (reason != null && !reason.trim().isEmpty()) {
                paymentEntity.setNotes((paymentEntity.getNotes() != null ? paymentEntity.getNotes() + "\n" : "") + 
                                     "Refund reason: " + reason);
            }
            
            CustomerPayment updatedPayment = customerPaymentService.updateCustomerPayment(paymentId, paymentEntity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Refund request submitted successfully");
            response.put("paymentId", paymentId);
            response.put("status", "refund_requested");
            response.put("reason", reason);
            response.put("amount", updatedPayment.getAmount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Refund request failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Xem thanh toán theo đơn hàng", description = "Khách vãng lai có thể xem tất cả thanh toán của đơn hàng")
    public ResponseEntity<?> getPaymentsByOrder(@PathVariable UUID orderId) {
        try {
            var payments = customerPaymentService.getPaymentsByOrder(orderId);
            
            List<Map<String, Object>> paymentList = payments.stream().map(payment -> {
                Map<String, Object> paymentMap = new HashMap<>();
                paymentMap.put("paymentId", payment.getPaymentId());
                paymentMap.put("amount", payment.getAmount());
                paymentMap.put("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod().getValue() : null);
                paymentMap.put("paymentType", payment.getPaymentType());
                paymentMap.put("status", payment.getStatus());
                paymentMap.put("paymentDate", payment.getPaymentDate());
                paymentMap.put("paymentNumber", payment.getPaymentNumber());
                paymentMap.put("notes", payment.getNotes());
                if (payment.getOrder() != null) {
                    paymentMap.put("orderId", payment.getOrder().getOrderId());
                }
                return paymentMap;
            }).collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("payments", paymentList);
            response.put("totalPayments", paymentList.size());
            response.put("totalAmount", payments.stream()
                    .map(CustomerPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/methods")
    @Operation(summary = "Phương thức thanh toán", description = "Khách vãng lai có thể xem các phương thức thanh toán có sẵn")
    public ResponseEntity<?> getPaymentMethods() {
        try {
            Map<String, Object> methods = new HashMap<>();
            methods.put("availableMethods", new String[]{
                "bank_transfer", "credit_card", "debit_card", "cash", "installment"
            });
            methods.put("supportedBanks", new String[]{
                "Vietcombank", "VietinBank", "BIDV", "Agribank", "Techcombank"
            });
            methods.put("installmentOptions", new String[]{
                "6_months", "12_months", "24_months", "36_months"
            });
            
            return ResponseEntity.ok(methods);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve payment methods: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
