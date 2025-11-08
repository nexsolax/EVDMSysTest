package com.evdealer.controller;

import com.evdealer.entity.Order;
import com.evdealer.entity.VehicleDelivery;
import com.evdealer.repository.VehicleDeliveryRepository;
import com.evdealer.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/deliveries")
@CrossOrigin(origins = "*")
@Tag(name = "Public Delivery Tracking", description = "Theo dõi giao xe cho khách vãng lai")
public class PublicDeliveryController {

    @Autowired
    private VehicleDeliveryRepository vehicleDeliveryRepository;

    @Autowired
    private OrderService orderService;
    
    private Map<String, Object> deliveryToMap(VehicleDelivery delivery) {
        Map<String, Object> map = new HashMap<>();
        map.put("deliveryId", delivery.getDeliveryId());
        map.put("deliveryDate", delivery.getDeliveryDate());
        map.put("deliveryTime", delivery.getDeliveryTime());
        map.put("deliveryAddress", delivery.getDeliveryAddress());
        map.put("deliveryContactName", delivery.getDeliveryContactName());
        map.put("deliveryContactPhone", delivery.getDeliveryContactPhone());
        map.put("deliveryStatus", delivery.getDeliveryStatus() != null ? delivery.getDeliveryStatus().getValue() : null);
        map.put("deliveryNotes", delivery.getNotes()); // Using notes field instead of deliveryNotes
        map.put("deliveryConfirmationDate", delivery.getDeliveryConfirmationDate());
        map.put("customerSignatureUrl", delivery.getCustomerSignatureUrl());
        map.put("customerSignaturePath", delivery.getCustomerSignaturePath());
        map.put("scheduledDeliveryDate", delivery.getScheduledDeliveryDate());
        map.put("actualDeliveryDate", delivery.getActualDeliveryDate());
        map.put("notes", delivery.getNotes());
        map.put("condition", delivery.getCondition() != null ? delivery.getCondition().toString() : null);
        map.put("createdAt", delivery.getCreatedAt());
        map.put("updatedAt", delivery.getUpdatedAt());
        if (delivery.getOrder() != null) {
            map.put("orderId", delivery.getOrder().getOrderId());
        }
        if (delivery.getInventory() != null) {
            map.put("inventoryId", delivery.getInventory().getInventoryId());
        }
        if (delivery.getCustomer() != null) {
            map.put("customerId", delivery.getCustomer().getCustomerId());
        }
        if (delivery.getDeliveredBy() != null) {
            map.put("deliveredById", delivery.getDeliveredBy().getUserId());
        }
        if (delivery.getDealerOrder() != null) {
            map.put("dealerOrderId", delivery.getDealerOrder().getDealerOrderId());
        }
        if (delivery.getDealerOrderItem() != null) {
            map.put("dealerOrderItemId", delivery.getDealerOrderItem().getItemId());
        }
        return map;
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Xem giao xe theo đơn", description = "Liệt kê các lịch giao xe của đơn hàng")
    public ResponseEntity<?> getByOrder(@PathVariable UUID orderId) {
        try {
            List<VehicleDelivery> deliveries = vehicleDeliveryRepository.findByOrderOrderId(orderId);
            List<Map<String, Object>> deliveryList = deliveries.stream().map(this::deliveryToMap).collect(Collectors.toList());
            return ResponseEntity.ok(deliveryList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve deliveries: " + e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/order-number/{orderNumber}")
    @Operation(summary = "Xem giao xe theo số đơn", description = "Tìm theo số đơn hàng")
    public ResponseEntity<?> getByOrderNumber(@PathVariable String orderNumber) {
        try {
            return orderService.getOrderByOrderNumber(orderNumber)
                    .map(Order::getOrderId)
                    .map(vehicleDeliveryRepository::findByOrderOrderId)
                    .map(deliveries -> {
                        List<Map<String, Object>> deliveryList = deliveries.stream().map(this::deliveryToMap).collect(Collectors.toList());
                        return ResponseEntity.ok(deliveryList);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve deliveries: " + e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/date")
    @Operation(summary = "Xem giao xe theo ngày", description = "Lọc lịch giao xe theo ngày")
    public ResponseEntity<?> getByDate(@RequestParam("date") String date) {
        try {
            List<VehicleDelivery> deliveries = vehicleDeliveryRepository.findByDeliveryDate(LocalDate.parse(date));
            List<Map<String, Object>> deliveryList = deliveries.stream().map(this::deliveryToMap).collect(Collectors.toList());
            return ResponseEntity.ok(deliveryList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve deliveries: " + e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}


