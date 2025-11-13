package com.evdealer.service;

import com.evdealer.entity.User;
import com.evdealer.entity.VehicleDelivery;
import com.evdealer.enums.VehicleDeliveryStatus;
import com.evdealer.repository.VehicleDeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(noRollbackFor = {Exception.class})
public class VehicleDeliveryService {
    
    @Autowired
    private VehicleDeliveryRepository vehicleDeliveryRepository;
    
    @Autowired
    private com.evdealer.service.DealerOrderService dealerOrderService;
    
    @Autowired
    private com.evdealer.service.DealerOrderItemService dealerOrderItemService;
    
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<VehicleDelivery> getAllDeliveries() {
        // Dùng native query để tránh lỗi khi customer/order đã bị xóa
        try {
            List<VehicleDelivery> deliveries = vehicleDeliveryRepository.findAllNative();
            System.out.println("VehicleDeliveryService.getAllDeliveries() - Found " + deliveries.size() + " deliveries (native query)");
            return deliveries;
        } catch (Exception e) {
            System.err.println("VehicleDeliveryService.getAllDeliveries() - Native query failed: " + e.getMessage());
            e.printStackTrace();
            // Fallback: thử findAll thông thường
            try {
                List<VehicleDelivery> deliveries = vehicleDeliveryRepository.findAll();
                System.out.println("VehicleDeliveryService.getAllDeliveries() - Found " + deliveries.size() + " deliveries (simple findAll)");
                return deliveries;
            } catch (Exception e2) {
                System.err.println("VehicleDeliveryService.getAllDeliveries() - Simple findAll also failed: " + e2.getMessage());
                e2.printStackTrace();
                return new java.util.ArrayList<>();
            }
        }
    }
    
    public Optional<VehicleDelivery> getDeliveryById(UUID deliveryId) {
        return vehicleDeliveryRepository.findById(deliveryId);
    }
    
    public List<VehicleDelivery> getDeliveriesByOrder(UUID orderId) {
        return vehicleDeliveryRepository.findByOrderOrderId(orderId);
    }
    
    public List<VehicleDelivery> getDeliveriesByInventory(UUID inventoryId) {
        return vehicleDeliveryRepository.findByInventoryInventoryId(inventoryId);
    }
    
    public List<VehicleDelivery> getDeliveriesByCustomer(UUID customerId) {
        return vehicleDeliveryRepository.findByCustomerCustomerId(customerId);
    }
    
    public List<VehicleDelivery> getDeliveriesByStatus(String deliveryStatus) {
        // Convert string to enum for validation
        VehicleDeliveryStatus statusEnum = VehicleDeliveryStatus.fromString(deliveryStatus);
        return vehicleDeliveryRepository.findByDeliveryStatus(statusEnum);
    }
    
    public List<VehicleDelivery> getDeliveriesByDate(LocalDate date) {
        return vehicleDeliveryRepository.findByDeliveryDate(date);
    }
    
    public List<VehicleDelivery> getDeliveriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return vehicleDeliveryRepository.findByDeliveryDateBetween(startDate, endDate);
    }
    
    public List<VehicleDelivery> getDeliveriesByDeliveredBy(UUID userId) {
        return vehicleDeliveryRepository.findByDeliveredBy(userId);
    }
    
    public List<VehicleDelivery> getDeliveriesByCustomerAndStatus(UUID customerId, String status) {
        // Convert string to enum for validation
        VehicleDeliveryStatus statusEnum = VehicleDeliveryStatus.fromString(status);
        return vehicleDeliveryRepository.findByCustomerAndStatus(customerId, statusEnum);
    }
    
    public List<VehicleDelivery> getOverdueDeliveries() {
        return vehicleDeliveryRepository.findOverdueDeliveries(LocalDate.now(), VehicleDeliveryStatus.DELIVERED);
    }
    
    public VehicleDelivery createDelivery(VehicleDelivery delivery) {
        return vehicleDeliveryRepository.save(delivery);
    }
    
    public VehicleDelivery updateDelivery(UUID deliveryId, VehicleDelivery deliveryDetails) {
        VehicleDelivery delivery = vehicleDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Vehicle delivery not found with id: " + deliveryId));
        
        delivery.setOrder(deliveryDetails.getOrder());
        delivery.setInventory(deliveryDetails.getInventory());
        delivery.setCustomer(deliveryDetails.getCustomer());
        delivery.setDeliveryDate(deliveryDetails.getDeliveryDate());
        delivery.setDeliveryTime(deliveryDetails.getDeliveryTime());
        delivery.setDeliveryAddress(deliveryDetails.getDeliveryAddress());
        delivery.setDeliveryContactName(deliveryDetails.getDeliveryContactName());
        delivery.setDeliveryContactPhone(deliveryDetails.getDeliveryContactPhone());
        delivery.setDeliveryStatus(deliveryDetails.getDeliveryStatus());
        delivery.setNotes(deliveryDetails.getNotes()); // Using notes field instead of deliveryNotes
        delivery.setDeliveredBy(deliveryDetails.getDeliveredBy());
        delivery.setDeliveryConfirmationDate(deliveryDetails.getDeliveryConfirmationDate());
        delivery.setCustomerSignatureUrl(deliveryDetails.getCustomerSignatureUrl());
        delivery.setCustomerSignaturePath(deliveryDetails.getCustomerSignaturePath());
        
        return vehicleDeliveryRepository.save(delivery);
    }
    
    public void deleteDelivery(UUID deliveryId) {
        VehicleDelivery delivery = vehicleDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Vehicle delivery not found with id: " + deliveryId));
        vehicleDeliveryRepository.delete(delivery);
    }
    
    public VehicleDelivery updateDeliveryStatus(UUID deliveryId, String status) {
        VehicleDelivery delivery = vehicleDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Vehicle delivery not found with id: " + deliveryId));
        VehicleDeliveryStatus statusEnum = VehicleDeliveryStatus.fromString(status);
        delivery.setDeliveryStatus(statusEnum);
        return vehicleDeliveryRepository.save(delivery);
    }
    
    public VehicleDelivery confirmDelivery(UUID deliveryId, User deliveredBy) {
        VehicleDelivery delivery = vehicleDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Vehicle delivery not found with id: " + deliveryId));
        
        // Set status = IN_TRANSIT khi EVM xác nhận bắt đầu vận chuyển (thay vì DELIVERED)
        delivery.setDeliveryStatus(VehicleDeliveryStatus.IN_TRANSIT);
        delivery.setDeliveredBy(deliveredBy);
        delivery.setDeliveryConfirmationDate(LocalDateTime.now());
        
        return vehicleDeliveryRepository.save(delivery);
    }
    
    // Additional methods for dealer delivery APIs
    public List<VehicleDelivery> getDeliveriesByDealer(UUID dealerId) {
        // This would need to be implemented based on dealer relationship
        // For now, return empty list
        return new java.util.ArrayList<>();
    }
    
    public List<VehicleDelivery> getDeliveriesByDealerOrder(UUID dealerOrderId) {
        return vehicleDeliveryRepository.findByDealerOrderDealerOrderId(dealerOrderId);
    }
    
    public List<VehicleDelivery> getDeliveriesByDealerAndStatus(UUID dealerId, String status) {
        // This would need to be implemented based on dealer relationship
        // For now, return empty list
        return new java.util.ArrayList<>();
    }
    
    public java.util.Map<String, Object> getDealerDeliverySummary(UUID dealerId) {
        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        
        List<VehicleDelivery> deliveries = getDeliveriesByDealer(dealerId);
        long totalDeliveries = deliveries.size();
        long completedDeliveries = deliveries.stream()
                .filter(d -> d.getDeliveryStatus() == VehicleDeliveryStatus.DELIVERED)
                .count();
        long pendingDeliveries = deliveries.stream()
                .filter(d -> d.getDeliveryStatus() == VehicleDeliveryStatus.SCHEDULED || d.getDeliveryStatus() == VehicleDeliveryStatus.IN_TRANSIT)
                .count();
        
        summary.put("totalDeliveries", totalDeliveries);
        summary.put("completedDeliveries", completedDeliveries);
        summary.put("pendingDeliveries", pendingDeliveries);
        
        return summary;
    }
    
    public java.util.Map<String, Object> getDeliveryStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        long totalDeliveries = vehicleDeliveryRepository.count();
        long completedDeliveries = vehicleDeliveryRepository.countByDeliveryStatus(VehicleDeliveryStatus.DELIVERED);
        long pendingDeliveries = vehicleDeliveryRepository.countByDeliveryStatus(VehicleDeliveryStatus.SCHEDULED) + 
                                 vehicleDeliveryRepository.countByDeliveryStatus(VehicleDeliveryStatus.IN_TRANSIT);
        long cancelledDeliveries = vehicleDeliveryRepository.countByDeliveryStatus(VehicleDeliveryStatus.CANCELLED);
        
        stats.put("totalDeliveries", totalDeliveries);
        stats.put("completedDeliveries", completedDeliveries);
        stats.put("pendingDeliveries", pendingDeliveries);
        stats.put("cancelledDeliveries", cancelledDeliveries);
        
        return stats;
    }
    
    /**
     * Tự động tạo VehicleDelivery sau khi DealerPayment completed và Invoice fully paid
     * Chỉ tạo delivery nếu chưa có delivery nào cho dealer order này
     */
    public void createDeliveryFromDealerOrderAfterPayment(UUID dealerOrderId) {
        // Kiểm tra xem đã có delivery chưa
        List<VehicleDelivery> existingDeliveries = vehicleDeliveryRepository.findByDealerOrderDealerOrderId(dealerOrderId);
        if (!existingDeliveries.isEmpty()) {
            // Đã có delivery, không tạo mới
            return;
        }
        
        // Load dealer order
        java.util.Optional<com.evdealer.entity.DealerOrder> dealerOrderOpt = dealerOrderService.getDealerOrderById(dealerOrderId);
        if (dealerOrderOpt.isEmpty()) {
            throw new RuntimeException("Dealer order not found with ID: " + dealerOrderId);
        }
        
        com.evdealer.entity.DealerOrder dealerOrder = dealerOrderOpt.get();
        
        // Lấy items từ dealer order
        java.util.List<com.evdealer.entity.DealerOrderItem> items = dealerOrderItemService.getItemsByDealerOrderId(dealerOrderId);
        if (items == null || items.isEmpty()) {
            return; // Không có items, không tạo delivery
        }
        
        // Tạo delivery cho mỗi item
        LocalDate scheduledDate = dealerOrder.getExpectedDeliveryDate() != null ? 
            dealerOrder.getExpectedDeliveryDate() : 
            java.time.LocalDate.now().plusDays(7); // Mặc định 7 ngày sau
        
        String deliveryAddress = dealerOrder.getDealer() != null && dealerOrder.getDealer().getAddress() != null ?
            dealerOrder.getDealer().getAddress() : "";
        
        for (com.evdealer.entity.DealerOrderItem item : items) {
            VehicleDelivery delivery = new VehicleDelivery();
            delivery.setDealerOrder(dealerOrder);
            delivery.setDealerOrderItem(item);
            delivery.setScheduledDeliveryDate(scheduledDate);
            delivery.setDeliveryDate(scheduledDate);
            delivery.setDeliveryAddress(deliveryAddress);
            delivery.setDeliveryStatus(com.evdealer.enums.VehicleDeliveryStatus.SCHEDULED);
            delivery.setIsEarlyDelivery(false); // Không phải giao trước vì đã thanh toán đủ
            delivery.setNotes("Tự động tạo sau khi thanh toán đủ");
            
            vehicleDeliveryRepository.save(delivery);
        }
        
        // Cập nhật DealerOrder status = READY_FOR_DELIVERY sau khi tạo delivery
        try {
            dealerOrderService.updateDealerOrderStatus(
                dealerOrderId, 
                com.evdealer.enums.DealerOrderStatus.READY_FOR_DELIVERY.getValue()
            );
        } catch (Exception e) {
            // Log error nhưng không fail delivery creation
            System.err.println("Failed to update dealer order status to READY_FOR_DELIVERY: " + e.getMessage());
        }
    }
}
