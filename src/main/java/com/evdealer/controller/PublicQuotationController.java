package com.evdealer.controller;

import com.evdealer.entity.Order;
import com.evdealer.entity.Quotation;
import com.evdealer.service.QuotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/quotations")
@CrossOrigin(origins = "*")
@Tag(name = "Public Quotation", description = "APIs công khai cho khách hàng xử lý báo giá")
public class PublicQuotationController {
    
    @Autowired
    private QuotationService quotationService;
    
    @GetMapping("/{quotationId}")
    @Operation(summary = "Xem báo giá", description = "Khách hàng xem báo giá (Public - không cần đăng nhập)")
    public ResponseEntity<?> getQuotationById(@PathVariable UUID quotationId) {
        try {
            return quotationService.getQuotationById(quotationId)
                    .map(quotation -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("quotationId", quotation.getQuotationId());
                        response.put("quotationNumber", quotation.getQuotationNumber());
                        response.put("customerId", quotation.getCustomer() != null ? quotation.getCustomer().getCustomerId() : null);
                        response.put("totalPrice", quotation.getTotalPrice());
                        response.put("discountAmount", quotation.getDiscountAmount());
                        response.put("finalPrice", quotation.getFinalPrice());
                        response.put("status", quotation.getStatus() != null ? quotation.getStatus().getValue() : null);
                        response.put("quotationDate", quotation.getQuotationDate());
                        response.put("expiryDate", quotation.getExpiryDate());
                        response.put("notes", quotation.getNotes());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/{quotationId}/accept")
    @Operation(summary = "Chấp nhận báo giá", description = "Khách hàng chấp nhận báo giá (Public - không cần đăng nhập)")
    public ResponseEntity<?> acceptQuotation(
            @PathVariable UUID quotationId,
            @RequestParam(required = false) String conditions) {
        try {
            Order order = quotationService.acceptQuotation(quotationId, conditions);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getOrderId());
            response.put("orderNumber", order.getOrderNumber());
            response.put("status", order.getStatus().getValue());
            response.put("totalAmount", order.getTotalAmount());
            response.put("quotationId", quotationId);
            response.put("message", "Order confirmed. You can now proceed to payment.");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to accept quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to accept quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/{quotationId}/reject")
    @Operation(summary = "Từ chối báo giá", description = "Khách hàng từ chối báo giá (Public - không cần đăng nhập)")
    public ResponseEntity<?> rejectQuotation(
            @PathVariable UUID quotationId,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String adjustmentRequest) {
        try {
            Quotation quotation = quotationService.rejectQuotation(quotationId, reason, adjustmentRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("quotationId", quotation.getQuotationId());
            response.put("status", quotation.getStatus() != null ? quotation.getStatus().getValue() : null);
            response.put("rejectedAt", quotation.getRejectedAt());
            response.put("rejectionReason", quotation.getRejectionReason());
            response.put("message", "Quotation rejected. Dealer staff will review and may create a new quotation.");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reject quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reject quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/{quotationId}/request-adjustment")
    @Operation(summary = "Yêu cầu điều chỉnh báo giá", description = "Khách hàng yêu cầu điều chỉnh báo giá (Public - không cần đăng nhập)")
    public ResponseEntity<?> requestQuotationAdjustment(
            @PathVariable UUID quotationId,
            @RequestParam String adjustmentRequest) {
        try {
            if (adjustmentRequest == null || adjustmentRequest.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Adjustment request is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Quotation quotation = quotationService.requestQuotationAdjustment(quotationId, adjustmentRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("quotationId", quotation.getQuotationId());
            response.put("status", quotation.getStatus() != null ? quotation.getStatus().getValue() : null);
            response.put("message", "Adjustment request received. Dealer staff will review and update the quotation.");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to request adjustment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to request adjustment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

