package com.evdealer.controller;

import com.evdealer.dto.OrderDTO;
import com.evdealer.dto.OrderRequest;
import com.evdealer.entity.Order;
import com.evdealer.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@Tag(name = "Order Management", description = "APIs quản lý đơn hàng")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
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
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderRequest request) {
        try {
            Order createdOrder = orderService.createOrderFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdOrder));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/legacy")
    @Operation(summary = "Tạo đơn hàng mới (Legacy)", description = "Tạo đơn hàng mới từ Order entity (legacy method)")
    public ResponseEntity<OrderDTO> createOrderLegacy(@RequestBody Order order) {
        try {
            Order createdOrder = orderService.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdOrder));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{orderId}")
    @Operation(summary = "Cập nhật đơn hàng", description = "Cập nhật thông tin đơn hàng")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable UUID orderId, @RequestBody OrderRequest request) {
        try {
            Order updatedOrder = orderService.updateOrderFromRequest(orderId, request);
            return ResponseEntity.ok(toDTO(updatedOrder));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{orderId}/status")
    @Operation(summary = "Cập nhật trạng thái đơn hàng", description = "Cập nhật trạng thái đơn hàng")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable UUID orderId, @RequestParam String status) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(toDTO(updatedOrder));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Xóa đơn hàng", description = "Xóa đơn hàng")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
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
