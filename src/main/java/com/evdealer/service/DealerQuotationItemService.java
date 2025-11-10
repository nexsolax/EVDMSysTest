package com.evdealer.service;

import com.evdealer.entity.DealerQuotationItem;
import com.evdealer.entity.VehicleVariant;
import com.evdealer.entity.VehicleColor;
import com.evdealer.entity.DealerQuotation;
import com.evdealer.repository.DealerQuotationItemRepository;
import com.evdealer.repository.DealerQuotationRepository;
import com.evdealer.repository.VehicleVariantRepository;
import com.evdealer.repository.VehicleColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DealerQuotationItemService {
    
    @Autowired
    private DealerQuotationItemRepository dealerQuotationItemRepository;
    
    @Autowired
    private DealerQuotationRepository dealerQuotationRepository;
    
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    
    @Autowired
    private VehicleColorRepository vehicleColorRepository;
    
    @Transactional(readOnly = true)
    public List<DealerQuotationItem> getAllItems() {
        try {
            return dealerQuotationItemRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all quotation items: " + e.getMessage(), e);
        }
    }
    
    @Transactional(readOnly = true)
    public List<DealerQuotationItem> getItemsByQuotationId(UUID quotationId) {
        return dealerQuotationItemRepository.findByQuotationQuotationId(quotationId);
    }
    
    @Transactional(readOnly = true)
    public Optional<DealerQuotationItem> getItemById(UUID itemId) {
        return dealerQuotationItemRepository.findById(itemId);
    }
    
    public DealerQuotationItem createItem(DealerQuotationItem item) {
        // Validate quotation exists
        if (item.getQuotation() != null && item.getQuotation().getQuotationId() != null) {
            DealerQuotation quotation = dealerQuotationRepository.findById(item.getQuotation().getQuotationId())
                .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + item.getQuotation().getQuotationId()));
            item.setQuotation(quotation);
        }
        
        // Validate variant exists
        if (item.getVariant() != null && item.getVariant().getVariantId() != null) {
            VehicleVariant variant = vehicleVariantRepository.findById(item.getVariant().getVariantId())
                .orElseThrow(() -> new RuntimeException("Variant not found with ID: " + item.getVariant().getVariantId()));
            item.setVariant(variant);
        }
        
        // Validate color exists
        if (item.getColor() != null && item.getColor().getColorId() != null) {
            VehicleColor color = vehicleColorRepository.findById(item.getColor().getColorId())
                .orElseThrow(() -> new RuntimeException("Color not found with ID: " + item.getColor().getColorId()));
            item.setColor(color);
        }
        
        // Calculate prices
        item.calculatePrices();
        
        return dealerQuotationItemRepository.save(item);
    }
    
    public DealerQuotationItem updateItem(UUID itemId, DealerQuotationItem itemDetails) {
        DealerQuotationItem item = dealerQuotationItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Quotation item not found with ID: " + itemId));
        
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
        if (itemDetails.getVariant() != null && itemDetails.getVariant().getVariantId() != null) {
            VehicleVariant variant = vehicleVariantRepository.findById(itemDetails.getVariant().getVariantId())
                .orElseThrow(() -> new RuntimeException("Variant not found with ID: " + itemDetails.getVariant().getVariantId()));
            item.setVariant(variant);
        }
        if (itemDetails.getColor() != null && itemDetails.getColor().getColorId() != null) {
            VehicleColor color = vehicleColorRepository.findById(itemDetails.getColor().getColorId())
                .orElseThrow(() -> new RuntimeException("Color not found with ID: " + itemDetails.getColor().getColorId()));
            item.setColor(color);
        }
        
        // Recalculate prices
        item.calculatePrices();
        
        return dealerQuotationItemRepository.save(item);
    }
    
    public void deleteItem(UUID itemId) {
        DealerQuotationItem item = dealerQuotationItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Quotation item not found with ID: " + itemId));
        dealerQuotationItemRepository.delete(item);
    }
}

