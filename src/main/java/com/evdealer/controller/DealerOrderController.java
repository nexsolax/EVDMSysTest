package com.evdealer.controller;

import com.evdealer.dto.CreateDealerOrderRequest;
import com.evdealer.dto.CreateDealerOrderResponse;
import com.evdealer.entity.DealerOrder;
import com.evdealer.entity.DealerOrderItem;
import com.evdealer.enums.ApprovalStatus;
import com.evdealer.enums.DealerOrderStatus;
import com.evdealer.service.DealerOrderService;
import com.evdealer.service.DealerOrderItemService;
import com.evdealer.util.SecurityUtils;
import com.evdealer.repository.DealerOrderRepository;
import com.evdealer.repository.DealerRepository;
import com.evdealer.entity.Dealer;
import com.evdealer.service.DealerService;
import java.util.Optional;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dealer-orders")
@CrossOrigin(origins = "*")
@Tag(name = "Dealer Order Management", description = "APIs quản lý đơn hàng đại lý")
public class DealerOrderController {
    
    @Autowired
    private DealerOrderService dealerOrderService;
    
    @Autowired
    private DealerOrderItemService dealerOrderItemService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @Autowired
    private DealerOrderRepository dealerOrderRepository;
    
    @Autowired
    private DealerRepository dealerRepository;
    
    @Autowired
    private DealerService dealerService;
    
    @GetMapping
    @Operation(summary = "Lấy danh sách đơn hàng đại lý", description = "Lấy tất cả đơn hàng đại lý")
    public ResponseEntity<?> getAllDealerOrders() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerOrder> orders = dealerOrderService.getAllDealerOrders();
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    orders = orders.stream()
                        .filter(order -> order.getDealer() != null && order.getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/all-items")
    @Operation(summary = "Lấy tất cả items", description = "Lấy danh sách tất cả items của tất cả đơn hàng đại lý")
    public ResponseEntity<?> getAllDealerOrderItems() {
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
                error.put("error", "Access denied. Only admin can view all order items");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            List<DealerOrderItem> allItems = dealerOrderItemService.getAllItems();
            return ResponseEntity.ok(allItems);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get all order items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{dealerOrderId}")
    public ResponseEntity<?> getDealerOrderById(@PathVariable UUID dealerOrderId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerOrder order = dealerOrderService.getDealerOrderById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Kiểm tra dealer user chỉ có thể xem order của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (order.getDealer() != null && !order.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view orders for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<?> getDealerOrderByOrderNumber(@PathVariable String orderNumber) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerOrder order = dealerOrderService.getDealerOrderByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Kiểm tra dealer user chỉ có thể xem order của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (order.getDealer() != null && !order.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view orders for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/evm-staff/{evmStaffId}")
    public ResponseEntity<?> getDealerOrdersByEvmStaff(@PathVariable UUID evmStaffId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerOrder> orders = dealerOrderService.getDealerOrdersByEvmStaff(evmStaffId);
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    orders = orders.stream()
                        .filter(order -> order.getDealer() != null && order.getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getDealerOrdersByStatus(@PathVariable String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Validate và convert status string to enum
            com.evdealer.enums.DealerOrderStatus statusEnum = com.evdealer.enums.DealerOrderStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.DealerOrderStatus.values())
                    .map(com.evdealer.enums.DealerOrderStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<DealerOrder> orders = dealerOrderService.getDealerOrdersByStatus(statusEnum.getValue());
            
            // Filter theo dealer nếu là dealer user - Đảm bảo dealer được load trước khi filter
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    orders = orders.stream()
                        .filter(order -> {
                            try {
                                return order.getDealer() != null && order.getDealer().getDealerId().equals(userDealerId);
                            } catch (Exception e) {
                                // Nếu có lỗi lazy loading, bỏ qua order này
                                return false;
                            }
                        })
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<?> getDealerOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerOrder> orders = dealerOrderService.getDealerOrdersByDateRange(startDate, endDate);
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    orders = orders.stream()
                        .filter(order -> order.getDealer() != null && order.getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Tạo đơn hàng đại lý mới", description = "Tạo đơn hàng đại lý mới")
    public ResponseEntity<?> createDealerOrder(@RequestBody DealerOrder dealerOrder) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: DEALER_MANAGER, EVM_STAFF, ADMIN
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only authorized users can create dealer orders");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerOrder createdOrder = dealerOrderService.createDealerOrder(dealerOrder);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{dealerOrderId}")
    public ResponseEntity<?> updateDealerOrder(@PathVariable UUID dealerOrderId, @RequestBody DealerOrder dealerOrderDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: DEALER_MANAGER, EVM_STAFF, ADMIN
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only authorized users can update dealer orders");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerOrder existingOrder = dealerOrderService.getDealerOrderById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Kiểm tra dealer user chỉ có thể update order của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (existingOrder.getDealer() != null && !existingOrder.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only update orders for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            DealerOrder updatedOrder = dealerOrderService.updateDealerOrder(dealerOrderId, dealerOrderDetails);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PutMapping("/{dealerOrderId}/status")
    public ResponseEntity<?> updateDealerOrderStatus(@PathVariable UUID dealerOrderId, @RequestParam String status) {
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
                error.put("error", "Access denied. Only EVM staff or admin can update order status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validate và convert status string to enum
            com.evdealer.enums.DealerOrderStatus statusEnum = com.evdealer.enums.DealerOrderStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.DealerOrderStatus.values())
                    .map(com.evdealer.enums.DealerOrderStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            DealerOrder updatedOrder = dealerOrderService.updateDealerOrderStatus(dealerOrderId, statusEnum.getValue());
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update order status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @DeleteMapping("/{dealerOrderId}")
    public ResponseEntity<?> deleteDealerOrder(@PathVariable UUID dealerOrderId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: EVM_STAFF hoặc ADMIN có thể xóa order
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can delete dealer orders");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            dealerOrderService.deleteDealerOrder(dealerOrderId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Dealer order deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    // ==================== NEW IMPROVED APIs ====================
    
    @PostMapping("/create-detailed")
    @Operation(summary = "Tạo đơn hàng đại lý chi tiết", description = "Tạo đơn hàng đại lý với danh sách xe chi tiết")
    public ResponseEntity<?> createDetailedDealerOrder(@RequestBody CreateDealerOrderRequest request) {
        try {
            var currentUserOpt = securityUtils.getCurrentUser();
            var currentRoleOpt = securityUtils.getCurrentUserRole();
            var currentUsernameOpt = securityUtils.getCurrentUsername();
            
            // Kiểm tra authentication
            if (!currentUserOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                error.put("debug", "currentUser is empty, username: " + currentUsernameOpt.orElse("EMPTY"));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: DEALER_MANAGER, EVM_STAFF, ADMIN
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only authorized users can create dealer orders");
                error.put("currentRole", currentRoleOpt.orElse("EMPTY"));
                error.put("username", currentUsernameOpt.orElse("EMPTY"));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Auto-set dealerId từ current user nếu request không có hoặc null
            if (request.getDealerId() == null && securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    request.setDealerId(userDealerId);
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Cannot determine dealer. Please provide dealerId or ensure your user account is associated with a dealer");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }
            
            // Kiểm tra dealer user chỉ có thể tạo order cho dealer của mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (request.getDealerId() != null && !request.getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only create orders for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            // Validate dealerId is set
            if (request.getDealerId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "dealerId is required. Please provide dealerId in request or ensure your user account is associated with a dealer");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            CreateDealerOrderResponse response = dealerOrderService.createDetailedDealerOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Dealer order creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Dealer order creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{dealerOrderId}/items")
    @Operation(summary = "Lấy chi tiết xe trong đơn hàng", description = "Lấy danh sách xe trong đơn hàng đại lý")
    public ResponseEntity<?> getDealerOrderItems(@PathVariable UUID dealerOrderId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem items của order của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                DealerOrder order = dealerOrderService.getDealerOrderById(dealerOrderId)
                    .orElseThrow(() -> new RuntimeException("Dealer order not found"));
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (!order.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view items for orders of your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            List<DealerOrderItem> items = dealerOrderItemService.getItemsByDealerOrderId(dealerOrderId);
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PostMapping("/{dealerOrderId}/items")
    @Operation(summary = "Thêm xe vào đơn hàng", description = "Thêm xe mới vào đơn hàng đại lý")
    public ResponseEntity<?> addItemToDealerOrder(@PathVariable UUID dealerOrderId, @RequestBody DealerOrderItem item) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: DEALER_MANAGER, EVM_STAFF, ADMIN
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only authorized users can add items to orders");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Set dealer order
            DealerOrder dealerOrder = dealerOrderService.getDealerOrderById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Dealer order not found"));
            
            // Kiểm tra dealer user chỉ có thể thêm items cho order của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (!dealerOrder.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only add items to orders of your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            item.setDealerOrder(dealerOrder);
            DealerOrderItem createdItem = dealerOrderItemService.createDealerOrderItem(item);
            
            // Recalculate totals
            dealerOrderService.recalculateOrderTotals(dealerOrderId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PutMapping("/{dealerOrderId}/items/{itemId}")
    @Operation(summary = "Cập nhật xe trong đơn hàng", description = "Cập nhật thông tin xe trong đơn hàng đại lý")
    public ResponseEntity<?> updateDealerOrderItem(@PathVariable UUID dealerOrderId, @PathVariable UUID itemId, @RequestBody DealerOrderItem itemDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: DEALER_MANAGER, EVM_STAFF, ADMIN
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only authorized users can update items");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể update items của order của dealer mình
            DealerOrder dealerOrder = dealerOrderService.getDealerOrderById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Dealer order not found"));
            
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (!dealerOrder.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only update items for orders of your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            DealerOrderItem updatedItem = dealerOrderItemService.updateDealerOrderItem(itemId, itemDetails);
            
            // Recalculate totals
            dealerOrderService.recalculateOrderTotals(dealerOrderId);
            
            return ResponseEntity.ok(updatedItem);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @DeleteMapping("/{dealerOrderId}/items/{itemId}")
    @Operation(summary = "Xóa xe khỏi đơn hàng", description = "Xóa xe khỏi đơn hàng đại lý")
    public ResponseEntity<?> deleteDealerOrderItem(@PathVariable UUID dealerOrderId, @PathVariable UUID itemId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: DEALER_MANAGER, EVM_STAFF, ADMIN
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only authorized users can delete items");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xóa items của order của dealer mình
            DealerOrder dealerOrder = dealerOrderService.getDealerOrderById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Dealer order not found"));
            
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (!dealerOrder.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only delete items for orders of your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            dealerOrderItemService.deleteDealerOrderItem(itemId);
            
            // Recalculate totals
            dealerOrderService.recalculateOrderTotals(dealerOrderId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Dealer order item deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/{dealerOrderId}/approve")
    @Operation(summary = "Duyệt đơn hàng", description = "Duyệt đơn hàng đại lý")
    public ResponseEntity<?> approveDealerOrder(@PathVariable UUID dealerOrderId, @RequestParam(required = false) @Parameter(description = "ID người duyệt") UUID approvedBy) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể duyệt
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can approve orders");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Sử dụng current user nếu không có approvedBy
            if (approvedBy == null) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent()) {
                    approvedBy = currentUserOpt.get().getUserId();
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Cannot determine approver. Please provide approvedBy parameter");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }
            
            DealerOrder approvedOrder = dealerOrderService.approveDealerOrder(dealerOrderId, approvedBy);
            return ResponseEntity.ok(approvedOrder);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to approve order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/{dealerOrderId}/reject")
    @Operation(summary = "Từ chối đơn hàng", description = "Từ chối đơn hàng đại lý")
    public ResponseEntity<?> rejectDealerOrder(@PathVariable UUID dealerOrderId, @RequestParam @Parameter(description = "Lý do từ chối") String rejectionReason) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể từ chối
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can reject orders");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerOrder rejectedOrder = dealerOrderService.rejectDealerOrder(dealerOrderId, rejectionReason);
            return ResponseEntity.ok(rejectedOrder);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reject order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/{dealerOrderId}/summary")
    @Operation(summary = "Lấy tóm tắt đơn hàng", description = "Lấy thông tin tóm tắt đơn hàng đại lý")
    public ResponseEntity<?> getDealerOrderSummary(@PathVariable UUID dealerOrderId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerOrder order = dealerOrderService.getDealerOrderById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Kiểm tra dealer user chỉ có thể xem summary của order của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (order.getDealer() != null && !order.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view summary for orders of your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            Map<String, Object> summary = dealerOrderService.getDealerOrderSummary(dealerOrderId);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get order summary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/pending-approval")
    @Operation(summary = "Lấy đơn hàng chờ duyệt", description = "Lấy danh sách đơn hàng chờ duyệt")
    public ResponseEntity<?> getPendingApprovalOrders() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerOrder> orders = dealerOrderService.getOrdersByApprovalStatus(ApprovalStatus.PENDING);
            
            // Filter theo dealer nếu là dealer user - Đảm bảo dealer được load trước khi filter
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    orders = orders.stream()
                        .filter(order -> {
                            try {
                                return order.getDealer() != null && order.getDealer().getDealerId().equals(userDealerId);
                            } catch (Exception e) {
                                // Nếu có lỗi lazy loading, bỏ qua order này
                                return false;
                            }
                        })
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/approved")
    @Operation(summary = "Lấy đơn hàng đã duyệt", description = "Lấy danh sách đơn hàng đã duyệt")
    public ResponseEntity<?> getApprovedOrders() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerOrder> orders = dealerOrderService.getOrdersByApprovalStatus(ApprovalStatus.APPROVED);
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    orders = orders.stream()
                        .filter(order -> order.getDealer() != null && order.getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/rejected")
    @Operation(summary = "Lấy đơn hàng bị từ chối", description = "Lấy danh sách đơn hàng bị từ chối")
    public ResponseEntity<?> getRejectedOrders() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerOrder> orders = dealerOrderService.getOrdersByApprovalStatus("REJECTED");
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    orders = orders.stream()
                        .filter(order -> order.getDealer() != null && order.getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // ==================== NEW: DEALER ORDER REQUEST FLOW ====================
    
    @PostMapping("/{dealerOrderId}/request-quotation")
    @Operation(summary = "Đại lý gửi yêu cầu báo giá", description = "Khi đại lý hết xe, quản lý đại lý sẽ tạo request từ DealerOrder để yêu cầu hãng báo giá")
    public ResponseEntity<?> requestQuotationFromFactory(@PathVariable UUID dealerOrderId, @RequestParam(required = false) String notes) {
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
                error.put("error", "Access denied. Only dealer manager or admin can request quotations");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerOrder dealerOrder = dealerOrderService.getDealerOrderById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Dealer order not found with ID: " + dealerOrderId));
            
            // Fix: Nếu order không có dealer, tự động set từ dealer_id trong DB, current user hoặc default dealer
            if (dealerOrder.getDealer() == null) {
                // Try to get dealer_id from DB
                Optional<UUID> dealerIdOpt = dealerOrderRepository.findDealerIdByOrderId(dealerOrderId);
                if (dealerIdOpt.isPresent() && dealerIdOpt.get() != null) {
                    UUID dealerId = dealerIdOpt.get();
                    Dealer dealer = dealerRepository.findById(dealerId)
                        .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + dealerId));
                    dealerOrder.setDealer(dealer);
                    dealerOrder = dealerOrderRepository.save(dealerOrder);
                } else {
                    // Try to get from current user
                    var currentUser = securityUtils.getCurrentUser();
                    if (currentUser.isPresent() && currentUser.get().getDealer() != null) {
                        Dealer userDealer = currentUser.get().getDealer();
                        dealerOrder.setDealer(userDealer);
                        dealerOrder = dealerOrderRepository.save(dealerOrder);
                    } else {
                        // Try to get default dealer (first available dealer)
                        List<Dealer> dealers = dealerService.getAllDealers();
                        if (!dealers.isEmpty()) {
                            Dealer defaultDealer = dealers.get(0);
                            dealerOrder.setDealer(defaultDealer);
                            dealerOrder = dealerOrderRepository.save(dealerOrder);
                        } else {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Dealer order does not have a dealer associated and no dealer available");
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                        }
                    }
                }
            }
            
            // Kiểm tra dealer user chỉ có thể request quotation cho dealer của mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (dealerOrder.getDealer() != null && !dealerOrder.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only request quotations for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            // Validate order is approved
            if (dealerOrder.getApprovalStatus() != ApprovalStatus.APPROVED) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Order must be approved before requesting quotation. Current status: " + (dealerOrder.getApprovalStatus() != null ? dealerOrder.getApprovalStatus().getValue() : "null"));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Update order status to indicate it's waiting for quotation
            dealerOrder.setStatus(DealerOrderStatus.WAITING_FOR_QUOTATION);
            if (notes != null && !notes.trim().isEmpty()) {
                dealerOrder.setNotes((dealerOrder.getNotes() != null ? dealerOrder.getNotes() + "\n" : "") + 
                    "Request for quotation: " + notes);
            }
            dealerOrderService.updateDealerOrder(dealerOrderId, dealerOrder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request for quotation sent successfully. Factory will create quotation based on this order.");
            response.put("dealerOrderId", dealerOrderId);
            response.put("dealerOrderNumber", dealerOrder.getDealerOrderNumber());
            response.put("status", dealerOrder.getStatus());
            response.put("totalAmount", dealerOrder.getTotalAmount());
            response.put("totalQuantity", dealerOrder.getTotalQuantity());
            response.put("nextStep", "Factory staff should create quotation using: POST /api/dealer-quotations/from-order/" + dealerOrderId);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to request quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}

