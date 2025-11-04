package com.evdealer.service;

import com.evdealer.entity.*;
import com.evdealer.repository.*;
import com.evdealer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DealerQuotationService {
    
    @Autowired
    private DealerQuotationRepository dealerQuotationRepository;
    
    @Autowired
    private DealerQuotationItemRepository dealerQuotationItemRepository;
    
    @Autowired
    private DealerRepository dealerRepository;
    
    @Autowired
    private DealerOrderRepository dealerOrderRepository;
    
    @Autowired
    private DealerOrderItemRepository dealerOrderItemRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleVariantRepository vehicleVariantRepository;
    
    @Autowired
    private VehicleColorRepository vehicleColorRepository;
    
    @Autowired
    private DealerInvoiceService dealerInvoiceService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    public List<DealerQuotation> getAllQuotations() {
        // Filter by dealer nếu là dealer user
        if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
            var currentUserOpt = securityUtils.getCurrentUser();
            if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                UUID dealerId = currentUserOpt.get().getDealer().getDealerId();
                return dealerQuotationRepository.findByDealerDealerId(dealerId);
            }
        }
        return dealerQuotationRepository.findAll();
    }
    
    public Optional<DealerQuotation> getQuotationById(UUID quotationId) {
        return dealerQuotationRepository.findById(quotationId);
    }
    
    public Optional<DealerQuotation> getQuotationByNumber(String quotationNumber) {
        return dealerQuotationRepository.findByQuotationNumber(quotationNumber);
    }
    
    public List<DealerQuotation> getQuotationsByDealer(UUID dealerId) {
        return dealerQuotationRepository.findByDealerDealerId(dealerId);
    }
    
    public List<DealerQuotation> getQuotationsByDealerOrder(UUID dealerOrderId) {
        return dealerQuotationRepository.findByDealerOrderDealerOrderId(dealerOrderId);
    }
    
    public List<DealerQuotation> getQuotationsByStatus(String status) {
        return dealerQuotationRepository.findByStatus(status);
    }
    
    public List<DealerQuotation> getQuotationsByEvmStaff(UUID evmStaffId) {
        return dealerQuotationRepository.findByEvmStaffUserId(evmStaffId);
    }
    
    public List<DealerQuotation> getQuotationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return dealerQuotationRepository.findByQuotationDateBetween(startDate, endDate);
    }
    
    public List<DealerQuotation> getExpiredQuotations() {
        return dealerQuotationRepository.findExpiredQuotations(LocalDate.now());
    }
    
    /**
     * Tạo báo giá từ đơn hàng đại lý
     */
    public DealerQuotation createQuotationFromOrder(UUID dealerOrderId, UUID evmStaffId, BigDecimal discountPercentage, String notes) {
        // Validate dealer order
        DealerOrder dealerOrder = dealerOrderRepository.findById(dealerOrderId)
            .orElseThrow(() -> new RuntimeException("Dealer order not found with ID: " + dealerOrderId));
        
        // Validate EVM staff
        User evmStaff = null;
        if (evmStaffId != null) {
            evmStaff = userRepository.findById(evmStaffId)
                .orElseThrow(() -> new RuntimeException("EVM staff not found with ID: " + evmStaffId));
        }
        
        // Check if quotation already exists for this order
        List<DealerQuotation> existingQuotations = dealerQuotationRepository.findByDealerOrderDealerOrderId(dealerOrderId);
        if (!existingQuotations.isEmpty()) {
            DealerQuotation existing = existingQuotations.stream()
                .filter(q -> q.getStatus().equals("pending") || q.getStatus().equals("sent"))
                .findFirst()
                .orElse(null);
            if (existing != null) {
                throw new RuntimeException("Active quotation already exists for this order");
            }
        }
        
        // Create quotation
        DealerQuotation quotation = new DealerQuotation();
        quotation.setQuotationNumber(generateQuotationNumber());
        quotation.setDealer(dealerOrder.getDealer());
        quotation.setDealerOrder(dealerOrder);
        quotation.setEvmStaff(evmStaff);
        quotation.setQuotationDate(LocalDate.now());
        quotation.setValidityDays(30);
        quotation.setStatus("pending");
        quotation.setNotes(notes);
        quotation.setPaymentTerms(dealerOrder.getPaymentTerms() != null ? dealerOrder.getPaymentTerms().toString() : "NET_30");
        quotation.setDeliveryTerms(dealerOrder.getDeliveryTerms() != null ? dealerOrder.getDeliveryTerms().toString() : "FOB_FACTORY");
        quotation.setExpectedDeliveryDate(dealerOrder.getExpectedDeliveryDate());
        
        // Calculate totals from order items
        List<DealerOrderItem> orderItems = dealerOrderItemRepository.findByDealerOrderId(dealerOrderId);
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (DealerOrderItem orderItem : orderItems) {
            DealerQuotationItem quotationItem = new DealerQuotationItem();
            quotationItem.setQuotation(quotation);
            quotationItem.setVariant(orderItem.getVariant());
            quotationItem.setColor(orderItem.getColor());
            quotationItem.setQuantity(orderItem.getQuantity());
            quotationItem.setUnitPrice(orderItem.getUnitPrice());
            
            // Apply discount if provided
            if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
                quotationItem.setDiscountPercentage(discountPercentage);
            } else if (orderItem.getDiscountPercentage() != null) {
                quotationItem.setDiscountPercentage(orderItem.getDiscountPercentage());
            }
            
            quotationItem.calculatePrices();
            subtotal = subtotal.add(quotationItem.getTotalPrice());
        }
        
        // Calculate final amounts
        quotation.setSubtotal(subtotal);
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            quotation.setDiscountPercentage(discountPercentage);
            quotation.setDiscountAmount(subtotal.multiply(discountPercentage).divide(BigDecimal.valueOf(100)));
        }
        quotation.setTotalAmount(subtotal.subtract(quotation.getDiscountAmount() != null ? quotation.getDiscountAmount() : BigDecimal.ZERO));
        
        // Save quotation
        DealerQuotation savedQuotation = dealerQuotationRepository.save(quotation);
        
        // Save quotation items (reload from DB to get fresh quotation reference)
        DealerQuotation reloadedQuotation = dealerQuotationRepository.findById(savedQuotation.getQuotationId())
            .orElse(savedQuotation);
        
        for (DealerOrderItem orderItem : orderItems) {
            DealerQuotationItem quotationItem = new DealerQuotationItem();
            quotationItem.setQuotation(reloadedQuotation);
            quotationItem.setVariant(orderItem.getVariant());
            quotationItem.setColor(orderItem.getColor());
            quotationItem.setQuantity(orderItem.getQuantity());
            quotationItem.setUnitPrice(orderItem.getUnitPrice());
            
            if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
                quotationItem.setDiscountPercentage(discountPercentage);
            } else if (orderItem.getDiscountPercentage() != null) {
                quotationItem.setDiscountPercentage(orderItem.getDiscountPercentage());
            }
            
            quotationItem.calculatePrices();
            dealerQuotationItemRepository.save(quotationItem);
        }
        
        return savedQuotation;
    }
    
    /**
     * Gửi báo giá cho đại lý (chuyển status từ pending sang sent)
     */
    public DealerQuotation sendQuotationToDealer(UUID quotationId) {
        DealerQuotation quotation = dealerQuotationRepository.findById(quotationId)
            .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        if (!quotation.getStatus().equals("pending")) {
            throw new RuntimeException("Quotation must be in 'pending' status to send");
        }
        
        quotation.setStatus("sent");
        return dealerQuotationRepository.save(quotation);
    }
    
    /**
     * Đại lý chấp nhận báo giá -> tạo Invoice
     */
    public DealerInvoice acceptQuotation(UUID quotationId) {
        DealerQuotation quotation = dealerQuotationRepository.findById(quotationId)
            .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        if (!quotation.getStatus().equals("sent")) {
            throw new RuntimeException("Quotation must be in 'sent' status to accept");
        }
        
        // Check if quotation is expired
        if (quotation.getExpiryDate() != null && quotation.getExpiryDate().isBefore(LocalDate.now())) {
            quotation.setStatus("expired");
            dealerQuotationRepository.save(quotation);
            throw new RuntimeException("Quotation has expired");
        }
        
        // Update quotation status
        quotation.setStatus("accepted");
        quotation.setAcceptedAt(LocalDateTime.now());
        dealerQuotationRepository.save(quotation);
        
        // Generate invoice from quotation
        DealerOrder dealerOrder = quotation.getDealerOrder();
        if (dealerOrder == null) {
            throw new RuntimeException("Dealer order not found for quotation");
        }
        
        // Create invoice
        DealerInvoice invoice = new DealerInvoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setDealerOrder(dealerOrder);
        invoice.setEvmStaff(quotation.getEvmStaff());
        invoice.setQuotationId(quotation.getQuotationId()); // Link to quotation
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setSubtotal(quotation.getSubtotal());
        invoice.setTaxAmount(quotation.getTaxAmount());
        invoice.setDiscountAmount(quotation.getDiscountAmount());
        invoice.setTotalAmount(quotation.getTotalAmount());
        invoice.setStatus("issued");
        invoice.setPaymentTermsDays(30);
        invoice.setNotes("Generated from quotation: " + quotation.getQuotationNumber());
        
        DealerInvoice savedInvoice = dealerInvoiceService.createInvoice(invoice);
        
        // Update quotation to converted
        quotation.setStatus("converted");
        dealerQuotationRepository.save(quotation);
        
        return savedInvoice;
    }
    
    /**
     * Đại lý từ chối báo giá
     */
    public DealerQuotation rejectQuotation(UUID quotationId, String reason) {
        DealerQuotation quotation = dealerQuotationRepository.findById(quotationId)
            .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        if (!quotation.getStatus().equals("sent")) {
            throw new RuntimeException("Quotation must be in 'sent' status to reject");
        }
        
        quotation.setStatus("rejected");
        quotation.setRejectedAt(LocalDateTime.now());
        quotation.setRejectionReason(reason);
        
        return dealerQuotationRepository.save(quotation);
    }
    
    public DealerQuotation updateQuotation(UUID quotationId, DealerQuotation quotationDetails) {
        DealerQuotation quotation = dealerQuotationRepository.findById(quotationId)
            .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        if (!quotation.getStatus().equals("pending")) {
            throw new RuntimeException("Only pending quotations can be updated");
        }
        
        // Update fields
        quotation.setNotes(quotationDetails.getNotes());
        quotation.setPaymentTerms(quotationDetails.getPaymentTerms());
        quotation.setDeliveryTerms(quotationDetails.getDeliveryTerms());
        quotation.setExpectedDeliveryDate(quotationDetails.getExpectedDeliveryDate());
        quotation.setValidityDays(quotationDetails.getValidityDays());
        
        return dealerQuotationRepository.save(quotation);
    }
    
    public void deleteQuotation(UUID quotationId) {
        DealerQuotation quotation = dealerQuotationRepository.findById(quotationId)
            .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        if (!quotation.getStatus().equals("pending")) {
            throw new RuntimeException("Only pending quotations can be deleted");
        }
        
        dealerQuotationRepository.delete(quotation);
    }
    
    private String generateQuotationNumber() {
        return "DQ-" + System.currentTimeMillis();
    }
    
    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }
}

