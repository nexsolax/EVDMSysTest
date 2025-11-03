package com.evdealer.service;

import com.evdealer.dto.OrderRequest;
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
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private QuotationRepository quotationRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleInventoryRepository vehicleInventoryRepository;
    
    public List<Order> getAllOrders() {
        try {
            // Use JOIN FETCH to eagerly load relationships
            return orderRepository.findAllWithRelationships();
        } catch (Exception e) {
            // Log error and return empty list
            System.err.println("Error fetching orders: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    public Optional<Order> getOrderById(UUID orderId) {
        return orderRepository.findById(orderId);
    }
    
    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }
    
    public List<Order> getOrdersByCustomer(UUID customerId) {
        return orderRepository.findByCustomerCustomerId(customerId);
    }
    
    
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatusString(status);
    }
    
    public List<Order> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }
    
    public List<Order> getOrdersByCustomerAndStatus(UUID customerId, String status) {
        return orderRepository.findByCustomerAndStatus(customerId, status);
    }
    
    
    public Order createOrder(Order order) {
        if (orderRepository.existsByOrderNumber(order.getOrderNumber())) {
            throw new RuntimeException("Order number already exists: " + order.getOrderNumber());
        }
        return orderRepository.save(order);
    }
    
    public Order createOrderFromRequest(OrderRequest request) {
        // Generate order number if not provided
        String orderNumber = generateOrderNumber();
        
        // Find related entities
        Quotation quotation = null;
        if (request.getQuotationId() != null) {
            quotation = quotationRepository.findById(request.getQuotationId())
                    .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + request.getQuotationId()));
        }
        
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        
        VehicleInventory inventory = vehicleInventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new RuntimeException("Vehicle inventory not found with ID: " + request.getInventoryId()));
        
        // Create order entity
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setQuotation(quotation);
        order.setCustomer(customer);
        order.setUser(user);
        order.setInventory(inventory);
        order.setOrderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now());
        // Set order type and status enums
        if (request.getOrderType() != null) {
            order.setOrderType(request.getOrderType());
        }
        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());
        }
        if (request.getDeliveryStatus() != null) {
            order.setDeliveryStatus(request.getDeliveryStatus());
        }
        if (request.getFulfillmentStatus() != null) {
            order.setFulfillmentStatus(request.getFulfillmentStatus());
        }
        if (request.getFulfillmentMethod() != null) {
            order.setFulfillmentMethod(request.getFulfillmentMethod());
        }
        order.setTotalAmount(request.getTotalAmount());
        order.setDepositAmount(request.getDepositAmount() != null ? request.getDepositAmount() : BigDecimal.ZERO);
        order.setBalanceAmount(request.getBalanceAmount());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNotes(request.getNotes());
        order.setDeliveryDate(request.getDeliveryDate());
        order.setSpecialRequests(request.getSpecialRequests());
        
        return orderRepository.save(order);
    }
    
    private String generateOrderNumber() {
        // Generate order number in format: ORD-YYYYMMDD-XXXX
        String dateStr = LocalDate.now().toString().replace("-", "");
        String randomStr = String.format("%04d", (int) (Math.random() * 10000));
        return "ORD-" + dateStr + "-" + randomStr;
    }
    
    public Order updateOrder(UUID orderId, Order orderDetails) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        // Check for duplicate order number (excluding current order)
        if (!order.getOrderNumber().equals(orderDetails.getOrderNumber()) && 
            orderRepository.existsByOrderNumber(orderDetails.getOrderNumber())) {
            throw new RuntimeException("Order number already exists: " + orderDetails.getOrderNumber());
        }
        
        order.setOrderNumber(orderDetails.getOrderNumber());
        order.setQuotation(orderDetails.getQuotation());
        order.setCustomer(orderDetails.getCustomer());
        order.setUser(orderDetails.getUser());
        order.setInventory(orderDetails.getInventory());
        order.setOrderDate(orderDetails.getOrderDate());
        order.setStatus(orderDetails.getStatus());
        order.setTotalAmount(orderDetails.getTotalAmount());
        order.setDepositAmount(orderDetails.getDepositAmount());
        order.setBalanceAmount(orderDetails.getBalanceAmount());
        order.setPaymentMethod(orderDetails.getPaymentMethod());
        order.setNotes(orderDetails.getNotes());
        
        return orderRepository.save(order);
    }
    
    public Order updateOrderFromRequest(UUID orderId, OrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        // Update quotation if provided
        if (request.getQuotationId() != null) {
            Quotation quotation = quotationRepository.findById(request.getQuotationId())
                    .orElseThrow(() -> new RuntimeException("Quotation not found with ID: " + request.getQuotationId()));
            order.setQuotation(quotation);
        }
        
        // Update customer if provided
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));
            order.setCustomer(customer);
        }
        
        // Update user if provided
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
            order.setUser(user);
        }
        
        // Update inventory if provided
        if (request.getInventoryId() != null) {
            VehicleInventory inventory = vehicleInventoryRepository.findById(request.getInventoryId())
                    .orElseThrow(() -> new RuntimeException("Vehicle inventory not found with ID: " + request.getInventoryId()));
            order.setInventory(inventory);
        }
        
        // Update other fields
        if (request.getOrderDate() != null) {
            order.setOrderDate(request.getOrderDate());
        }
        if (request.getOrderType() != null) {
            order.setOrderType(request.getOrderType());
        }
        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());
        }
        if (request.getDeliveryStatus() != null) {
            order.setDeliveryStatus(request.getDeliveryStatus());
        }
        if (request.getFulfillmentStatus() != null) {
            order.setFulfillmentStatus(request.getFulfillmentStatus());
        }
        if (request.getFulfillmentMethod() != null) {
            order.setFulfillmentMethod(request.getFulfillmentMethod());
        }
        if (request.getTotalAmount() != null) {
            order.setTotalAmount(request.getTotalAmount());
        }
        if (request.getDepositAmount() != null) {
            order.setDepositAmount(request.getDepositAmount());
        }
        if (request.getBalanceAmount() != null) {
            order.setBalanceAmount(request.getBalanceAmount());
        }
        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }
        if (request.getDeliveryDate() != null) {
            order.setDeliveryDate(request.getDeliveryDate());
        }
        if (request.getSpecialRequests() != null) {
            order.setSpecialRequests(request.getSpecialRequests());
        }
        
        return orderRepository.save(order);
    }
    
    public void deleteOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        orderRepository.delete(order);
    }
    
    public Order updateOrderStatus(UUID orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
