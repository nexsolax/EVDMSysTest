package com.evdealer.service;

import com.evdealer.entity.DealerInvoice;
import com.evdealer.entity.DealerOrder;
import com.evdealer.enums.DealerInvoiceStatus;
import com.evdealer.enums.DealerPaymentStatus;
import com.evdealer.repository.DealerInvoiceRepository;
import com.evdealer.repository.DealerOrderRepository;
import com.evdealer.repository.DealerPaymentRepository;
import com.evdealer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DealerInvoiceService {
    
    @Autowired
    private DealerInvoiceRepository dealerInvoiceRepository;
    
    @Autowired
    private DealerOrderRepository dealerOrderRepository;
    
    @Autowired
    private DealerPaymentRepository dealerPaymentRepository;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @Transactional(readOnly = true)
    public List<DealerInvoice> getAllInvoices() {
        try {
            // Filter by dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID dealerId = currentUser.getDealer().getDealerId();
                    return dealerInvoiceRepository.findByDealerOrderDealerDealerId(dealerId);
                }
            }
            // Use findAllWithDetails to eagerly load dealerOrder
            return dealerInvoiceRepository.findAllWithDetails();
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<DealerInvoice> getInvoicesByStatus(String status) {
        DealerInvoiceStatus statusEnum = DealerInvoiceStatus.fromString(status);
        return dealerInvoiceRepository.findByStatus(statusEnum);
    }
    
    @Transactional(readOnly = true)
    public List<DealerInvoice> getInvoicesByDealerOrder(UUID dealerOrderId) {
        return dealerInvoiceRepository.findByDealerOrderDealerOrderId(dealerOrderId);
    }
    
    public List<DealerInvoice> getInvoicesByEvmStaff(UUID evmStaffId) {
        return dealerInvoiceRepository.findByEvmStaffUserId(evmStaffId);
    }
    
    public List<DealerInvoice> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        return dealerInvoiceRepository.findByInvoiceDateBetween(startDate, endDate);
    }
    
    public List<DealerInvoice> getOverdueInvoices() {
        LocalDate currentDate = LocalDate.now();
        List<DealerInvoiceStatus> overdueStatuses = Arrays.asList(
            DealerInvoiceStatus.ISSUED,
            DealerInvoiceStatus.PARTIALLY_PAID
        );
        return dealerInvoiceRepository.findOverdueInvoices(currentDate, overdueStatuses);
    }
    
    @Transactional(readOnly = true)
    public Optional<DealerInvoice> getInvoiceById(UUID invoiceId) {
        return dealerInvoiceRepository.findByIdWithDetails(invoiceId);
    }
    
    public Optional<DealerInvoice> getInvoiceByNumber(String invoiceNumber) {
        return dealerInvoiceRepository.findByInvoiceNumber(invoiceNumber);
    }
    
    public DealerInvoice createInvoice(DealerInvoice invoice) {
        if (dealerInvoiceRepository.existsByInvoiceNumber(invoice.getInvoiceNumber())) {
            throw new RuntimeException("Invoice number already exists");
        }
        return dealerInvoiceRepository.save(invoice);
    }
    
    public DealerInvoice updateInvoice(UUID invoiceId, DealerInvoice invoiceDetails) {
        DealerInvoice invoice = dealerInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        invoice.setInvoiceNumber(invoiceDetails.getInvoiceNumber());
        invoice.setDealerOrder(invoiceDetails.getDealerOrder());
        invoice.setEvmStaff(invoiceDetails.getEvmStaff());
        invoice.setQuotationId(invoiceDetails.getQuotationId());
        invoice.setInvoiceDate(invoiceDetails.getInvoiceDate());
        invoice.setDueDate(invoiceDetails.getDueDate());
        invoice.setSubtotal(invoiceDetails.getSubtotal());
        invoice.setTaxAmount(invoiceDetails.getTaxAmount());
        invoice.setDiscountAmount(invoiceDetails.getDiscountAmount());
        invoice.setTotalAmount(invoiceDetails.getTotalAmount());
        invoice.setStatus(invoiceDetails.getStatus());
        invoice.setPaymentTermsDays(invoiceDetails.getPaymentTermsDays());
        invoice.setNotes(invoiceDetails.getNotes());
        
        return dealerInvoiceRepository.save(invoice);
    }
    
    public void deleteInvoice(UUID invoiceId) {
        DealerInvoice invoice = dealerInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + invoiceId));
        dealerInvoiceRepository.delete(invoice);
    }
    
    public DealerInvoice updateInvoiceStatus(UUID invoiceId, String status) {
        DealerInvoice invoice = dealerInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        DealerInvoiceStatus statusEnum = DealerInvoiceStatus.fromString(status);
        invoice.setStatus(statusEnum);
        return dealerInvoiceRepository.save(invoice);
    }
    
    // Additional methods for new APIs
    public DealerInvoice generateInvoiceFromOrder(UUID dealerOrderId, UUID evmStaffId) {
        DealerOrder dealerOrder = dealerOrderRepository.findById(dealerOrderId)
            .orElseThrow(() -> new RuntimeException("Dealer order not found with ID: " + dealerOrderId));
        
        // Check if invoice already exists
        List<DealerInvoice> existingInvoices = dealerInvoiceRepository.findByDealerOrderDealerOrderId(dealerOrderId);
        if (!existingInvoices.isEmpty()) {
            throw new RuntimeException("Invoice already exists for this dealer order");
        }
        
        DealerInvoice invoice = new DealerInvoice();
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis());
        invoice.setDealerOrder(dealerOrder);
        invoice.setInvoiceDate(java.time.LocalDate.now());
        invoice.setDueDate(java.time.LocalDate.now().plusDays(30));
        invoice.setStatus(DealerInvoiceStatus.ISSUED);
        invoice.setPaymentTermsDays(30);
        invoice.setNotes("Generated from dealer order: " + dealerOrder.getDealerOrderNumber());
        
        // Calculate amounts from dealer order
        invoice.setSubtotal(dealerOrder.getTotalAmount());
        invoice.setTaxAmount(java.math.BigDecimal.ZERO); // Can be calculated if tax rate is known
        invoice.setDiscountAmount(dealerOrder.getDiscountApplied() != null ? dealerOrder.getDiscountApplied() : java.math.BigDecimal.ZERO);
        invoice.setTotalAmount(dealerOrder.getTotalAmount().subtract(invoice.getDiscountAmount()));
        
        return dealerInvoiceRepository.save(invoice);
    }
    
    public java.math.BigDecimal calculatePaidAmount(UUID invoiceId) {
        return dealerPaymentRepository.findByInvoiceInvoiceId(invoiceId).stream()
            .filter(payment -> payment.getStatus() == DealerPaymentStatus.COMPLETED)
            .map(payment -> payment.getAmount())
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
    
    public List<DealerInvoice> getInvoicesByDealer(UUID dealerId) {
        return dealerInvoiceRepository.findByDealerOrderDealerDealerId(dealerId);
    }
    
    public List<DealerInvoice> getUnpaidInvoicesByDealer(UUID dealerId) {
        return dealerInvoiceRepository.findByDealerOrderDealerDealerIdAndStatus(dealerId, DealerInvoiceStatus.ISSUED);
    }
    
    public java.util.Map<String, Object> getInvoiceStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        long totalInvoices = dealerInvoiceRepository.count();
        long pendingInvoices = dealerInvoiceRepository.countByStatus(DealerInvoiceStatus.ISSUED);
        long paidInvoices = dealerInvoiceRepository.countByStatus(DealerInvoiceStatus.PAID);
        long overdueInvoices = getOverdueInvoices().size();
        
        stats.put("totalInvoices", totalInvoices);
        stats.put("pendingInvoices", pendingInvoices);
        stats.put("paidInvoices", paidInvoices);
        stats.put("overdueInvoices", overdueInvoices);
        
        return stats;
    }
}
