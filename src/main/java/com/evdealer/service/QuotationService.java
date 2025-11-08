package com.evdealer.service;

import com.evdealer.dto.QuotationRequest;
import com.evdealer.entity.*;
import com.evdealer.repository.*;
import com.evdealer.enums.DealerQuotationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class QuotationService {
    
    @Autowired
    private QuotationRepository quotationRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    
    @Autowired
    private VehicleColorRepository vehicleColorRepository;
    
    public List<Quotation> getAllQuotations() {
        try {
            // Use JOIN FETCH to eagerly load relationships
            return quotationRepository.findAllWithRelationships();
        } catch (Exception e) {
            // Log error and return empty list
            return new java.util.ArrayList<>();
        }
    }
    
    public List<Quotation> getQuotationsByStatus(String status) {
        try {
            return quotationRepository.findByStatus(status);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<Quotation> getQuotationsByCustomer(UUID customerId) {
        try {
            return quotationRepository.findByCustomerCustomerId(customerId);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<Quotation> getQuotationsByUser(UUID userId) {
        try {
            return quotationRepository.findByUserUserId(userId);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<Quotation> getQuotationsByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            return quotationRepository.findByQuotationDateBetween(startDate, endDate);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<Quotation> getExpiredQuotations() {
        try {
            LocalDate currentDate = LocalDate.now();
            return quotationRepository.findExpiredQuotations(currentDate);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public Optional<Quotation> getQuotationById(UUID quotationId) {
        return quotationRepository.findById(quotationId);
    }
    
    public Optional<Quotation> getQuotationByNumber(String quotationNumber) {
        return quotationRepository.findByQuotationNumber(quotationNumber);
    }
    
    public Quotation createQuotation(Quotation quotation) {
        if (quotationRepository.existsByQuotationNumber(quotation.getQuotationNumber())) {
            throw new RuntimeException("Quotation number already exists");
        }
        
        // Validate foreign keys
        if (quotation.getCustomer() != null && quotation.getCustomer().getCustomerId() != null) {
            quotation.setCustomer(customerRepository.findById(quotation.getCustomer().getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + quotation.getCustomer().getCustomerId())));
        }
        
        if (quotation.getUser() != null && quotation.getUser().getUserId() != null) {
            quotation.setUser(userRepository.findById(quotation.getUser().getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + quotation.getUser().getUserId())));
        }
        
        if (quotation.getVariant() != null && quotation.getVariant().getVariantId() != null) {
            quotation.setVariant(vehicleVariantRepository.findById(quotation.getVariant().getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found with id: " + quotation.getVariant().getVariantId())));
        }
        
        if (quotation.getColor() != null && quotation.getColor().getColorId() != null) {
            quotation.setColor(vehicleColorRepository.findById(quotation.getColor().getColorId())
                    .orElseThrow(() -> new RuntimeException("Color not found with id: " + quotation.getColor().getColorId())));
        }
        
        // normalize status using enum
        if (quotation.getStatus() != null) {
            quotation.setStatus(DealerQuotationStatus.fromString(quotation.getStatus()).getValue());
        }
        return quotationRepository.save(quotation);
    }
    
    public Quotation createQuotationFromRequest(QuotationRequest request) {
        // Generate quotation number if not provided
        String quotationNumber = generateQuotationNumber();
        
        // Find related entities
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));
        }
        
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        }
        
        VehicleVariant variant = null;
        if (request.getVariantId() != null) {
            variant = vehicleVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Vehicle variant not found with ID: " + request.getVariantId()));
        }
        
        VehicleColor color = null;
        if (request.getColorId() != null) {
            color = vehicleColorRepository.findById(request.getColorId())
                    .orElseThrow(() -> new RuntimeException("Vehicle color not found with ID: " + request.getColorId()));
        }
        
        // Create quotation entity
        Quotation quotation = new Quotation();
        quotation.setQuotationNumber(quotationNumber);
        quotation.setCustomer(customer);
        quotation.setUser(user);
        quotation.setVariant(variant);
        quotation.setColor(color);
        quotation.setQuotationDate(request.getQuotationDate() != null ? request.getQuotationDate() : LocalDate.now());
        quotation.setTotalPrice(request.getTotalPrice());
        quotation.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        quotation.setFinalPrice(request.getFinalPrice());
        quotation.setValidityDays(request.getValidityDays() != null ? request.getValidityDays() : 7);
        if (request.getStatus() != null) {
            quotation.setStatus(DealerQuotationStatus.fromString(request.getStatus()).getValue());
        } else {
            quotation.setStatus(DealerQuotationStatus.PENDING.getValue());
        }
        quotation.setNotes(request.getNotes());
        
        return quotationRepository.save(quotation);
    }
    
    private String generateQuotationNumber() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        int maxAttempts = 10;
        
        // Retry mechanism với entropy cao hơn (6 chữ số thay vì 4)
        for (int i = 0; i < maxAttempts; i++) {
            String randomStr = String.format("%06d", (int) (Math.random() * 1000000));
            String quotationNumber = "QUO-" + dateStr + "-" + randomStr;
            
            // Check if quotation number already exists
            if (!quotationRepository.existsByQuotationNumber(quotationNumber)) {
                return quotationNumber;
            }
        }
        
        // Fallback: dùng UUID nếu vẫn trùng sau maxAttempts lần
        String uuidSuffix = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "QUO-" + dateStr + "-" + uuidSuffix;
    }
    
    public Quotation updateQuotation(UUID quotationId, Quotation quotationDetails) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        
        quotation.setQuotationNumber(quotationDetails.getQuotationNumber());
        quotation.setCustomer(quotationDetails.getCustomer());
        quotation.setUser(quotationDetails.getUser());
        quotation.setVariant(quotationDetails.getVariant());
        quotation.setColor(quotationDetails.getColor());
        quotation.setQuotationDate(quotationDetails.getQuotationDate());
        quotation.setTotalPrice(quotationDetails.getTotalPrice());
        quotation.setDiscountAmount(quotationDetails.getDiscountAmount());
        quotation.setFinalPrice(quotationDetails.getFinalPrice());
        quotation.setValidityDays(quotationDetails.getValidityDays());
        if (quotationDetails.getStatus() != null) {
            quotation.setStatus(DealerQuotationStatus.fromString(quotationDetails.getStatus()).getValue());
        }
        quotation.setNotes(quotationDetails.getNotes());
        
        return quotationRepository.save(quotation);
    }
    
    public Quotation updateQuotationFromRequest(UUID quotationId, QuotationRequest request) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        
        // Update customer if provided
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));
            quotation.setCustomer(customer);
        }
        
        // Update user if provided
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
            quotation.setUser(user);
        }
        
        // Update variant if provided
        if (request.getVariantId() != null) {
            VehicleVariant variant = vehicleVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Vehicle variant not found with ID: " + request.getVariantId()));
            quotation.setVariant(variant);
        }
        
        // Update color if provided
        if (request.getColorId() != null) {
            VehicleColor color = vehicleColorRepository.findById(request.getColorId())
                    .orElseThrow(() -> new RuntimeException("Vehicle color not found with ID: " + request.getColorId()));
            quotation.setColor(color);
        }
        
        // Update other fields
        if (request.getQuotationDate() != null) {
            quotation.setQuotationDate(request.getQuotationDate());
        }
        if (request.getTotalPrice() != null) {
            quotation.setTotalPrice(request.getTotalPrice());
        }
        if (request.getDiscountAmount() != null) {
            quotation.setDiscountAmount(request.getDiscountAmount());
        }
        if (request.getFinalPrice() != null) {
            quotation.setFinalPrice(request.getFinalPrice());
        }
        if (request.getValidityDays() != null) {
            quotation.setValidityDays(request.getValidityDays());
        }
        if (request.getStatus() != null) {
            quotation.setStatus(DealerQuotationStatus.fromString(request.getStatus()).getValue());
        }
        if (request.getNotes() != null) {
            quotation.setNotes(request.getNotes());
        }
        
        return quotationRepository.save(quotation);
    }
    
    public void deleteQuotation(UUID quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found with id: " + quotationId));
        quotationRepository.delete(quotation);
    }
    
    public Quotation updateQuotationStatus(UUID quotationId, String status) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        // Use DealerQuotationStatus enum for validation and normalization
        DealerQuotationStatus statusEnum = DealerQuotationStatus.fromString(status);
        quotation.setStatus(statusEnum.getValue());
        return quotationRepository.save(quotation);
    }
}
