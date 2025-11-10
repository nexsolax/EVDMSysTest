package com.evdealer.service;

import com.evdealer.entity.*;
import com.evdealer.enums.DealerInvoiceStatus;
import com.evdealer.enums.DealerQuotationStatus;
import com.evdealer.repository.*;
import com.evdealer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    
    @Transactional(readOnly = true)
    public List<DealerQuotation> getAllQuotations() {
        try {
            // Filter by dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent()) {
                    var currentUser = currentUserOpt.get();
                    if (currentUser.getDealer() != null) {
                        UUID dealerId = currentUser.getDealer().getDealerId();
                        return dealerQuotationRepository.findByDealerDealerId(dealerId);
                    }
                }
            }
            // Use findAllWithDetails to eagerly load dealer and dealerOrder
            return dealerQuotationRepository.findAllWithDetails();
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    @Transactional(readOnly = true)
    public List<DealerQuotation> getQuotationsByDealerOrder(UUID dealerOrderId) {
        return dealerQuotationRepository.findByDealerOrderDealerOrderId(dealerOrderId);
    }
    
    @Transactional(readOnly = true)
    public Optional<DealerQuotation> getQuotationById(UUID quotationId) {
        return dealerQuotationRepository.findByIdWithDetails(quotationId);
    }
    
    public Optional<DealerQuotation> getQuotationByNumber(String quotationNumber) {
        return dealerQuotationRepository.findByQuotationNumber(quotationNumber);
    }
    
    public List<DealerQuotation> getQuotationsByDealer(UUID dealerId) {
        return dealerQuotationRepository.findByDealerDealerId(dealerId);
    }
    
    public List<DealerQuotation> getQuotationsByStatus(String status) {
        com.evdealer.enums.DealerQuotationStatus statusEnum = com.evdealer.enums.DealerQuotationStatus.fromString(status);
        return dealerQuotationRepository.findByStatus(statusEnum);
    }
    
    public List<DealerQuotation> getQuotationsByEvmStaff(UUID evmStaffId) {
        return dealerQuotationRepository.findByEvmStaffUserId(evmStaffId);
    }
    
    public List<DealerQuotation> getQuotationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return dealerQuotationRepository.findByQuotationDateBetween(startDate, endDate);
    }
    
    public List<DealerQuotation> getExpiredQuotations() {
        List<DealerQuotationStatus> expiredStatuses = Arrays.asList(
            DealerQuotationStatus.PENDING,
            DealerQuotationStatus.SENT
        );
        return dealerQuotationRepository.findExpiredQuotations(LocalDate.now(), expiredStatuses);
    }
    
    /**
     * Tạo báo giá từ đơn hàng đại lý
     */
    @Transactional
    public DealerQuotation createQuotationFromOrder(UUID dealerOrderId, UUID evmStaffId, BigDecimal discountPercentage, String notes) {
        // Validate dealer order - Use findByIdWithDetails to eagerly load dealer
        DealerOrder dealerOrder = dealerOrderRepository.findByIdWithDetails(dealerOrderId)
            .orElseThrow(() -> new RuntimeException("Dealer order not found with ID: " + dealerOrderId));
        
        // Ensure dealer is loaded - If JOIN FETCH didn't work, try to load it manually
        Dealer dealer = dealerOrder.getDealer();
        if (dealer == null) {
            // Try to get dealer_id from the order using native query or EntityManager
            // First reload order without JOIN to check if dealer_id exists
            DealerOrder orderWithoutJoin = dealerOrderRepository.findById(dealerOrderId)
                .orElseThrow(() -> new RuntimeException("Dealer order not found with ID: " + dealerOrderId));
            
            // Try to access dealer through Hibernate proxy
            try {
                dealer = orderWithoutJoin.getDealer();
                if (dealer != null) {
                    UUID dealerId = dealer.getDealerId(); // Force initialization
                }
            } catch (Exception e) {
            }
            
            // If still null, try to query dealer directly from dealer_id
            if (dealer == null) {
                // Try to get dealer_id directly from database
                Optional<UUID> dealerIdOpt = dealerOrderRepository.findDealerIdByOrderId(dealerOrderId);
                if (dealerIdOpt.isPresent()) {
                    UUID dealerId = dealerIdOpt.get();
                    dealer = dealerRepository.findById(dealerId)
                        .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + dealerId + " from order " + dealerOrderId));
                    // Update dealerOrder with the loaded dealer and save
                    dealerOrder.setDealer(dealer);
                    dealerOrderRepository.save(dealerOrder);
                } else {
                    // If dealer_id is null in DB, try to get a default dealer
                    List<Dealer> dealers = dealerRepository.findAll();
                    if (!dealers.isEmpty()) {
                        Dealer defaultDealer = dealers.get(0);
                        dealer = defaultDealer;
                        dealerOrder.setDealer(dealer);
                        dealerOrderRepository.save(dealerOrder);
                    } else {
                        throw new RuntimeException("Dealer order must have a dealer associated. Order ID: " + dealerOrderId + ". dealer_id is NULL in database and no dealer available.");
                    }
                }
            }
        } else {
            // Force initialization to ensure dealer is loaded within transaction
            try {
                UUID dealerId = dealer.getDealerId(); // This should trigger loading if needed
            } catch (org.hibernate.LazyInitializationException e) {
                // Try to reload dealer directly using Hibernate session
                try {
                    // Use Hibernate's getIdentifier method to get dealer_id from proxy
                    if (dealer instanceof org.hibernate.proxy.HibernateProxy) {
                        org.hibernate.proxy.LazyInitializer initializer = ((org.hibernate.proxy.HibernateProxy) dealer).getHibernateLazyInitializer();
                        UUID dealerId = (UUID) initializer.getIdentifier();
                        dealer = dealerRepository.findById(dealerId)
                            .orElseThrow(() -> new RuntimeException("Dealer not found with ID: " + dealerId));
                    } else {
                        throw new RuntimeException("Dealer is not a Hibernate proxy, cannot extract ID");
                    }
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to load dealer from order: " + ex.getMessage());
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load dealer from order: " + e.getMessage());
            }
        }
        
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
                .filter(q -> q.getStatus() == DealerQuotationStatus.PENDING || q.getStatus() == DealerQuotationStatus.SENT)
                .findFirst()
                .orElse(null);
            if (existing != null) {
                throw new RuntimeException("Active quotation already exists for this order");
            }
        }
        
        // Create quotation
        DealerQuotation quotation = new DealerQuotation();
        quotation.setQuotationNumber(generateQuotationNumber());
        quotation.setDealer(dealer); // Use the dealer variable we ensured is loaded
        quotation.setDealerOrder(dealerOrder);
        quotation.setEvmStaff(evmStaff);
        quotation.setQuotationDate(LocalDate.now());
        quotation.setValidityDays(30);
        quotation.setStatus(DealerQuotationStatus.PENDING);
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
            
            // Use orderItem unitPrice if available, otherwise use variant base price
            // IMPORTANT: Set unitPrice BEFORE quantity to avoid null pointer in calculatePrices()
            BigDecimal unitPrice = null;
            if (orderItem.getUnitPrice() != null) {
                unitPrice = orderItem.getUnitPrice();
            } else if (orderItem.getVariant() != null) {
                try {
                    // Force load variant price if needed
                    BigDecimal variantPrice = orderItem.getVariant().getPriceBase();
                    if (variantPrice != null) {
                        unitPrice = variantPrice;
                    } else {
                        unitPrice = BigDecimal.ZERO;
                    }
                } catch (Exception e) {
                    unitPrice = BigDecimal.ZERO;
                }
            } else {
                unitPrice = BigDecimal.ZERO;
            }
            
            // Ensure unitPrice is never null before setting
            if (unitPrice == null) {
                unitPrice = BigDecimal.ZERO;
            }
            
            // Set unitPrice FIRST, then quantity (quantity triggers calculatePrices)
            quotationItem.setUnitPrice(unitPrice);
            Integer quantity = orderItem.getQuantity() != null ? orderItem.getQuantity() : 1;
            quotationItem.setQuantity(quantity);
            
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
            quotation.setDiscountAmount(subtotal.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
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
            
            // Use orderItem unitPrice if available, otherwise use variant base price
            // IMPORTANT: Set unitPrice BEFORE quantity to avoid null pointer in calculatePrices()
            BigDecimal unitPrice = null;
            if (orderItem.getUnitPrice() != null) {
                unitPrice = orderItem.getUnitPrice();
            } else if (orderItem.getVariant() != null) {
                try {
                    BigDecimal variantPrice = orderItem.getVariant().getPriceBase();
                    unitPrice = variantPrice != null ? variantPrice : BigDecimal.ZERO;
                } catch (Exception e) {
                    unitPrice = BigDecimal.ZERO;
                }
            } else {
                unitPrice = BigDecimal.ZERO;
            }
            
            // Ensure unitPrice is never null before setting
            if (unitPrice == null) {
                unitPrice = BigDecimal.ZERO;
            }
            
            // Set unitPrice FIRST, then quantity (quantity triggers calculatePrices)
            quotationItem.setUnitPrice(unitPrice);
            quotationItem.setQuantity(orderItem.getQuantity());
            
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
        
        if (quotation.getStatus() != DealerQuotationStatus.PENDING) {
            throw new RuntimeException("Quotation must be in 'pending' status to send");
        }
        
        quotation.setStatus(DealerQuotationStatus.SENT);
        return dealerQuotationRepository.save(quotation);
    }
    
    /**
     * Đại lý chấp nhận báo giá -> tạo Invoice
     */
    public DealerInvoice acceptQuotation(UUID quotationId) {
        DealerQuotation quotation = dealerQuotationRepository.findById(quotationId)
            .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        if (quotation.getStatus() != DealerQuotationStatus.SENT) {
            throw new RuntimeException("Quotation must be in 'sent' status to accept");
        }
        
        // Check if quotation is expired
        if (quotation.getExpiryDate() != null && quotation.getExpiryDate().isBefore(LocalDate.now())) {
            quotation.setStatus(DealerQuotationStatus.EXPIRED);
            dealerQuotationRepository.save(quotation);
            throw new RuntimeException("Quotation has expired");
        }
        
        // Update quotation status
        quotation.setStatus(DealerQuotationStatus.ACCEPTED);
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
        invoice.setStatus(DealerInvoiceStatus.ISSUED);
        invoice.setPaymentTermsDays(30);
        invoice.setNotes("Generated from quotation: " + quotation.getQuotationNumber());
        
        DealerInvoice savedInvoice = dealerInvoiceService.createInvoice(invoice);
        
        // Update quotation to converted
        quotation.setStatus(DealerQuotationStatus.CONVERTED);
        dealerQuotationRepository.save(quotation);
        
        return savedInvoice;
    }
    
    /**
     * Đại lý từ chối báo giá
     */
    public DealerQuotation rejectQuotation(UUID quotationId, String reason) {
        DealerQuotation quotation = dealerQuotationRepository.findById(quotationId)
            .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        if (quotation.getStatus() != DealerQuotationStatus.SENT) {
            throw new RuntimeException("Quotation must be in 'sent' status to reject");
        }
        
        quotation.setStatus(DealerQuotationStatus.REJECTED);
        quotation.setRejectedAt(LocalDateTime.now());
        quotation.setRejectionReason(reason);
        
        return dealerQuotationRepository.save(quotation);
    }
    
    public DealerQuotation updateQuotation(UUID quotationId, DealerQuotation quotationDetails) {
        DealerQuotation quotation = dealerQuotationRepository.findById(quotationId)
            .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + quotationId));
        
        if (quotation.getStatus() != DealerQuotationStatus.PENDING) {
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
        
        if (quotation.getStatus() != DealerQuotationStatus.PENDING) {
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

