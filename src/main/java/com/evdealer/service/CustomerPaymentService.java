package com.evdealer.service;

import com.evdealer.entity.CustomerPayment;
import com.evdealer.entity.Order;
import com.evdealer.entity.VehicleInventory;
import com.evdealer.enums.PaymentStatus;
import com.evdealer.enums.CustomerPaymentStatus;
import com.evdealer.enums.OrderStatus;
import com.evdealer.enums.VehicleStatus;
import com.evdealer.repository.CustomerPaymentRepository;
import com.evdealer.repository.OrderRepository;
import com.evdealer.repository.VehicleInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(noRollbackFor = {Exception.class})
public class CustomerPaymentService {
    
    @Autowired
    private CustomerPaymentRepository customerPaymentRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private VehicleInventoryRepository vehicleInventoryRepository;
    
    @Autowired
    private SalesContractService salesContractService;
    
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<CustomerPayment> getAllCustomerPayments() {
        // Dùng native query để tránh lỗi khi customer/order đã bị xóa
        try {
            List<CustomerPayment> payments = customerPaymentRepository.findAllNative();
            System.out.println("CustomerPaymentService.getAllCustomerPayments() - Found " + payments.size() + " payments (native query)");
            return payments;
        } catch (Exception e) {
            System.err.println("CustomerPaymentService.getAllCustomerPayments() - Native query failed: " + e.getMessage());
            e.printStackTrace();
            // Fallback: thử findAll thông thường
            try {
                List<CustomerPayment> payments = customerPaymentRepository.findAll();
                System.out.println("CustomerPaymentService.getAllCustomerPayments() - Found " + payments.size() + " payments (simple findAll)");
                return payments;
            } catch (Exception e2) {
                System.err.println("CustomerPaymentService.getAllCustomerPayments() - Simple findAll also failed: " + e2.getMessage());
                e2.printStackTrace();
                return new java.util.ArrayList<>();
            }
        }
    }
    
    public List<CustomerPayment> getPaymentsByStatus(String status) {
        CustomerPaymentStatus statusEnum = CustomerPaymentStatus.fromString(status);
        return customerPaymentRepository.findByStatus(statusEnum);
    }
    
    public List<CustomerPayment> getPaymentsByCustomer(UUID customerId) {
        return customerPaymentRepository.findByCustomerCustomerId(customerId);
    }
    
    public List<CustomerPayment> getPaymentsByOrder(UUID orderId) {
        return customerPaymentRepository.findByOrderOrderId(orderId);
    }
    
    public List<CustomerPayment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return customerPaymentRepository.findByPaymentDateBetween(startDate, endDate);
    }
    
    public List<CustomerPayment> getPaymentsByType(String paymentType) {
        return customerPaymentRepository.findByPaymentType(paymentType);
    }
    
    public List<CustomerPayment> getPaymentsByMethod(String paymentMethod) {
        // Convert String to enum and query
        com.evdealer.enums.PaymentMethod paymentMethodEnum = com.evdealer.enums.PaymentMethod.fromString(paymentMethod);
        return customerPaymentRepository.findByPaymentMethod(paymentMethodEnum);
    }
    
    public List<CustomerPayment> getPaymentsByProcessedBy(UUID userId) {
        return customerPaymentRepository.findByProcessedByUserId(userId);
    }
    
    public Optional<CustomerPayment> getPaymentById(UUID paymentId) {
        return customerPaymentRepository.findById(paymentId);
    }
    
    public Optional<CustomerPayment> getPaymentByNumber(String paymentNumber) {
        return customerPaymentRepository.findByPaymentNumber(paymentNumber);
    }
    
    public CustomerPayment createCustomerPayment(CustomerPayment customerPayment) {
        if (customerPaymentRepository.existsByPaymentNumber(customerPayment.getPaymentNumber())) {
            throw new RuntimeException("Payment number already exists");
        }
        
        // VALIDATION: Order phải có Quotation và status >= "confirmed"
        if (customerPayment.getOrder() != null) {
            Order order = customerPayment.getOrder();
            
            // Kiểm tra Order có Quotation chưa
            if (order.getQuotation() == null) {
                throw new RuntimeException("Order must have an accepted quotation before payment");
            }
            
            // Kiểm tra Order status
            if (order.getStatus() != OrderStatus.CONFIRMED && 
                order.getStatus() != OrderStatus.PAID) {
                throw new RuntimeException(
                    "Order must be in CONFIRMED or PAID status to accept payment. Current status: " + 
                    (order.getStatus() != null ? order.getStatus().getValue() : "null")
                );
            }
            
            // Kiểm tra Order có totalAmount chưa
            if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Order must have a valid total amount before payment");
            }
        }
        
        CustomerPayment savedPayment = customerPaymentRepository.save(customerPayment);
        
        // Update Order status after payment is created and if status is "completed"
        if (savedPayment.getStatus() == CustomerPaymentStatus.COMPLETED && savedPayment.getOrder() != null) {
            updateOrderStatusAfterPayment(savedPayment.getOrder().getOrderId());
        }
        
        return savedPayment;
    }
    
    public CustomerPayment updateCustomerPayment(UUID paymentId, CustomerPayment customerPaymentDetails) {
        CustomerPayment customerPayment = customerPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Customer payment not found"));
        
        customerPayment.setOrder(customerPaymentDetails.getOrder());
        customerPayment.setCustomer(customerPaymentDetails.getCustomer());
        customerPayment.setPaymentNumber(customerPaymentDetails.getPaymentNumber());
        customerPayment.setPaymentDate(customerPaymentDetails.getPaymentDate());
        customerPayment.setAmount(customerPaymentDetails.getAmount());
        customerPayment.setPaymentType(customerPaymentDetails.getPaymentType());
        customerPayment.setPaymentMethod(customerPaymentDetails.getPaymentMethod());
        customerPayment.setReferenceNumber(customerPaymentDetails.getReferenceNumber());
        customerPayment.setStatus(customerPaymentDetails.getStatus());
        customerPayment.setProcessedBy(customerPaymentDetails.getProcessedBy());
        customerPayment.setNotes(customerPaymentDetails.getNotes());
        
        return customerPaymentRepository.save(customerPayment);
    }
    
    public void deleteCustomerPayment(UUID paymentId) {
        CustomerPayment customerPayment = customerPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Customer payment not found with id: " + paymentId));
        customerPaymentRepository.delete(customerPayment);
    }
    
    public CustomerPayment updatePaymentStatus(UUID paymentId, String status) {
        CustomerPayment customerPayment = customerPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Customer payment not found"));
        CustomerPaymentStatus statusEnum = CustomerPaymentStatus.fromString(status);
        customerPayment.setStatus(statusEnum);
        
        CustomerPayment savedPayment = customerPaymentRepository.save(customerPayment);
        
        // Update Order status after payment status is updated to "completed"
        if (savedPayment.getStatus() == CustomerPaymentStatus.COMPLETED && savedPayment.getOrder() != null) {
            updateOrderStatusAfterPayment(savedPayment.getOrder().getOrderId());
        }
        
        return savedPayment;
    }
    
    /**
     * Calculate total paid amount for an order
     */
    private BigDecimal calculateTotalPaid(UUID orderId) {
        List<CustomerPayment> payments = customerPaymentRepository.findByOrderOrderId(orderId);
        return payments.stream()
                .filter(p -> p.getStatus() == CustomerPaymentStatus.COMPLETED)
                .map(CustomerPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Update Order status and Inventory status after payment
     */
    private void updateOrderStatusAfterPayment(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElse(null);
        
        if (order == null) {
            return;
        }
        
        // Calculate total paid amount
        BigDecimal totalPaid = calculateTotalPaid(orderId);
        
        if (order.getTotalAmount() != null && totalPaid.compareTo(order.getTotalAmount()) >= 0) {
            // Fully paid
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.PAID);
            
            // Update Inventory status to "sold" if fully paid
            if (order.getInventory() != null) {
                VehicleInventory inventory = order.getInventory();
                inventory.setStatus(VehicleStatus.SOLD);
                vehicleInventoryRepository.save(inventory);
            }
            
            // Tự động tạo SalesContract sau khi thanh toán đủ
            try {
                salesContractService.createContractFromOrder(order);
            } catch (Exception e) {
                // Log error nhưng không fail payment
                System.err.println("Failed to auto-create sales contract after payment: " + e.getMessage());
            }
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            // Partially paid
            order.setPaymentStatus(PaymentStatus.PARTIAL);
            order.setStatus(OrderStatus.CONFIRMED);
        }
        
        orderRepository.save(order);
    }
}
