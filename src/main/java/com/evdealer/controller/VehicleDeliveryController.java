package com.evdealer.controller;

import com.evdealer.entity.User;
import com.evdealer.dto.VehicleDeliveryDTO;
import com.evdealer.entity.VehicleDelivery;
import com.evdealer.entity.DealerOrder;
import com.evdealer.entity.DealerOrderItem;
import com.evdealer.entity.Dealer;
import com.evdealer.service.VehicleDeliveryService;
import com.evdealer.service.DealerOrderService;
import com.evdealer.service.DealerOrderItemService;
import com.evdealer.service.DealerService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicle-deliveries")
@CrossOrigin(origins = "*")
@Tag(name = "Vehicle Delivery Management", description = "APIs quản lý giao xe cho đại lý")
public class VehicleDeliveryController {
    
    @Autowired
    private VehicleDeliveryService vehicleDeliveryService;
    
    @Autowired
    private DealerOrderService dealerOrderService;
    
    @Autowired
    private DealerOrderItemService dealerOrderItemService;
    
    @Autowired
    private DealerService dealerService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    public ResponseEntity<?> getAllDeliveries() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<VehicleDelivery> deliveries = vehicleDeliveryService.getAllDeliveries();
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    deliveries = deliveries.stream()
                        .filter(delivery -> delivery.getDealerOrder() != null
                            && delivery.getDealerOrder().getDealer() != null
                            && delivery.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(deliveries.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{deliveryId}")
    public ResponseEntity<?> getDeliveryById(@PathVariable UUID deliveryId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            VehicleDelivery delivery = vehicleDeliveryService.getDeliveryById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
            
            // Kiểm tra dealer user chỉ có thể xem delivery của dealer mình (nếu là dealer order delivery)
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    if (delivery.getDealerOrder() != null && delivery.getDealerOrder().getDealer() != null) {
                        UUID deliveryDealerId = delivery.getDealerOrder().getDealer().getDealerId();
                        if (!deliveryDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view deliveries for your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            return ResponseEntity.ok(toDTO(delivery));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get delivery: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getDeliveriesByOrder(@PathVariable UUID orderId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem deliveries của order của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    // Kiểm tra order thuộc về dealer của user (nếu là dealer order)
                    var orderOpt = dealerOrderService.getDealerOrderById(orderId);
                    if (orderOpt.isPresent() && orderOpt.get().getDealer() != null) {
                        UUID orderDealerId = orderOpt.get().getDealer().getDealerId();
                        if (!orderDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only view deliveries for orders of your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            List<VehicleDelivery> deliveries = vehicleDeliveryService.getDeliveriesByOrder(orderId);
            return ResponseEntity.ok(deliveries.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/inventory/{inventoryId}")
    public ResponseEntity<?> getDeliveriesByInventory(@PathVariable UUID inventoryId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<VehicleDelivery> deliveries = vehicleDeliveryService.getDeliveriesByInventory(inventoryId);
            
            // Filter theo dealer nếu là dealer user (chỉ lọc deliveries từ dealer orders)
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    deliveries = deliveries.stream()
                        .filter(delivery -> delivery.getDealerOrder() != null
                            && delivery.getDealerOrder().getDealer() != null
                            && delivery.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(deliveries.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getDeliveriesByCustomer(@PathVariable UUID customerId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<VehicleDelivery> deliveries = vehicleDeliveryService.getDeliveriesByCustomer(customerId);
            
            // Filter theo dealer nếu là dealer user (chỉ lọc deliveries từ dealer orders)
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    deliveries = deliveries.stream()
                        .filter(delivery -> delivery.getDealerOrder() != null
                            && delivery.getDealerOrder().getDealer() != null
                            && delivery.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(deliveries.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{deliveryStatus}")
    public ResponseEntity<?> getDeliveriesByStatus(@PathVariable String deliveryStatus) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<VehicleDelivery> deliveries = vehicleDeliveryService.getDeliveriesByStatus(deliveryStatus);
            
            // Filter theo dealer nếu là dealer user (chỉ lọc deliveries từ dealer orders)
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    deliveries = deliveries.stream()
                        .filter(delivery -> delivery.getDealerOrder() != null
                            && delivery.getDealerOrder().getDealer() != null
                            && delivery.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(deliveries.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/date/{date}")
    public ResponseEntity<?> getDeliveriesByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<VehicleDelivery> deliveries = vehicleDeliveryService.getDeliveriesByDate(date);
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    deliveries = deliveries.stream()
                        .filter(delivery -> delivery.getDealerOrder() != null
                            && delivery.getDealerOrder().getDealer() != null
                            && delivery.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(deliveries.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<?> getDeliveriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<VehicleDelivery> deliveries = vehicleDeliveryService.getDeliveriesByDateRange(startDate, endDate);
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    deliveries = deliveries.stream()
                        .filter(delivery -> delivery.getDealerOrder() != null
                            && delivery.getDealerOrder().getDealer() != null
                            && delivery.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(deliveries.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/delivered-by/{userId}")
    public ResponseEntity<?> getDeliveriesByDeliveredBy(@PathVariable UUID userId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<VehicleDelivery> deliveries = vehicleDeliveryService.getDeliveriesByDeliveredBy(userId);
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    deliveries = deliveries.stream()
                        .filter(delivery -> delivery.getDealerOrder() != null
                            && delivery.getDealerOrder().getDealer() != null
                            && delivery.getDealerOrder().getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(deliveries.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/customer/{customerId}/status/{status}")
    public ResponseEntity<List<VehicleDeliveryDTO>> getDeliveriesByCustomerAndStatus(@PathVariable UUID customerId, @PathVariable String status) {
        List<VehicleDelivery> deliveries = vehicleDeliveryService.getDeliveriesByCustomerAndStatus(customerId, status);
        return ResponseEntity.ok(deliveries.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueDeliveries() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể xem overdue deliveries
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can view overdue deliveries");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            List<VehicleDelivery> deliveries = vehicleDeliveryService.getOverdueDeliveries();
            return ResponseEntity.ok(deliveries.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get overdue deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createDelivery(@RequestBody VehicleDelivery delivery) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể tạo delivery
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can create deliveries");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleDelivery createdDelivery = vehicleDeliveryService.createDelivery(delivery);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdDelivery));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create delivery: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{deliveryId}")
    public ResponseEntity<?> updateDelivery(@PathVariable UUID deliveryId, @RequestBody VehicleDelivery deliveryDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể update delivery
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can update deliveries");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleDelivery updatedDelivery = vehicleDeliveryService.updateDelivery(deliveryId, deliveryDetails);
            return ResponseEntity.ok(toDTO(updatedDelivery));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update delivery: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PutMapping("/{deliveryId}/status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable UUID deliveryId, @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể update delivery status
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can update delivery status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleDelivery updatedDelivery = vehicleDeliveryService.updateDeliveryStatus(deliveryId, status);
            return ResponseEntity.ok(toDTO(updatedDelivery));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update delivery status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PutMapping("/{deliveryId}/confirm")
    public ResponseEntity<?> confirmDelivery(@PathVariable UUID deliveryId, @RequestBody User deliveredBy) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể confirm delivery
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can confirm deliveries");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleDelivery updatedDelivery = vehicleDeliveryService.confirmDelivery(deliveryId, deliveredBy);
            return ResponseEntity.ok(updatedDelivery);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to confirm delivery: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<?> deleteDelivery(@PathVariable UUID deliveryId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ ADMIN có thể xóa delivery
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete deliveries");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            vehicleDeliveryService.deleteDelivery(deliveryId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete delivery: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    // ==================== NEW DEALER DELIVERY APIs ====================
    
    @PostMapping("/dealer-order/{dealerOrderId}")
    @Operation(summary = "Tạo giao hàng từ đơn hàng đại lý", description = "Tạo giao hàng cho đại lý từ đơn hàng đã duyệt")
    public ResponseEntity<?> createDeliveryFromDealerOrder(@PathVariable UUID dealerOrderId, @RequestBody Map<String, Object> deliveryRequest) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: Chỉ EVM_STAFF hoặc ADMIN có thể tạo delivery từ order
            if (!securityUtils.hasAnyRole("EVM_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only EVM staff or admin can create deliveries from orders");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validate dealer order exists and is approved
            DealerOrder dealerOrder = dealerOrderService.getDealerOrderById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Dealer order not found with ID: " + dealerOrderId));
            
            if (!"APPROVED".equals(dealerOrder.getApprovalStatus())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cannot create delivery for non-approved order. Order status: " + dealerOrder.getApprovalStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Extract delivery details
            LocalDate scheduledDeliveryDate = LocalDate.parse(deliveryRequest.get("scheduledDeliveryDate").toString());
            String deliveryAddress = deliveryRequest.getOrDefault("deliveryAddress", dealerOrder.getDealer().getAddress()).toString();
            String notes = deliveryRequest.getOrDefault("notes", "").toString();
            UUID deliveredBy = deliveryRequest.containsKey("deliveredBy") ? UUID.fromString(deliveryRequest.get("deliveredBy").toString()) : null;
            
            // Create delivery for each order item
            List<DealerOrderItem> items = dealerOrderItemService.getItemsByDealerOrderId(dealerOrderId);
            List<VehicleDelivery> createdDeliveries = new java.util.ArrayList<>();
            
            for (DealerOrderItem item : items) {
                VehicleDelivery delivery = new VehicleDelivery();
                delivery.setDealerOrder(dealerOrder);
                delivery.setDealerOrderItem(item);
                delivery.setScheduledDeliveryDate(scheduledDeliveryDate);
                delivery.setDeliveryAddress(deliveryAddress);
                delivery.setDeliveryStatus("SCHEDULED");
                delivery.setNotes(notes);
                delivery.setCreatedAt(LocalDateTime.now());
                
                if (deliveredBy != null) {
                    User deliveryPerson = new User();
                    deliveryPerson.setUserId(deliveredBy);
                    delivery.setDeliveredBy(deliveryPerson);
                }
                
                VehicleDelivery createdDelivery = vehicleDeliveryService.createDelivery(delivery);
                createdDeliveries.add(createdDelivery);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Deliveries created successfully for dealer order");
            response.put("dealerOrderId", dealerOrderId);
            response.put("dealerOrderNumber", dealerOrder.getDealerOrderNumber());
            response.put("dealerName", dealerOrder.getDealer().getDealerName());
            response.put("deliveryCount", createdDeliveries.size());
            response.put("scheduledDeliveryDate", scheduledDeliveryDate);
            response.put("deliveries", createdDeliveries);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create delivery: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create delivery: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "Lấy giao hàng theo đại lý", description = "Lấy danh sách giao hàng của một đại lý")
    public ResponseEntity<?> getDeliveriesByDealer(@PathVariable UUID dealerId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem deliveries của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view deliveries for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            Dealer dealer = dealerService.getDealerById(dealerId)
                .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + dealerId));
            
            List<VehicleDelivery> deliveries = vehicleDeliveryService.getDeliveriesByDealer(dealerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("dealerId", dealerId);
            response.put("dealerName", dealer.getDealerName());
            response.put("deliveries", deliveries.stream().map(this::toDTO).toList());
            response.put("deliveryCount", deliveries.size());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/dealer/{dealerId}/status/{status}")
    @Operation(summary = "Lấy giao hàng theo đại lý và trạng thái", description = "Lấy danh sách giao hàng của đại lý theo trạng thái")
    public ResponseEntity<?> getDeliveriesByDealerAndStatus(@PathVariable UUID dealerId, @PathVariable String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem deliveries của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view deliveries for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            Dealer dealer = dealerService.getDealerById(dealerId)
                .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + dealerId));
            
            List<VehicleDelivery> deliveries = vehicleDeliveryService.getDeliveriesByDealerAndStatus(dealerId, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("dealerId", dealerId);
            response.put("dealerName", dealer.getDealerName());
            response.put("status", status);
            response.put("deliveries", deliveries.stream().map(this::toDTO).toList());
            response.put("deliveryCount", deliveries.size());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @PutMapping("/{deliveryId}/dealer-confirm")
    @Operation(summary = "Xác nhận nhận xe từ đại lý", description = "Đại lý xác nhận đã nhận xe")
    public ResponseEntity<?> confirmDeliveryByDealer(@PathVariable UUID deliveryId, @RequestBody Map<String, Object> confirmationRequest) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: DEALER_MANAGER, DEALER_STAFF, ADMIN
            if (!securityUtils.hasAnyRole("DEALER_MANAGER", "DEALER_STAFF", "ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only dealer users or admin can confirm deliveries");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            VehicleDelivery delivery = vehicleDeliveryService.getDeliveryById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with ID: " + deliveryId));
            
            // Kiểm tra dealer user chỉ có thể confirm delivery của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    if (delivery.getDealerOrder() != null && delivery.getDealerOrder().getDealer() != null) {
                        UUID deliveryDealerId = delivery.getDealerOrder().getDealer().getDealerId();
                        if (!deliveryDealerId.equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only confirm deliveries for your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    }
                }
            }
            
            if (!"IN_TRANSIT".equals(delivery.getDeliveryStatus())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cannot confirm delivery that is not in transit. Current status: " + delivery.getDeliveryStatus());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            String dealerNotes = confirmationRequest.getOrDefault("dealerNotes", "").toString();
            String condition = confirmationRequest.getOrDefault("condition", "GOOD").toString();
            
            // Update delivery status
            delivery.setDeliveryStatus("DELIVERED");
            delivery.setActualDeliveryDate(LocalDate.now());
            delivery.setNotes(delivery.getNotes() + " [DEALER CONFIRMED: " + dealerNotes + "]");
            delivery.setCondition(condition);
            
            vehicleDeliveryService.updateDelivery(deliveryId, delivery);
            
            // Update order item status
            if (delivery.getDealerOrderItem() != null) {
                DealerOrderItem item = delivery.getDealerOrderItem();
                item.setStatus("DELIVERED");
                dealerOrderItemService.updateDealerOrderItem(item.getItemId(), item);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Delivery confirmed by dealer successfully");
            response.put("deliveryId", deliveryId);
            response.put("dealerOrderId", delivery.getDealerOrder().getDealerOrderId());
            response.put("dealerName", delivery.getDealerOrder().getDealer().getDealerName());
            response.put("actualDeliveryDate", LocalDate.now());
            response.put("condition", condition);
            response.put("dealerNotes", dealerNotes);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to confirm delivery: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/dealer/{dealerId}/summary")
    @Operation(summary = "Tóm tắt giao hàng đại lý", description = "Lấy tóm tắt giao hàng của đại lý")
    public ResponseEntity<?> getDealerDeliverySummary(@PathVariable UUID dealerId, @RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem summary của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view delivery summary for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            Dealer dealer = dealerService.getDealerById(dealerId)
                .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + dealerId));
            
            Map<String, Object> summary = vehicleDeliveryService.getDealerDeliverySummary(dealerId);
            summary.put("dealerId", dealerId);
            summary.put("dealerName", dealer.getDealerName());
            
            return ResponseEntity.ok(summary);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get delivery summary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/dealer/{dealerId}/pending")
    @Operation(summary = "Lấy giao hàng chờ xử lý", description = "Lấy danh sách giao hàng chờ xử lý của đại lý")
    public ResponseEntity<?> getPendingDeliveriesByDealer(@PathVariable UUID dealerId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem pending deliveries của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view pending deliveries for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            Dealer dealer = dealerService.getDealerById(dealerId)
                .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + dealerId));
            
            List<VehicleDelivery> pendingDeliveries = vehicleDeliveryService.getDeliveriesByDealerAndStatus(dealerId, "SCHEDULED");
            List<VehicleDelivery> inTransitDeliveries = vehicleDeliveryService.getDeliveriesByDealerAndStatus(dealerId, "IN_TRANSIT");
            
            Map<String, Object> response = new HashMap<>();
            response.put("dealerId", dealerId);
            response.put("dealerName", dealer.getDealerName());
            response.put("scheduledDeliveries", pendingDeliveries.stream().map(this::toDTO).toList());
            response.put("inTransitDeliveries", inTransitDeliveries.stream().map(this::toDTO).toList());
            response.put("scheduledCount", pendingDeliveries.size());
            response.put("inTransitCount", inTransitDeliveries.size());
            response.put("totalPendingCount", pendingDeliveries.size() + inTransitDeliveries.size());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get pending deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Thống kê giao hàng", description = "Lấy thống kê tổng quan về giao hàng")
    public ResponseEntity<?> getDeliveryStatistics(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate) {
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
                error.put("error", "Access denied. Only EVM staff or admin can view delivery statistics");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Map<String, Object> statistics = vehicleDeliveryService.getDeliveryStatistics();
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get delivery statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private VehicleDeliveryDTO toDTO(VehicleDelivery d) {
        VehicleDeliveryDTO dto = new VehicleDeliveryDTO();
        dto.setDeliveryId(d.getDeliveryId());
        dto.setOrderId(d.getOrder() != null ? d.getOrder().getOrderId() : null);
        dto.setInventoryId(d.getInventory() != null ? d.getInventory().getInventoryId() : null);
        dto.setCustomerId(d.getCustomer() != null ? d.getCustomer().getCustomerId() : null);
        dto.setDeliveryDate(d.getDeliveryDate());
        dto.setDeliveryStatus(d.getDeliveryStatus());
        dto.setDeliveryAddress(d.getDeliveryAddress());
        dto.setDeliveryContactName(d.getDeliveryContactName());
        dto.setDeliveryContactPhone(d.getDeliveryContactPhone());
        dto.setDeliveredBy(d.getDeliveredBy() != null ? d.getDeliveredBy().getUserId() : null);
        dto.setCreatedAt(d.getCreatedAt());
        dto.setUpdatedAt(d.getUpdatedAt());
        return dto;
    }
}
