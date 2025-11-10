package com.evdealer.controller;

import com.evdealer.dto.OrderRequest;
import com.evdealer.entity.Order;
import com.evdealer.service.OrderService;
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
@RequestMapping("/api/public/orders")
@CrossOrigin(origins = "*")
@Tag(name = "Public Order Management", description = "APIs đặt hàng cho khách vãng lai - không cần đăng nhập")
public class PublicOrderController {
    
    @Autowired
    private OrderService orderService;
    
    @PostMapping
    @Operation(summary = "Tạo đơn hàng", description = "Khách vãng lai có thể tạo đơn hàng mà không cần đăng nhập")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            Order createdOrder = orderService.createOrderFromRequest(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", createdOrder.getOrderId());
            response.put("orderNumber", createdOrder.getOrderNumber());
            response.put("status", createdOrder.getStatus() != null ? createdOrder.getStatus().getValue() : null);
            response.put("orderDate", createdOrder.getOrderDate());
            response.put("totalAmount", createdOrder.getTotalAmount());
            response.put("depositAmount", createdOrder.getDepositAmount());
            response.put("balanceAmount", createdOrder.getBalanceAmount());
            response.put("paymentMethod", createdOrder.getPaymentMethod() != null ? createdOrder.getPaymentMethod().getValue() : null);
            response.put("orderType", createdOrder.getOrderType() != null ? createdOrder.getOrderType().toString() : null);
            response.put("paymentStatus", createdOrder.getPaymentStatus() != null ? createdOrder.getPaymentStatus().name() : null);
            response.put("deliveryStatus", createdOrder.getDeliveryStatus() != null ? createdOrder.getDeliveryStatus().name() : null);
            response.put("notes", createdOrder.getNotes());
            
            if (createdOrder.getCustomer() != null) {
                response.put("customerId", createdOrder.getCustomer().getCustomerId());
            }
            if (createdOrder.getInventory() != null) {
                response.put("inventoryId", createdOrder.getInventory().getInventoryId());
            }
            if (createdOrder.getQuotation() != null) {
                response.put("quotationId", createdOrder.getQuotation().getQuotationId());
            }
            if (createdOrder.getUser() != null) {
                response.put("userId", createdOrder.getUser().getUserId());
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Order creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Order creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "Xem đơn hàng", description = "Khách vãng lai có thể xem thông tin đơn hàng")
    public ResponseEntity<?> getOrderById(@PathVariable UUID orderId) {
        try {
            return orderService.getOrderById(orderId)
                    .map(order -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("orderId", order.getOrderId());
                        response.put("orderNumber", order.getOrderNumber());
                        response.put("status", order.getStatus() != null ? order.getStatus().getValue() : null);
                        response.put("orderDate", order.getOrderDate());
                        response.put("totalAmount", order.getTotalAmount());
                        response.put("depositAmount", order.getDepositAmount());
                        response.put("balanceAmount", order.getBalanceAmount());
                        response.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod().getValue() : null);
                        response.put("orderType", order.getOrderType() != null ? order.getOrderType().toString() : null);
                        response.put("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
                        response.put("deliveryStatus", order.getDeliveryStatus() != null ? order.getDeliveryStatus().name() : null);
                        response.put("notes", order.getNotes());
                        response.put("deliveryDate", order.getDeliveryDate());
                        
                        // Chỉ trả về ID thay vì toàn bộ entity để tránh lazy loading
                        if (order.getCustomer() != null) {
                            response.put("customerId", order.getCustomer().getCustomerId());
                        }
                        if (order.getInventory() != null) {
                            response.put("inventoryId", order.getInventory().getInventoryId());
                        }
                        if (order.getQuotation() != null) {
                            response.put("quotationId", order.getQuotation().getQuotationId());
                        }
                        if (order.getUser() != null) {
                            response.put("userId", order.getUser().getUserId());
                        }
                        
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/order-number/{orderNumber}")
    @Operation(summary = "Xem đơn hàng theo số", description = "Khách vãng lai có thể xem đơn hàng theo số đơn hàng")
    public ResponseEntity<?> getOrderByOrderNumber(@PathVariable String orderNumber) {
        try {
            return orderService.getOrderByOrderNumber(orderNumber)
                    .map(order -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("orderId", order.getOrderId());
                        response.put("orderNumber", order.getOrderNumber());
                        response.put("status", order.getStatus() != null ? order.getStatus().getValue() : null);
                        response.put("orderDate", order.getOrderDate());
                        response.put("totalAmount", order.getTotalAmount());
                        response.put("depositAmount", order.getDepositAmount());
                        response.put("balanceAmount", order.getBalanceAmount());
                        response.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod().getValue() : null);
                        response.put("orderType", order.getOrderType() != null ? order.getOrderType().toString() : null);
                        response.put("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
                        response.put("deliveryStatus", order.getDeliveryStatus() != null ? order.getDeliveryStatus().name() : null);
                        response.put("notes", order.getNotes());
                        response.put("deliveryDate", order.getDeliveryDate());
                        
                        // Chỉ trả về ID thay vì toàn bộ entity để tránh lazy loading
                        if (order.getCustomer() != null) {
                            response.put("customerId", order.getCustomer().getCustomerId());
                        }
                        if (order.getInventory() != null) {
                            response.put("inventoryId", order.getInventory().getInventoryId());
                        }
                        if (order.getQuotation() != null) {
                            response.put("quotationId", order.getQuotation().getQuotationId());
                        }
                        if (order.getUser() != null) {
                            response.put("userId", order.getUser().getUserId());
                        }
                        
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Hủy đơn hàng", description = "Khách vãng lai có thể hủy đơn hàng. Hệ thống sẽ tự động cập nhật inventory status về 'available' nếu đã reserved.")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID orderId, @RequestParam(required = false) String reason) {
        try {
            // Use cancelOrder method which handles inventory status update automatically
            Order cancelledOrder = orderService.cancelOrder(orderId);
            
            if (reason != null && !reason.trim().isEmpty()) {
                cancelledOrder.setNotes((cancelledOrder.getNotes() != null ? cancelledOrder.getNotes() + "\n" : "") + 
                              "Cancellation reason: " + reason);
                orderService.updateOrder(orderId, cancelledOrder);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order cancelled successfully. Inventory status has been updated if applicable.");
            response.put("orderId", orderId);
            response.put("orderNumber", cancelledOrder.getOrderNumber());
            response.put("status", cancelledOrder.getStatus() != null ? cancelledOrder.getStatus().getValue() : null);
            response.put("reason", reason);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to cancel order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{orderId}/status")
    @Operation(summary = "Xem trạng thái đơn hàng", description = "Khách vãng lai có thể xem trạng thái đơn hàng")
    public ResponseEntity<?> getOrderStatus(@PathVariable UUID orderId) {
        try {
            return orderService.getOrderById(orderId)
                    .map(order -> {
                        Map<String, Object> status = new HashMap<>();
                        status.put("orderId", order.getOrderId());
                        status.put("orderNumber", order.getOrderNumber());
                        status.put("status", order.getStatus() != null ? order.getStatus().getValue() : null);
                        status.put("orderDate", order.getOrderDate());
                        status.put("totalAmount", order.getTotalAmount());
                        status.put("notes", order.getNotes());
                        return ResponseEntity.ok(status);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve order status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/track/{orderNumber}")
    @Operation(summary = "Theo dõi đơn hàng", description = "Khách vãng lai có thể theo dõi đơn hàng bằng số đơn hàng")
    public ResponseEntity<?> trackOrder(@PathVariable String orderNumber) {
        try {
            return orderService.getOrderByOrderNumber(orderNumber)
                    .map(order -> {
                        Map<String, Object> tracking = new HashMap<>();
                        tracking.put("orderNumber", order.getOrderNumber());
                        tracking.put("status", order.getStatus() != null ? order.getStatus().getValue() : null);
                        tracking.put("orderDate", order.getOrderDate());
                        tracking.put("totalAmount", order.getTotalAmount());
                        tracking.put("customerName", order.getCustomer() != null ? 
                            order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() : "N/A");
                        tracking.put("vehicleInfo", order.getQuotation() != null && order.getQuotation().getVariant() != null ?
                            order.getQuotation().getVariant().getModel().getBrand().getBrandName() + " " +
                            order.getQuotation().getVariant().getModel().getModelName() + " " +
                            order.getQuotation().getVariant().getVariantName() : "N/A");
                        return ResponseEntity.ok(tracking);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to track order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
