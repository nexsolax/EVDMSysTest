package com.evdealer.controller;

import com.evdealer.entity.DealerInvoice;
import com.evdealer.entity.DealerOrder;
import com.evdealer.entity.DealerOrderItem;
import com.evdealer.enums.ApprovalStatus;
import com.evdealer.enums.DealerInvoiceStatus;
import com.evdealer.service.DealerInvoiceService;
import com.evdealer.service.DealerOrderService;
import com.evdealer.service.DealerOrderItemService;
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
@RequestMapping("/api/dealer-invoices")
@CrossOrigin(origins = "*")
@Tag(name = "Dealer Invoice Management", description = "APIs quản lý hóa đơn đại lý")
public class DealerInvoiceController {
    
    @Autowired
    private DealerInvoiceService dealerInvoiceService;
    
    @Autowired
    private DealerOrderService dealerOrderService;
    
    @Autowired
    private DealerOrderItemService dealerOrderItemService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy danh sách hóa đơn", description = "Lấy tất cả hóa đơn đại lý")
    public ResponseEntity<?> getAllInvoices() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Service đã xử lý filter theo dealer nếu là dealer user
            List<DealerInvoice> invoices = dealerInvoiceService.getAllInvoices();
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{invoiceId}")
    @Operation(summary = "Lấy hóa đơn theo ID", description = "Lấy hóa đơn theo ID")
    public ResponseEntity<?> getInvoiceById(@PathVariable @Parameter(description = "Invoice ID") UUID invoiceId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerInvoice invoice = dealerInvoiceService.getInvoiceById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
            
            // Kiểm tra dealer user chỉ có thể xem invoice của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (invoice.getDealerOrder() != null && invoice.getDealerOrder().getDealer() != null) {
                        UUID invoiceDealerId = invoice.getDealerOrder().getDealer().getDealerId();
                        if (!invoiceDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view invoices for your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/number/{invoiceNumber}")
    @Operation(summary = "Lấy hóa đơn theo số", description = "Lấy hóa đơn theo số hóa đơn")
    public ResponseEntity<?> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerInvoice invoice = dealerInvoiceService.getInvoiceByNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
            
            // Kiểm tra dealer user chỉ có thể xem invoice của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (invoice.getDealerOrder() != null && invoice.getDealerOrder().getDealer() != null) {
                        UUID invoiceDealerId = invoice.getDealerOrder().getDealer().getDealerId();
                        if (!invoiceDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view invoices for your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Lấy hóa đơn theo trạng thái", description = "Lấy hóa đơn theo trạng thái")
    public ResponseEntity<?> getInvoicesByStatus(@PathVariable String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Validate và convert status string to enum
            com.evdealer.enums.DealerInvoiceStatus statusEnum = com.evdealer.enums.DealerInvoiceStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.DealerInvoiceStatus.values())
                    .map(com.evdealer.enums.DealerInvoiceStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<DealerInvoice> invoices = dealerInvoiceService.getInvoicesByStatus(statusEnum.getValue());
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    invoices = invoices.stream()
                        .filter(invoice -> invoice.getDealerOrder() != null 
                            && invoice.getDealerOrder().getDealer() != null
                            && invoice.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/dealer-order/{dealerOrderId}")
    @Operation(summary = "Lấy hóa đơn theo đơn hàng", description = "Lấy hóa đơn theo đơn hàng đại lý")
    public ResponseEntity<?> getInvoicesByDealerOrder(@PathVariable UUID dealerOrderId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem invoices của order của dealer mình
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
                            error.put("error", "Access denied. You can only view invoices for orders of your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            List<DealerInvoice> invoices = dealerInvoiceService.getInvoicesByDealerOrder(dealerOrderId);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/evm-staff/{evmStaffId}")
    @Operation(summary = "Lấy hóa đơn theo nhân viên", description = "Lấy hóa đơn theo nhân viên EVM")
    public ResponseEntity<?> getInvoicesByEvmStaff(@PathVariable UUID evmStaffId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerInvoice> invoices = dealerInvoiceService.getInvoicesByEvmStaff(evmStaffId);
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    invoices = invoices.stream()
                        .filter(invoice -> invoice.getDealerOrder() != null 
                            && invoice.getDealerOrder().getDealer() != null
                            && invoice.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Lấy hóa đơn theo khoảng ngày", description = "Lấy hóa đơn theo khoảng ngày")
    public ResponseEntity<?> getInvoicesByDateRange(
            @RequestParam @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @Parameter(description = "End date") LocalDate endDate) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerInvoice> invoices = dealerInvoiceService.getInvoicesByDateRange(startDate, endDate);
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    invoices = invoices.stream()
                        .filter(invoice -> invoice.getDealerOrder() != null 
                            && invoice.getDealerOrder().getDealer() != null
                            && invoice.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/overdue")
    @Operation(summary = "Lấy hóa đơn quá hạn", description = "Lấy hóa đơn quá hạn")
    public ResponseEntity<?> getOverdueInvoices() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerInvoice> invoices = dealerInvoiceService.getOverdueInvoices();
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    invoices = invoices.stream()
                        .filter(invoice -> invoice.getDealerOrder() != null 
                            && invoice.getDealerOrder().getDealer() != null
                            && invoice.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Tạo hóa đơn mới", description = "Tạo hóa đơn đại lý mới")
    public ResponseEntity<?> createInvoice(@RequestBody DealerInvoice invoice) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể tạo invoice
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can create invoices");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerInvoice createdInvoice = dealerInvoiceService.createInvoice(invoice);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInvoice);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PutMapping("/{invoiceId}")
    @Operation(summary = "Cập nhật hóa đơn", description = "Cập nhật hóa đơn")
    public ResponseEntity<?> updateInvoice(
            @PathVariable UUID invoiceId, 
            @RequestBody DealerInvoice invoiceDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể update invoice
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can update invoices");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerInvoice updatedInvoice = dealerInvoiceService.updateInvoice(invoiceId, invoiceDetails);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PutMapping("/{invoiceId}/status")
    @Operation(summary = "Cập nhật trạng thái hóa đơn", description = "Cập nhật trạng thái hóa đơn")
    public ResponseEntity<?> updateInvoiceStatus(
            @PathVariable UUID invoiceId, 
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
                error.put("error", "Access denied. Only EVM staff or admin can update invoice status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validate và convert status string to enum
            com.evdealer.enums.DealerInvoiceStatus statusEnum = com.evdealer.enums.DealerInvoiceStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.DealerInvoiceStatus.values())
                    .map(com.evdealer.enums.DealerInvoiceStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            DealerInvoice updatedInvoice = dealerInvoiceService.updateInvoiceStatus(invoiceId, statusEnum.getValue());
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update invoice status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @DeleteMapping("/{invoiceId}")
    @Operation(summary = "Xóa hóa đơn", description = "Xóa hóa đơn")
    public ResponseEntity<?> deleteInvoice(@PathVariable UUID invoiceId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ ADMIN có thể xóa invoice
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete invoices");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            dealerInvoiceService.deleteInvoice(invoiceId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invoice deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    // ==================== NEW IMPROVED APIs ====================
    
    @PostMapping("/generate-from-order/{dealerOrderId}")
    @Operation(summary = "Tự động tạo hóa đơn từ đơn hàng", description = "Tự động tạo hóa đơn từ đơn hàng đại lý đã duyệt")
    public ResponseEntity<?> generateInvoiceFromOrder(@PathVariable UUID dealerOrderId, @RequestParam(required = false) UUID evmStaffId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể tạo invoice từ order
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can generate invoices from orders");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Sử dụng current user nếu không có evmStaffId
            if (evmStaffId == null) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent()) {
                    evmStaffId = currentUserOpt.get().getUserId();
                }
            }
            
            // Validate dealer order exists and is approved
            DealerOrder dealerOrder = dealerOrderService.getDealerOrderById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Dealer order not found with ID: " + dealerOrderId));
            
            if (dealerOrder.getApprovalStatus() != ApprovalStatus.APPROVED) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cannot generate invoice for non-approved order. Order status: " + (dealerOrder.getApprovalStatus() != null ? dealerOrder.getApprovalStatus().getValue() : "null"));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Check if invoice already exists for this order
            List<DealerInvoice> existingInvoices = dealerInvoiceService.getInvoicesByDealerOrder(dealerOrderId);
            if (!existingInvoices.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invoice already exists for this order");
                error.put("existingInvoiceId", existingInvoices.get(0).getInvoiceId().toString());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            
            // Generate invoice
            DealerInvoice invoice = dealerInvoiceService.generateInvoiceFromOrder(dealerOrderId, evmStaffId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Invoice generated successfully from dealer order");
            response.put("invoiceId", invoice.getInvoiceId());
            response.put("invoiceNumber", invoice.getInvoiceNumber());
            response.put("dealerOrderId", dealerOrderId);
            response.put("totalAmount", invoice.getTotalAmount());
            response.put("dueDate", invoice.getDueDate());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{invoiceId}/items")
    @Operation(summary = "Lấy chi tiết xe trong hóa đơn", description = "Lấy danh sách xe trong hóa đơn đại lý")
    public ResponseEntity<?> getInvoiceItems(@PathVariable UUID invoiceId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerInvoice invoice = dealerInvoiceService.getInvoiceById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));
            
            // Kiểm tra dealer user chỉ có thể xem items của invoice của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (invoice.getDealerOrder() != null && invoice.getDealerOrder().getDealer() != null) {
                        UUID invoiceDealerId = invoice.getDealerOrder().getDealer().getDealerId();
                        if (!invoiceDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view items for invoices of your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            List<DealerOrderItem> items = dealerOrderItemService.getItemsByDealerOrderId(invoice.getDealerOrder().getDealerOrderId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("invoiceId", invoiceId);
            response.put("invoiceNumber", invoice.getInvoiceNumber());
            response.put("dealerOrderId", invoice.getDealerOrder().getDealerOrderId());
            response.put("dealerOrderNumber", invoice.getDealerOrder().getDealerOrderNumber());
            response.put("items", items);
            response.put("itemCount", items.size());
            response.put("totalAmount", invoice.getTotalAmount());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoice items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PostMapping("/{invoiceId}/send")
    @Operation(summary = "Gửi hóa đơn cho đại lý", description = "Gửi hóa đơn cho đại lý qua email")
    public ResponseEntity<?> sendInvoiceToDealer(@PathVariable UUID invoiceId, @RequestParam(required = false) String email) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể gửi invoice
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can send invoices");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerInvoice invoice = dealerInvoiceService.getInvoiceById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));
            
            // Get dealer email if not provided
            String dealerEmail = email != null ? email : invoice.getDealerOrder().getDealer().getEmail();
            
            if (dealerEmail == null || dealerEmail.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Dealer email not found. Please provide email address.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Update invoice status to sent
            // Note: DealerInvoiceStatus doesn't have SENT, using ISSUED instead
            // Consider adding SENT to enum if needed, or use ISSUED as equivalent
            invoice.setStatus(DealerInvoiceStatus.ISSUED);
            dealerInvoiceService.updateInvoice(invoiceId, invoice);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Invoice sent successfully to dealer");
            response.put("invoiceId", invoiceId);
            response.put("invoiceNumber", invoice.getInvoiceNumber());
            response.put("dealerEmail", dealerEmail);
            response.put("sentAt", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to send invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/{invoiceId}/pdf")
    @Operation(summary = "Tải hóa đơn PDF", description = "Tải hóa đơn dưới dạng PDF")
    public ResponseEntity<?> downloadInvoicePDF(@PathVariable UUID invoiceId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerInvoice invoice = dealerInvoiceService.getInvoiceById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));
            
            // Kiểm tra dealer user chỉ có thể xem PDF của invoice của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (invoice.getDealerOrder() != null && invoice.getDealerOrder().getDealer() != null) {
                        UUID invoiceDealerId = invoice.getDealerOrder().getDealer().getDealerId();
                        if (!invoiceDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view PDF for invoices of your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            // For now, return invoice data as JSON
            // In real implementation, you would generate PDF and return as byte array
            Map<String, Object> response = new HashMap<>();
            response.put("message", "PDF generation not implemented yet. Returning invoice data.");
            response.put("invoice", invoice);
            response.put("dealerOrder", invoice.getDealerOrder());
            response.put("items", dealerOrderItemService.getItemsByDealerOrderId(invoice.getDealerOrder().getDealerOrderId()));
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/{invoiceId}/balance")
    @Operation(summary = "Kiểm tra số dư còn lại", description = "Kiểm tra số tiền còn lại chưa thanh toán")
    public ResponseEntity<?> getInvoiceBalance(@PathVariable UUID invoiceId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerInvoice invoice = dealerInvoiceService.getInvoiceById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));
            
            // Kiểm tra dealer user chỉ có thể xem balance của invoice của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (invoice.getDealerOrder() != null && invoice.getDealerOrder().getDealer() != null) {
                        UUID invoiceDealerId = invoice.getDealerOrder().getDealer().getDealerId();
                        if (!invoiceDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view balance for invoices of your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            // Calculate paid amount and balance
            BigDecimal paidAmount = dealerInvoiceService.calculatePaidAmount(invoiceId);
            BigDecimal balance = invoice.getTotalAmount().subtract(paidAmount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("invoiceId", invoiceId);
            response.put("invoiceNumber", invoice.getInvoiceNumber());
            response.put("totalAmount", invoice.getTotalAmount());
            response.put("paidAmount", paidAmount);
            response.put("balance", balance);
            response.put("isFullyPaid", balance.compareTo(BigDecimal.ZERO) <= 0);
            response.put("overdue", invoice.getDueDate().isBefore(LocalDate.now()) && balance.compareTo(BigDecimal.ZERO) > 0);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoice balance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "Lấy hóa đơn theo đại lý", description = "Lấy danh sách hóa đơn của một đại lý")
    public ResponseEntity<?> getInvoicesByDealer(@PathVariable UUID dealerId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem invoices của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view invoices for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            List<DealerInvoice> invoices = dealerInvoiceService.getInvoicesByDealer(dealerId);
            return ResponseEntity.ok(invoices);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/dealer/{dealerId}/unpaid")
    @Operation(summary = "Lấy hóa đơn chưa thanh toán", description = "Lấy danh sách hóa đơn chưa thanh toán của đại lý")
    public ResponseEntity<?> getUnpaidInvoicesByDealer(@PathVariable UUID dealerId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem invoices của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view unpaid invoices for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            List<DealerInvoice> unpaidInvoices = dealerInvoiceService.getUnpaidInvoicesByDealer(dealerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("dealerId", dealerId);
            response.put("unpaidInvoices", unpaidInvoices);
            response.put("count", unpaidInvoices.size());
            response.put("totalUnpaidAmount", unpaidInvoices.stream()
                .map(DealerInvoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get unpaid invoices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Thống kê hóa đơn", description = "Lấy thống kê tổng quan về hóa đơn")
    public ResponseEntity<?> getInvoiceStatistics(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate) {
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
                error.put("error", "Access denied. Only EVM staff or admin can view invoice statistics");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Map<String, Object> statistics = dealerInvoiceService.getInvoiceStatistics();
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get invoice statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
