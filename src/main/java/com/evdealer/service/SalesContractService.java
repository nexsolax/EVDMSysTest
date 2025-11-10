package com.evdealer.service;

import com.evdealer.entity.SalesContract;
import com.evdealer.entity.Order;
import com.evdealer.enums.SalesContractStatus;
import com.evdealer.repository.SalesContractRepository;
import com.evdealer.repository.OrderRepository;
import com.evdealer.repository.CustomerRepository;
import com.evdealer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SalesContractService {
    
    @Autowired
    private SalesContractRepository salesContractRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<SalesContract> getAllContracts() {
        // Dùng native query để tránh lỗi khi customer/order đã bị xóa
        try {
            List<SalesContract> contracts = salesContractRepository.findAllNative();
            System.out.println("SalesContractService.getAllContracts() - Found " + contracts.size() + " contracts (native query)");
            return contracts;
        } catch (Exception e) {
            System.err.println("SalesContractService.getAllContracts() - Native query failed: " + e.getMessage());
            e.printStackTrace();
            // Fallback: thử findAll thông thường
            try {
                List<SalesContract> contracts = salesContractRepository.findAll();
                System.out.println("SalesContractService.getAllContracts() - Found " + contracts.size() + " contracts (simple findAll)");
                return contracts;
            } catch (Exception e2) {
                System.err.println("SalesContractService.getAllContracts() - Simple findAll also failed: " + e2.getMessage());
                e2.printStackTrace();
                return new java.util.ArrayList<>();
            }
        }
    }
    
    public Optional<SalesContract> getContractById(UUID contractId) {
        return salesContractRepository.findById(contractId);
    }
    
    public Optional<SalesContract> getContractByNumber(String contractNumber) {
        return salesContractRepository.findByContractNumber(contractNumber);
    }
    
    public List<SalesContract> getContractsByOrder(UUID orderId) {
        try {
            return salesContractRepository.findByOrderOrderId(orderId);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<SalesContract> getContractsByCustomer(UUID customerId) {
        try {
            return salesContractRepository.findByCustomerCustomerId(customerId);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<SalesContract> getContractsByUser(UUID userId) {
        try {
            return salesContractRepository.findByUserUserId(userId);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<SalesContract> getContractsByStatus(String contractStatus) {
        try {
            // Convert string to enum for validation
            SalesContractStatus statusEnum = SalesContractStatus.fromString(contractStatus);
            return salesContractRepository.findByContractStatus(statusEnum);
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public List<SalesContract> getContractsByDateRange(LocalDate startDate, LocalDate endDate) {
        return salesContractRepository.findByContractDateBetween(startDate, endDate);
    }
    
    public List<SalesContract> getContractsByDeliveryDateRange(LocalDate startDate, LocalDate endDate) {
        return salesContractRepository.findByDeliveryDateBetween(startDate, endDate);
    }
    
    public List<SalesContract> getContractsByCustomerAndStatus(UUID customerId, String status) {
        // Convert string to enum for validation
        SalesContractStatus statusEnum = SalesContractStatus.fromString(status);
        return salesContractRepository.findByCustomerAndStatus(customerId, statusEnum);
    }
    
    public SalesContract createContract(SalesContract contract) {
        // Validate contract number
        if (contract.getContractNumber() == null || contract.getContractNumber().trim().isEmpty()) {
            throw new RuntimeException("Contract number is required");
        }
        
        if (salesContractRepository.existsByContractNumber(contract.getContractNumber())) {
            throw new RuntimeException("Contract number already exists: " + contract.getContractNumber());
        }
        
        // Validate contract date
        if (contract.getContractDate() == null) {
            throw new RuntimeException("Contract date is required");
        }
        
        // Validate contract value
        if (contract.getContractValue() == null || 
            contract.getContractValue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Contract value must be greater than zero");
        }
        
        // Validate delivery date
        if (contract.getDeliveryDate() != null && contract.getContractDate() != null) {
            if (contract.getDeliveryDate().isBefore(contract.getContractDate())) {
                throw new RuntimeException("Delivery date must be on or after contract date");
            }
        }
        
        // Validate foreign keys
        if (contract.getOrder() != null && contract.getOrder().getOrderId() != null) {
            contract.setOrder(orderRepository.findById(contract.getOrder().getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found with id: " + contract.getOrder().getOrderId())));
        }
        
        if (contract.getCustomer() != null && contract.getCustomer().getCustomerId() != null) {
            contract.setCustomer(customerRepository.findById(contract.getCustomer().getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + contract.getCustomer().getCustomerId())));
        }
        
        if (contract.getUser() != null && contract.getUser().getUserId() != null) {
            contract.setUser(userRepository.findById(contract.getUser().getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + contract.getUser().getUserId())));
        }
        
        return salesContractRepository.save(contract);
    }
    
    /**
     * Tự động tạo SalesContract từ Order khi khách hàng thanh toán đủ
     */
    public SalesContract createContractFromOrder(Order order) {
        // Kiểm tra xem đã có contract chưa
        List<SalesContract> existingContracts = getContractsByOrder(order.getOrderId());
        if (!existingContracts.isEmpty()) {
            // Đã có contract, không tạo mới
            return existingContracts.get(0);
        }
        
        // Tạo contract mới
        SalesContract contract = new SalesContract();
        contract.setContractNumber("SC-" + System.currentTimeMillis());
        contract.setOrder(order);
        contract.setCustomer(order.getCustomer());
        contract.setContractDate(java.time.LocalDate.now());
        contract.setDeliveryDate(order.getDeliveryDate()); // Order có deliveryDate, không có expectedDeliveryDate
        contract.setContractValue(order.getTotalAmount() != null ? order.getTotalAmount() : java.math.BigDecimal.ZERO);
        contract.setPaymentTerms("Thanh toán đủ: " + order.getTotalAmount());
        contract.setWarrantyPeriodMonths(24); // Mặc định 24 tháng
        contract.setContractStatus(com.evdealer.enums.SalesContractStatus.DRAFT);
        contract.setNotes("Tự động tạo sau khi thanh toán đủ");
        
        return createContract(contract);
    }
    
    public SalesContract updateContract(UUID contractId, SalesContract contractDetails) {
        SalesContract contract = salesContractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Sales contract not found with id: " + contractId));
        
        // Validate contract number
        if (contractDetails.getContractNumber() == null || contractDetails.getContractNumber().trim().isEmpty()) {
            throw new RuntimeException("Contract number is required");
        }
        
        // Check for duplicate contract number (excluding current contract)
        if (!contract.getContractNumber().equals(contractDetails.getContractNumber()) && 
            salesContractRepository.existsByContractNumber(contractDetails.getContractNumber())) {
            throw new RuntimeException("Contract number already exists: " + contractDetails.getContractNumber());
        }
        
        // Validate contract date
        if (contractDetails.getContractDate() == null) {
            throw new RuntimeException("Contract date is required");
        }
        
        // Validate contract value
        if (contractDetails.getContractValue() == null || 
            contractDetails.getContractValue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Contract value must be greater than zero");
        }
        
        // Validate delivery date
        if (contractDetails.getDeliveryDate() != null && contractDetails.getContractDate() != null) {
            if (contractDetails.getDeliveryDate().isBefore(contractDetails.getContractDate())) {
                throw new RuntimeException("Delivery date must be on or after contract date");
            }
        }
        
        // Validate foreign keys
        if (contractDetails.getOrder() != null && contractDetails.getOrder().getOrderId() != null) {
            contract.setOrder(orderRepository.findById(contractDetails.getOrder().getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found with id: " + contractDetails.getOrder().getOrderId())));
        } else {
            contract.setOrder(null);
        }
        
        if (contractDetails.getCustomer() != null && contractDetails.getCustomer().getCustomerId() != null) {
            contract.setCustomer(customerRepository.findById(contractDetails.getCustomer().getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + contractDetails.getCustomer().getCustomerId())));
        } else {
            contract.setCustomer(null);
        }
        
        if (contractDetails.getUser() != null && contractDetails.getUser().getUserId() != null) {
            contract.setUser(userRepository.findById(contractDetails.getUser().getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + contractDetails.getUser().getUserId())));
        } else {
            contract.setUser(null);
        }
        
        contract.setContractNumber(contractDetails.getContractNumber());
        contract.setContractDate(contractDetails.getContractDate());
        contract.setDeliveryDate(contractDetails.getDeliveryDate());
        contract.setContractValue(contractDetails.getContractValue());
        contract.setPaymentTerms(contractDetails.getPaymentTerms());
        contract.setWarrantyPeriodMonths(contractDetails.getWarrantyPeriodMonths());
        contract.setContractStatus(contractDetails.getContractStatus());
        contract.setSignedDate(contractDetails.getSignedDate());
        contract.setContractFileUrl(contractDetails.getContractFileUrl());
        contract.setContractFilePath(contractDetails.getContractFilePath());
        contract.setNotes(contractDetails.getNotes());
        
        return salesContractRepository.save(contract);
    }
    
    public void deleteContract(UUID contractId) {
        SalesContract contract = salesContractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Sales contract not found with id: " + contractId));
        salesContractRepository.delete(contract);
    }
    
    public SalesContract updateContractStatus(UUID contractId, String status) {
        SalesContract contract = salesContractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Sales contract not found with id: " + contractId));
        // Use overloaded setter that accepts String (backward compatibility)
        contract.setContractStatus(status);
        return salesContractRepository.save(contract);
    }
    
    public SalesContract signContract(UUID contractId, LocalDate signedDate) {
        SalesContract contract = salesContractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Sales contract not found with id: " + contractId));
        
        // Validate signed date
        if (signedDate == null) {
            throw new RuntimeException("Signed date is required");
        }
        
        // Validate signed date is not before contract date
        if (contract.getContractDate() != null && signedDate.isBefore(contract.getContractDate())) {
            throw new RuntimeException("Signed date must be on or after contract date");
        }
        
        contract.setSignedDate(signedDate);
        contract.setContractStatus(com.evdealer.enums.SalesContractStatus.SIGNED);
        return salesContractRepository.save(contract);
    }
}
