package com.evdealer.controller;

import com.evdealer.dto.OrderDTO;
import com.evdealer.dto.OrderRequest;
import com.evdealer.entity.Order;
import com.evdealer.service.OrderService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@Tag(name = "Order Management", description = "APIs quản lý đơn hàng")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy danh sách đơn hàng", description = "Lấy tất cả đơn hàng")
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            if (orders == null) {
                orders = new java.util.ArrayList<>();
            }
            List<OrderDTO> orderList = orders.stream()
                .map(order -> {
                    try {
                        return toDTO(order);
                    } catch (Exception e) {
                        // Return basic DTO if mapping fails
                        OrderDTO errorDTO = new OrderDTO();
                        try {
                            errorDTO.setOrderId(order.getOrderId());
                        } catch (Exception e2) {
                            // Skip if order is null
                        }
                        return errorDTO;
                    }
                })
                .toList();
            return ResponseEntity.ok(orderList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "Lấy đơn hàng theo ID", description = "Lấy thông tin đơn hàng theo ID")
    public ResponseEntity<?> getOrderById(@PathVariable UUID orderId) {
        try {
            return orderService.getOrderById(orderId)
                    .map(order -> ResponseEntity.ok(toDTO(order)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/order-number/{orderNumber}")
    @Operation(summary = "Lấy đơn hàng theo số đơn", description = "Lấy thông tin đơn hàng theo số đơn hàng")
    public ResponseEntity<?> getOrderByOrderNumber(@PathVariable String orderNumber) {
        try {
            return orderService.getOrderByOrderNumber(orderNumber)
                    .map(order -> ResponseEntity.ok(toDTO(order)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Lấy đơn hàng theo khách hàng", description = "Lấy danh sách đơn hàng theo khách hàng")
    public ResponseEntity<?> getOrdersByCustomer(@PathVariable UUID customerId) {
        try {
            List<Order> orders = orderService.getOrdersByCustomer(customerId);
            return ResponseEntity.ok(orders.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Lấy đơn hàng theo trạng thái", description = "Lấy danh sách đơn hàng theo trạng thái")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            // Validate và convert status string to enum
            com.evdealer.enums.OrderStatus statusEnum = com.evdealer.enums.OrderStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.OrderStatus.values())
                    .map(com.evdealer.enums.OrderStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<Order> orders = orderService.getOrdersByStatus(statusEnum.getValue());
            return ResponseEntity.ok(orders.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Lấy đơn hàng theo khoảng ngày", description = "Lấy đơn hàng theo khoảng ngày")
    public ResponseEntity<?> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // Validate date range
            if (startDate == null || endDate == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Start date and end date are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (startDate.isAfter(endDate)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Start date cannot be after end date");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<Order> orders = orderService.getOrdersByDateRange(startDate, endDate);
            return ResponseEntity.ok(orders.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/customer/{customerId}/status/{status}")
    @Operation(summary = "Lấy đơn hàng theo khách hàng và trạng thái", description = "Lấy đơn hàng theo khách hàng và trạng thái")
    public ResponseEntity<?> getOrdersByCustomerAndStatus(
            @PathVariable UUID customerId, 
            @PathVariable String status) {
        try {
            // Validate và convert status string to enum
            com.evdealer.enums.OrderStatus statusEnum = com.evdealer.enums.OrderStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.OrderStatus.values())
                    .map(com.evdealer.enums.OrderStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<Order> orders = orderService.getOrdersByCustomerAndStatus(customerId, statusEnum.getValue());
            return ResponseEntity.ok(orders.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    
    @PostMapping
    @Operation(summary = "Tạo đơn hàng mới", description = "Tạo đơn hàng mới từ OrderRequest DTO")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Cho phép tất cả user đã authenticated tạo order (bao gồm customer, dealer user, ADMIN)
            Order createdOrder = orderService.createOrderFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdOrder));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/legacy")
    @Operation(summary = "Tạo đơn hàng mới (Legacy)", description = "Tạo đơn hàng mới từ Order entity (legacy method)")
    public ResponseEntity<?> createOrderLegacy(@RequestBody Order order) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Cho phép tất cả user đã authenticated tạo order
            Order createdOrder = orderService.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdOrder));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{orderId}")
    @Operation(summary = "Cập nhật đơn hàng", description = "Cập nhật thông tin đơn hàng")
    public ResponseEntity<?> updateOrder(@PathVariable UUID orderId, @RequestBody OrderRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Lấy order hiện tại để kiểm tra ownership
            Order existingOrder = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Kiểm tra phân quyền: ADMIN, DEALER_STAFF hoặc user tạo order
            if (!securityUtils.isAdmin() && !securityUtils.hasAnyRole("DEALER_STAFF")) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent()) {
                    UUID currentUserId = currentUserOpt.get().getUserId();
                    // User chỉ có thể update order của chính mình (nếu order có user)
                    if (existingOrder.getUser() != null && !existingOrder.getUser().getUserId().equals(currentUserId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only update your own orders");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only admin, dealer staff or the order creator can update orders");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            Order updatedOrder = orderService.updateOrderFromRequest(orderId, request);
            return ResponseEntity.ok(toDTO(updatedOrder));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{orderId}/status")
    @Operation(summary = "Cập nhật trạng thái đơn hàng", description = "Cập nhật trạng thái đơn hàng")
    public ResponseEntity<?> updateOrderStatus(@PathVariable UUID orderId, @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc DEALER_STAFF mới có thể update order status
            if (!securityUtils.hasAnyRole("ADMIN", "DEALER_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or dealer staff can update order status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validate và convert status string to enum
            com.evdealer.enums.OrderStatus statusEnum = com.evdealer.enums.OrderStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.OrderStatus.values())
                    .map(com.evdealer.enums.OrderStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Order updatedOrder = orderService.updateOrderStatus(orderId, statusEnum.getValue());
            return ResponseEntity.ok(toDTO(updatedOrder));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update order status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update order status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Xóa đơn hàng", description = "Xóa đơn hàng")
    public ResponseEntity<?> deleteOrder(@PathVariable UUID orderId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa order
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete orders");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Kiểm tra orderId có hợp lệ không
            if (orderId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Order ID cannot be null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Kiểm tra xem order có tồn tại không trước khi xóa
            Optional<Order> existingOrder = orderService.getOrderById(orderId);
            if (!existingOrder.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Order not found with id: " + orderId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            orderService.deleteOrder(orderId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Order deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            String errorMessage = e.getMessage();
            error.put("error", errorMessage);
            // Phân biệt giữa entity không tồn tại và lỗi foreign key constraint
            if (errorMessage != null && errorMessage.contains("Cannot delete")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else if (errorMessage != null && errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private OrderDTO toDTO(Order o) {
        if (o == null) {
            return new OrderDTO();
        }
        OrderDTO dto = new OrderDTO();
        try {
            dto.setOrderId(o.getOrderId());
        } catch (Exception e) {
            // Skip if error
        }
        try {
            dto.setOrderNumber(o.getOrderNumber());
        } catch (Exception e) {
            // Skip if error
        }
        try {
            dto.setCustomerId(o.getCustomer() != null ? o.getCustomer().getCustomerId() : null);
        } catch (Exception e) {
            // Relationship not loaded, skip
        }
        try {
            dto.setUserId(o.getUser() != null ? o.getUser().getUserId() : null);
        } catch (Exception e) {
            // Relationship not loaded, skip
        }
        try {
            dto.setInventoryId(o.getInventory() != null ? o.getInventory().getInventoryId() : null);
        } catch (Exception e) {
            // Relationship not loaded, skip
        }
        try {
            dto.setOrderDate(o.getOrderDate());
        } catch (Exception e) {
            // Skip if error
        }
        try {
            dto.setStatus(o.getStatus() != null ? o.getStatus().getValue() : null);
        } catch (Exception e) {
            // Skip if error
        }
        try {
            dto.setTotalAmount(o.getTotalAmount());
        } catch (Exception e) {
            // Skip if error
        }
        return dto;
    }
}
