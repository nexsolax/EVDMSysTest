package com.evdealer.service;

import com.evdealer.dto.QuotationRequest;
import com.evdealer.entity.*;
import com.evdealer.repository.*;
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
            System.err.println("Error fetching quotations: " + e.getMessage());
            e.printStackTrace();
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
        // normalize status
        quotation.setStatus(normalizeStatus(quotation.getStatus()));
        return quotationRepository.save(quotation);
    }
    
    public Quotation createQuotationFromRequest(QuotationRequest request) {
        // Generate quotation number if not provided
        String quotationNumber = generateQuotationNumber();
        
        // Find related entities
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        
        VehicleVariant variant = vehicleVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Vehicle variant not found with ID: " + request.getVariantId()));
        
        VehicleColor color = vehicleColorRepository.findById(request.getColorId())
                .orElseThrow(() -> new RuntimeException("Vehicle color not found with ID: " + request.getColorId()));
        
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
        quotation.setStatus(normalizeStatus(request.getStatus()));
        quotation.setNotes(request.getNotes());
        
        return quotationRepository.save(quotation);
    }
    
    private String generateQuotationNumber() {
        // Generate quotation number in format: QUO-YYYYMMDD-XXXX
        String dateStr = LocalDate.now().toString().replace("-", "");
        String randomStr = String.format("%04d", (int) (Math.random() * 10000));
        return "QUO-" + dateStr + "-" + randomStr;
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
        quotation.setStatus(normalizeStatus(quotationDetails.getStatus()));
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
            quotation.setStatus(normalizeStatus(request.getStatus()));
        }
        if (request.getNotes() != null) {
            quotation.setNotes(request.getNotes());
        }
        
        return quotationRepository.save(quotation);
    }
    
    public void deleteQuotation(UUID quotationId) {
        if (!quotationRepository.existsById(quotationId)) {
            throw new RuntimeException("Quotation not found");
        }
        quotationRepository.deleteById(quotationId);
    }
    
    public Quotation updateQuotationStatus(UUID quotationId, String status) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        quotation.setStatus(normalizeStatus(status));
        return quotationRepository.save(quotation);
    }

    private String normalizeStatus(String raw) {
        if (raw == null) {
            return "pending";
        }
        String value = raw.trim().toLowerCase();
        if (value.isEmpty() || value.equals("undefined") || value.equals("null")) {
            return "pending";
        }
        // allowed statuses per business rules
        switch (value) {
            case "pending":
            case "accepted":
            case "rejected":
            case "expired":
                return value;
            default:
                // fallback to pending if unknown
                return "pending";
        }
    }
}
