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
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "Lấy đơn hàng theo ID", description = "Lấy thông tin đơn hàng theo ID")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable UUID orderId) {
        return orderService.getOrderById(orderId)
                .map(order -> ResponseEntity.ok(toDTO(order)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/order-number/{orderNumber}")
    @Operation(summary = "Lấy đơn hàng theo số đơn", description = "Lấy thông tin đơn hàng theo số đơn hàng")
    public ResponseEntity<OrderDTO> getOrderByOrderNumber(@PathVariable String orderNumber) {
        return orderService.getOrderByOrderNumber(orderNumber)
                .map(order -> ResponseEntity.ok(toDTO(order)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Lấy đơn hàng theo khách hàng", description = "Lấy danh sách đơn hàng theo khách hàng")
    public ResponseEntity<List<OrderDTO>> getOrdersByCustomer(@PathVariable UUID customerId) {
        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders.stream().map(this::toDTO).toList());
    }
    
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Lấy đơn hàng theo trạng thái", description = "Lấy danh sách đơn hàng theo trạng thái")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Lấy đơn hàng theo khoảng ngày", description = "Lấy đơn hàng theo khoảng ngày")
    public ResponseEntity<List<OrderDTO>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Order> orders = orderService.getOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/customer/{customerId}/status/{status}")
    @Operation(summary = "Lấy đơn hàng theo khách hàng và trạng thái", description = "Lấy đơn hàng theo khách hàng và trạng thái")
    public ResponseEntity<List<OrderDTO>> getOrdersByCustomerAndStatus(
            @PathVariable UUID customerId, 
            @PathVariable String status) {
        List<Order> orders = orderService.getOrdersByCustomerAndStatus(customerId, status);
        return ResponseEntity.ok(orders.stream().map(this::toDTO).toList());
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
            
            // Cho phép tất cả user đã authenticated tạo order (bao gồm customer, dealer user, EVM_STAFF, ADMIN)
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
            
            // Kiểm tra phân quyền: ADMIN, EVM_STAFF hoặc user tạo order
            if (!securityUtils.isAdmin() && !securityUtils.isEvmStaff()) {
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
                    error.put("error", "Access denied. Only admin, EVM staff or the order creator can update orders");
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
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update order status
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update order status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Order updatedOrder = orderService.updateOrderStatus(orderId, status);
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
            
            orderService.deleteOrder(orderId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Order deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private OrderDTO toDTO(Order o) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(o.getOrderId());
        dto.setOrderNumber(o.getOrderNumber());
        dto.setCustomerId(o.getCustomer() != null ? o.getCustomer().getCustomerId() : null);
        dto.setUserId(o.getUser() != null ? o.getUser().getUserId() : null);
        dto.setInventoryId(o.getInventory() != null ? o.getInventory().getInventoryId() : null);
        dto.setOrderDate(o.getOrderDate());
        dto.setStatus(o.getStatus());
        dto.setTotalAmount(o.getTotalAmount());
        return dto;
    }
}
