package com.evdealer.service;

import com.evdealer.entity.DealerPayment;
import com.evdealer.enums.DealerPaymentStatus;
import com.evdealer.repository.DealerPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DealerPaymentService {
    
    @Autowired
    private DealerPaymentRepository dealerPaymentRepository;
    
    @Transactional(readOnly = true)
    public List<DealerPayment> getAllDealerPayments() {
        try {
            return dealerPaymentRepository.findAllWithDetails();
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    @Transactional(readOnly = true)
    public List<DealerPayment> getPaymentsByStatus(String status) {
        // Convert string to enum for validation
        DealerPaymentStatus statusEnum = DealerPaymentStatus.fromString(status);
        return dealerPaymentRepository.findByStatus(statusEnum);
    }
    
    @Transactional(readOnly = true)
    public List<DealerPayment> getPaymentsByInvoice(UUID invoiceId) {
        return dealerPaymentRepository.findByInvoiceInvoiceId(invoiceId);
    }
    
    @Transactional(readOnly = true)
    public List<DealerPayment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return dealerPaymentRepository.findByPaymentDateBetween(startDate, endDate);
    }
    
    @Transactional(readOnly = true)
    public List<DealerPayment> getPaymentsByType(String paymentType) {
        // Convert String to enum and query
        com.evdealer.enums.PaymentMethod paymentMethod = com.evdealer.enums.PaymentMethod.fromString(paymentType);
        return dealerPaymentRepository.findByPaymentMethod(paymentMethod);
    }
    
    @Transactional(readOnly = true)
    public List<DealerPayment> getPaymentsByReferenceNumber(String referenceNumber) {
        return dealerPaymentRepository.findByReferenceNumber(referenceNumber);
    }
    
    @Transactional(readOnly = true)
    public Optional<DealerPayment> getPaymentById(UUID paymentId) {
        return dealerPaymentRepository.findByIdWithDetails(paymentId);
    }
    
    @Transactional(readOnly = true)
    public Optional<DealerPayment> getPaymentByNumber(String paymentNumber) {
        return dealerPaymentRepository.findByPaymentNumber(paymentNumber);
    }
    
    public DealerPayment createDealerPayment(DealerPayment dealerPayment) {
        if (dealerPaymentRepository.existsByPaymentNumber(dealerPayment.getPaymentNumber())) {
            throw new RuntimeException("Payment number already exists");
        }
        return dealerPaymentRepository.save(dealerPayment);
    }
    
    public DealerPayment updateDealerPayment(UUID paymentId, DealerPayment dealerPaymentDetails) {
        DealerPayment dealerPayment = dealerPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Dealer payment not found"));
        
        dealerPayment.setInvoice(dealerPaymentDetails.getInvoice());
        dealerPayment.setPaymentNumber(dealerPaymentDetails.getPaymentNumber());
        dealerPayment.setPaymentDate(dealerPaymentDetails.getPaymentDate());
        dealerPayment.setAmount(dealerPaymentDetails.getAmount());
        dealerPayment.setPaymentMethod(dealerPaymentDetails.getPaymentMethod());
        dealerPayment.setReferenceNumber(dealerPaymentDetails.getReferenceNumber());
        dealerPayment.setStatus(dealerPaymentDetails.getStatus());
        dealerPayment.setNotes(dealerPaymentDetails.getNotes());
        
        return dealerPaymentRepository.save(dealerPayment);
    }
    
    public void deleteDealerPayment(UUID paymentId) {
        DealerPayment payment = dealerPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Dealer payment not found with id: " + paymentId));
        dealerPaymentRepository.delete(payment);
    }
    
    public DealerPayment updatePaymentStatus(UUID paymentId, String status) {
        DealerPayment dealerPayment = dealerPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Dealer payment not found"));
        DealerPaymentStatus statusEnum = DealerPaymentStatus.fromString(status);
        dealerPayment.setStatus(statusEnum);
        return dealerPaymentRepository.save(dealerPayment);
    }
    
    // Additional methods for new APIs
    public Optional<DealerPayment> getDealerPaymentById(UUID paymentId) {
        return dealerPaymentRepository.findById(paymentId);
    }
    
    @Transactional(readOnly = true)
    public List<DealerPayment> getPaymentsByDealer(UUID dealerId) {
        return dealerPaymentRepository.findByInvoiceDealerOrderDealerDealerId(dealerId);
    }
    
    public java.util.Map<String, Object> getDealerPaymentSummary(UUID dealerId) {
        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        
        List<DealerPayment> payments = getPaymentsByDealer(dealerId);
        java.math.BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == DealerPaymentStatus.COMPLETED)
                .map(DealerPayment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        long totalPayments = payments.size();
        long completedPayments = payments.stream()
                .filter(p -> p.getStatus() == DealerPaymentStatus.COMPLETED)
                .count();
        
        summary.put("totalPayments", totalPayments);
        summary.put("completedPayments", completedPayments);
        summary.put("totalPaidAmount", totalPaid);
        
        return summary;
    }
    
    public java.util.Map<String, Object> getPaymentStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        long totalPayments = dealerPaymentRepository.count();
        long completedPayments = dealerPaymentRepository.countByStatus(DealerPaymentStatus.COMPLETED);
        long pendingPayments = dealerPaymentRepository.countByStatus(DealerPaymentStatus.PENDING);
        long failedPayments = dealerPaymentRepository.countByStatus(DealerPaymentStatus.FAILED);
        
        stats.put("totalPayments", totalPayments);
        stats.put("completedPayments", completedPayments);
        stats.put("pendingPayments", pendingPayments);
        stats.put("failedPayments", failedPayments);
        
        return stats;
    }
}
