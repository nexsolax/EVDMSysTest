package com.evdealer.service;

import com.evdealer.entity.DealerOrderItem;
import com.evdealer.enums.DealerOrderItemStatus;
import com.evdealer.enums.VehicleStatus;
import com.evdealer.entity.VehicleVariant;
import com.evdealer.entity.VehicleColor;
import com.evdealer.repository.DealerOrderItemRepository;
import com.evdealer.repository.VehicleVariantRepository;
import com.evdealer.repository.VehicleColorRepository;
import com.evdealer.repository.VehicleInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DealerOrderItemService {
    
    @Autowired
    private DealerOrderItemRepository dealerOrderItemRepository;
    
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    
    @Autowired
    private VehicleColorRepository vehicleColorRepository;
    
    @Autowired
    private VehicleInventoryRepository vehicleInventoryRepository;
    
    @Transactional(readOnly = true)
    public List<DealerOrderItem> getAllItems() {
        try {
            return dealerOrderItemRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all order items: " + e.getMessage(), e);
        }
    }
    
    public DealerOrderItem createDealerOrderItem(DealerOrderItem item) {
        // Validate variant exists
        VehicleVariant variant = vehicleVariantRepository.findById(item.getVariant().getVariantId())
            .orElseThrow(() -> new RuntimeException("Variant not found with ID: " + item.getVariant().getVariantId()));
        
        // Validate color exists
        VehicleColor color = vehicleColorRepository.findById(item.getColor().getColorId())
            .orElseThrow(() -> new RuntimeException("Color not found with ID: " + item.getColor().getColorId()));
        
        // Set variant and color
        item.setVariant(variant);
        item.setColor(color);
        
        // Calculate prices
        item.calculatePrices();
        
        // Check inventory availability
        checkInventoryAvailability(variant.getVariantId(), color.getColorId(), item.getQuantity());
        
        return dealerOrderItemRepository.save(item);
    }
    
    public DealerOrderItem updateDealerOrderItem(UUID itemId, DealerOrderItem itemDetails) {
        DealerOrderItem item = dealerOrderItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Dealer order item not found with ID: " + itemId));
        
        // Update fields
        if (itemDetails.getQuantity() != null) {
            item.setQuantity(itemDetails.getQuantity());
        }
        if (itemDetails.getUnitPrice() != null) {
            item.setUnitPrice(itemDetails.getUnitPrice());
        }
        if (itemDetails.getDiscountPercentage() != null) {
            item.setDiscountPercentage(itemDetails.getDiscountPercentage());
        }
        if (itemDetails.getNotes() != null) {
            item.setNotes(itemDetails.getNotes());
        }
        if (itemDetails.getStatus() != null) {
            item.setStatus(itemDetails.getStatus());
        }
        
        // Recalculate prices
        item.calculatePrices();
        
        return dealerOrderItemRepository.save(item);
    }
    
    public void deleteDealerOrderItem(UUID itemId) {
        DealerOrderItem item = dealerOrderItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Dealer order item not found with ID: " + itemId));
        
        // Check if item can be deleted (not confirmed or delivered)
        if (item.getStatus() == DealerOrderItemStatus.CONFIRMED || item.getStatus() == DealerOrderItemStatus.DELIVERED) {
            throw new RuntimeException("Cannot delete confirmed or delivered items");
        }
        
        dealerOrderItemRepository.delete(item);
    }
    
    @Transactional(readOnly = true)
    public List<DealerOrderItem> getItemsByDealerOrderId(UUID dealerOrderId) {
        // findByDealerOrderId already uses LEFT JOIN FETCH for variant and color
        return dealerOrderItemRepository.findByDealerOrderId(dealerOrderId);
    }
    
    public List<DealerOrderItem> getItemsByStatus(String status) {
        // Convert string to enum for validation
        DealerOrderItemStatus statusEnum = DealerOrderItemStatus.fromString(status);
        return dealerOrderItemRepository.findByStatus(statusEnum);
    }
    
    public List<DealerOrderItem> getItemsByDealerOrderIdAndStatus(UUID dealerOrderId, String status) {
        // Convert string to enum for validation
        DealerOrderItemStatus statusEnum = DealerOrderItemStatus.fromString(status);
        return dealerOrderItemRepository.findByDealerOrderIdAndStatus(dealerOrderId, statusEnum);
    }
    
    public Optional<DealerOrderItem> getItemById(UUID itemId) {
        return dealerOrderItemRepository.findById(itemId);
    }
    
    public Long countPendingOrdersByVariant(Integer variantId) {
        List<DealerOrderItemStatus> pendingStatuses = Arrays.asList(
            DealerOrderItemStatus.PENDING,
            DealerOrderItemStatus.CONFIRMED
        );
        return dealerOrderItemRepository.countPendingOrdersByVariant(variantId, pendingStatuses);
    }
    
    public Long sumPendingQuantityByVariant(Integer variantId) {
        List<DealerOrderItemStatus> pendingStatuses = Arrays.asList(
            DealerOrderItemStatus.PENDING,
            DealerOrderItemStatus.CONFIRMED
        );
        Long sum = dealerOrderItemRepository.sumPendingQuantityByVariant(variantId, pendingStatuses);
        return sum != null ? sum : 0L;
    }
    
    private void checkInventoryAvailability(Integer variantId, Integer colorId, Integer requestedQuantity) {
        // Get available inventory for this variant and color
        List<com.evdealer.entity.VehicleInventory> availableInventory = 
            vehicleInventoryRepository.findByVariantVariantIdAndColorColorIdAndStatus(variantId, colorId, VehicleStatus.AVAILABLE);
        
        int availableQuantity = availableInventory.size();
        
        // Get pending orders for this variant
        Long pendingQuantity = sumPendingQuantityByVariant(variantId);
        
        int actuallyAvailable = availableQuantity - pendingQuantity.intValue();
        
        if (actuallyAvailable < requestedQuantity) {
            throw new RuntimeException(
                String.format("Insufficient inventory. Available: %d, Requested: %d, Pending orders: %d", 
                    availableQuantity, requestedQuantity, pendingQuantity)
            );
        }
    }
    
    public BigDecimal calculateTotalAmount(List<DealerOrderItem> items) {
        return items.stream()
            .map(DealerOrderItem::getFinalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public Integer calculateTotalQuantity(List<DealerOrderItem> items) {
        return items.stream()
            .mapToInt(DealerOrderItem::getQuantity)
            .sum();
    }
}
