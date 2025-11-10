package com.evdealer.controller;

import com.evdealer.entity.DealerInvoice;
import com.evdealer.entity.DealerQuotation;
import com.evdealer.entity.DealerQuotationItem;
import com.evdealer.service.DealerQuotationService;
import com.evdealer.service.DealerOrderService;
import com.evdealer.repository.DealerQuotationItemRepository;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/dealer-quotations")
@CrossOrigin(origins = "*")
@Tag(name = "Dealer Quotation Management", description = "APIs quản lý báo giá cho đại lý")
public class DealerQuotationController {
    
    @Autowired
    private DealerQuotationService dealerQuotationService;
    
    @Autowired
    private DealerOrderService dealerOrderService;
    
    @Autowired
    private DealerQuotationItemRepository dealerQuotationItemRepository;
    
    @Autowired
    private com.evdealer.service.DealerQuotationItemService dealerQuotationItemService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy danh sách báo giá", description = "Lấy tất cả báo giá đại lý")
    public ResponseEntity<?> getAllQuotations() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Service đã xử lý filter theo dealer nếu là dealer user
            List<DealerQuotation> quotations = dealerQuotationService.getAllQuotations();
            return ResponseEntity.ok(quotations);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get quotations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{quotationId}")
    @Operation(summary = "Lấy báo giá theo ID", description = "Lấy báo giá theo ID")
    public ResponseEntity<?> getQuotationById(@PathVariable UUID quotationId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerQuotation quotation = dealerQuotationService.getQuotationById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
            
            // Kiểm tra dealer user chỉ có thể xem quotation của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (quotation.getDealerOrder() != null && quotation.getDealerOrder().getDealer() != null) {
                        UUID quotationDealerId = quotation.getDealerOrder().getDealer().getDealerId();
                        if (!quotationDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view quotations for your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            return ResponseEntity.ok(quotation);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/number/{quotationNumber}")
    @Operation(summary = "Lấy báo giá theo số", description = "Lấy báo giá theo số báo giá")
    public ResponseEntity<?> getQuotationByNumber(@PathVariable String quotationNumber) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerQuotation quotation = dealerQuotationService.getQuotationByNumber(quotationNumber)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
            
            // Kiểm tra dealer user chỉ có thể xem quotation của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (quotation.getDealerOrder() != null && quotation.getDealerOrder().getDealer() != null) {
                        UUID quotationDealerId = quotation.getDealerOrder().getDealer().getDealerId();
                        if (!quotationDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view quotations for your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            return ResponseEntity.ok(quotation);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "Lấy báo giá theo đại lý", description = "Lấy danh sách báo giá của một đại lý")
    public ResponseEntity<?> getQuotationsByDealer(@PathVariable UUID dealerId) {
        // Kiểm tra authentication
        if (!securityUtils.getCurrentUser().isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        // Kiểm tra dealer user chỉ có thể xem báo giá của dealer mình
        if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
            var currentUser = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
            if (currentUser.getDealer() != null) {
                UUID userDealerId = currentUser.getDealer().getDealerId();
                if (!dealerId.equals(userDealerId)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. You can only view quotations for your own dealer");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
        }
        
        List<DealerQuotation> quotations = dealerQuotationService.getQuotationsByDealer(dealerId);
        return ResponseEntity.ok(quotations);
    }
    
    @GetMapping("/dealer-order/{dealerOrderId}")
    @Operation(summary = "Lấy báo giá theo đơn hàng", description = "Lấy danh sách báo giá của một đơn hàng đại lý")
    public ResponseEntity<?> getQuotationsByDealerOrder(@PathVariable UUID dealerOrderId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem quotations của order của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    // Kiểm tra order thuộc về dealer của user
                    var dealerOrder = dealerOrderService.getDealerOrderById(dealerOrderId)
                        .orElseThrow(() -> new RuntimeException("Dealer order not found"));
                    if (dealerOrder.getDealer() != null) {
                        UUID orderDealerId = dealerOrder.getDealer().getDealerId();
                        if (!orderDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view quotations for orders of your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            List<DealerQuotation> quotations = dealerQuotationService.getQuotationsByDealerOrder(dealerOrderId);
            return ResponseEntity.ok(quotations);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get quotations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Lấy báo giá theo trạng thái", description = "Lấy danh sách báo giá theo trạng thái")
    public ResponseEntity<?> getQuotationsByStatus(@PathVariable String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Validate và convert status string to enum
            com.evdealer.enums.DealerQuotationStatus statusEnum = com.evdealer.enums.DealerQuotationStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.DealerQuotationStatus.values())
                    .map(com.evdealer.enums.DealerQuotationStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<DealerQuotation> quotations = dealerQuotationService.getQuotationsByStatus(statusEnum.getValue());
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    quotations = quotations.stream()
                        .filter(quotation -> quotation.getDealerOrder() != null
                            && quotation.getDealerOrder().getDealer() != null
                            && quotation.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(quotations);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get quotations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/evm-staff/{evmStaffId}")
    @Operation(summary = "Lấy báo giá theo nhân viên EVM", description = "Lấy danh sách báo giá do nhân viên EVM tạo")
    public ResponseEntity<?> getQuotationsByEvmStaff(@PathVariable UUID evmStaffId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerQuotation> quotations = dealerQuotationService.getQuotationsByEvmStaff(evmStaffId);
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    quotations = quotations.stream()
                        .filter(quotation -> quotation.getDealerOrder() != null
                            && quotation.getDealerOrder().getDealer() != null
                            && quotation.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(quotations);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get quotations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Lấy báo giá theo khoảng ngày", description = "Lấy báo giá theo khoảng ngày")
    public ResponseEntity<?> getQuotationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerQuotation> quotations = dealerQuotationService.getQuotationsByDateRange(startDate, endDate);
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    quotations = quotations.stream()
                        .filter(quotation -> quotation.getDealerOrder() != null
                            && quotation.getDealerOrder().getDealer() != null
                            && quotation.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(quotations);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get quotations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/expired")
    @Operation(summary = "Lấy báo giá đã hết hạn", description = "Lấy danh sách báo giá đã hết hạn")
    public ResponseEntity<?> getExpiredQuotations() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerQuotation> quotations = dealerQuotationService.getExpiredQuotations();
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    quotations = quotations.stream()
                        .filter(quotation -> quotation.getDealerOrder() != null
                            && quotation.getDealerOrder().getDealer() != null
                            && quotation.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(quotations);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get quotations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/from-order/{dealerOrderId}")
    @Operation(summary = "Tạo báo giá từ đơn hàng", description = "Hãng tạo báo giá từ đơn hàng đại lý")
    public ResponseEntity<?> createQuotationFromOrder(
            @PathVariable UUID dealerOrderId,
            @RequestParam(required = false) UUID evmStaffId,
            @RequestParam(required = false) BigDecimal discountPercentage,
            @RequestParam(required = false) String notes) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể tạo báo giá
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can create quotations");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Sử dụng current user nếu không có evmStaffId
            if (evmStaffId == null) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent()) {
                    evmStaffId = currentUserOpt.get().getUserId();
                }
            }
            
            DealerQuotation quotation = dealerQuotationService.createQuotationFromOrder(
                dealerOrderId, evmStaffId, discountPercentage, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quotation created successfully");
            response.put("quotationId", quotation.getQuotationId());
            response.put("quotationNumber", quotation.getQuotationNumber());
            response.put("dealerOrderId", dealerOrderId);
            response.put("totalAmount", quotation.getTotalAmount());
            response.put("status", quotation.getStatus() != null ? quotation.getStatus().getValue() : null);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/{quotationId}/send")
    @Operation(summary = "Gửi báo giá cho đại lý", description = "Gửi báo giá cho đại lý (chuyển status từ pending sang sent)")
    public ResponseEntity<?> sendQuotationToDealer(@PathVariable UUID quotationId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể gửi báo giá
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can send quotations");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerQuotation quotation = dealerQuotationService.sendQuotationToDealer(quotationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quotation sent successfully to dealer");
            response.put("quotationId", quotationId);
            response.put("quotationNumber", quotation.getQuotationNumber());
            response.put("status", quotation.getStatus() != null ? quotation.getStatus().getValue() : null);
            response.put("expiryDate", quotation.getExpiryDate());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to send quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/{quotationId}/accept")
    @Operation(summary = "Đại lý chấp nhận báo giá", description = "Đại lý chấp nhận báo giá, hệ thống tự động tạo Invoice")
    public ResponseEntity<?> acceptQuotation(@PathVariable UUID quotationId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ DEALER_MANAGER hoặc ADMIN
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only dealer manager or admin can accept quotations");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể accept quotation của dealer mình
            var quotation = dealerQuotationService.getQuotationById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (quotation.getDealer() != null && !quotation.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only accept quotations for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            DealerInvoice invoice = dealerQuotationService.acceptQuotation(quotationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quotation accepted successfully. Invoice created.");
            response.put("quotationId", quotationId);
            response.put("invoiceId", invoice.getInvoiceId());
            response.put("invoiceNumber", invoice.getInvoiceNumber());
            response.put("totalAmount", invoice.getTotalAmount());
            response.put("dueDate", invoice.getDueDate());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to accept quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/{quotationId}/reject")
    @Operation(summary = "Đại lý từ chối báo giá", description = "Đại lý từ chối báo giá")
    public ResponseEntity<?> rejectQuotation(
            @PathVariable UUID quotationId,
            @RequestParam(required = false) String reason) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ DEALER_MANAGER hoặc ADMIN
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only dealer manager or admin can reject quotations");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể reject quotation của dealer mình
            var quotationCheck = dealerQuotationService.getQuotationById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (quotationCheck.getDealer() != null && !quotationCheck.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only reject quotations for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            DealerQuotation quotation = dealerQuotationService.rejectQuotation(
                quotationId, reason != null ? reason : "Rejected by dealer");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quotation rejected successfully");
            response.put("quotationId", quotationId);
            response.put("quotationNumber", quotation.getQuotationNumber());
            response.put("status", quotation.getStatus() != null ? quotation.getStatus().getValue() : null);
            response.put("rejectionReason", quotation.getRejectionReason());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reject quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/items/all")
    @Operation(summary = "Lấy tất cả items", description = "Lấy danh sách tất cả items của tất cả báo giá đại lý")
    public ResponseEntity<?> getAllDealerQuotationItems() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xem tất cả items
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can view all quotation items");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            List<DealerQuotationItem> allItems = dealerQuotationItemService.getAllItems();
            return ResponseEntity.ok(allItems);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get all quotation items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{quotationId}/items")
    @Operation(summary = "Lấy chi tiết báo giá", description = "Lấy danh sách sản phẩm trong báo giá")
    public ResponseEntity<?> getQuotationItems(@PathVariable UUID quotationId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerQuotation quotation = dealerQuotationService.getQuotationById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
            
            // Kiểm tra dealer user chỉ có thể xem items của quotation của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (quotation.getDealerOrder() != null && quotation.getDealerOrder().getDealer() != null) {
                        UUID quotationDealerId = quotation.getDealerOrder().getDealer().getDealerId();
                        if (!quotationDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view items for quotations of your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            List<DealerQuotationItem> items = dealerQuotationItemService.getItemsByQuotationId(quotationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("quotationId", quotationId);
            response.put("quotationNumber", quotation.getQuotationNumber());
            response.put("dealerOrderId", quotation.getDealerOrder() != null ? quotation.getDealerOrder().getDealerOrderId() : null);
            response.put("items", items);
            response.put("itemCount", items.size());
            response.put("totalAmount", quotation.getTotalAmount());
            response.put("subtotal", quotation.getSubtotal());
            response.put("discountAmount", quotation.getDiscountAmount());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get quotation items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PostMapping("/{quotationId}/items")
    @Operation(summary = "Thêm item vào báo giá", description = "Thêm một sản phẩm vào báo giá đại lý")
    public ResponseEntity<?> addItemToQuotation(@PathVariable UUID quotationId, @RequestBody DealerQuotationItem item) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ EVM_STAFF hoặc ADMIN mới có thể thêm item
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can add items to quotations");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validate quotation exists and is pending
            DealerQuotation quotation = dealerQuotationService.getQuotationById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
            
            if (quotation.getStatus() != com.evdealer.enums.DealerQuotationStatus.PENDING) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only pending quotations can have items added");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Set quotation
            item.setQuotation(quotation);
            
            // Create item
            DealerQuotationItem createdItem = dealerQuotationItemService.createItem(item);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{quotationId}/items/{itemId}")
    @Operation(summary = "Cập nhật item trong báo giá", description = "Cập nhật thông tin của một sản phẩm trong báo giá")
    public ResponseEntity<?> updateQuotationItem(
            @PathVariable UUID quotationId,
            @PathVariable UUID itemId,
            @RequestBody DealerQuotationItem itemDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ EVM_STAFF hoặc ADMIN mới có thể update item
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can update quotation items");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validate quotation exists and is pending
            DealerQuotation quotation = dealerQuotationService.getQuotationById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
            
            if (quotation.getStatus() != com.evdealer.enums.DealerQuotationStatus.PENDING) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only pending quotations can have items updated");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Update item
            DealerQuotationItem updatedItem = dealerQuotationItemService.updateItem(itemId, itemDetails);
            
            return ResponseEntity.ok(updatedItem);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{quotationId}/items/{itemId}")
    @Operation(summary = "Xóa item khỏi báo giá", description = "Xóa một sản phẩm khỏi báo giá đại lý")
    public ResponseEntity<?> deleteQuotationItem(
            @PathVariable UUID quotationId,
            @PathVariable UUID itemId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ EVM_STAFF hoặc ADMIN mới có thể xóa item
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can delete quotation items");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validate quotation exists and is pending
            DealerQuotation quotation = dealerQuotationService.getQuotationById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
            
            if (quotation.getStatus() != com.evdealer.enums.DealerQuotationStatus.PENDING) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only pending quotations can have items deleted");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Delete item
            dealerQuotationItemService.deleteItem(itemId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Quotation item deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{quotationId}")
    @Operation(summary = "Cập nhật báo giá", description = "Cập nhật báo giá (chỉ báo giá ở trạng thái pending)")
    public ResponseEntity<?> updateQuotation(
            @PathVariable UUID quotationId,
            @RequestBody DealerQuotation quotationDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể update quotation
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can update quotations");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerQuotation updatedQuotation = dealerQuotationService.updateQuotation(quotationId, quotationDetails);
            return ResponseEntity.ok(updatedQuotation);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @DeleteMapping("/{quotationId}")
    @Operation(summary = "Xóa báo giá", description = "Xóa báo giá (chỉ báo giá ở trạng thái pending)")
    public ResponseEntity<?> deleteQuotation(@PathVariable UUID quotationId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ ADMIN có thể xóa quotation
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete quotations");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            dealerQuotationService.deleteQuotation(quotationId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Quotation deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}

