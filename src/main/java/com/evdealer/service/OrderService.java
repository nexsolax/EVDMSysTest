package com.evdealer.service;

import com.evdealer.dto.OrderRequest;
import com.evdealer.entity.*;
import com.evdealer.repository.*;
import com.evdealer.enums.DealerQuotationStatus;
import com.evdealer.enums.DeliveryStatus;
import com.evdealer.enums.OrderStatus;
import com.evdealer.enums.VehicleStatus;
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
        OrderStatus statusEnum = OrderStatus.fromString(status);
        return orderRepository.findByCustomerAndStatus(customerId, statusEnum);
    }
    
    
    public Order createOrder(Order order) {
        if (orderRepository.existsByOrderNumber(order.getOrderNumber())) {
            throw new RuntimeException("Order number already exists: " + order.getOrderNumber());
        }
        
        // Validate foreign keys
        if (order.getQuotation() != null && order.getQuotation().getQuotationId() != null) {
            order.setQuotation(quotationRepository.findById(order.getQuotation().getQuotationId())
                    .orElseThrow(() -> new RuntimeException("Quotation not found with id: " + order.getQuotation().getQuotationId())));
        }
        
        if (order.getCustomer() != null && order.getCustomer().getCustomerId() != null) {
            order.setCustomer(customerRepository.findById(order.getCustomer().getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + order.getCustomer().getCustomerId())));
        }
        
        if (order.getUser() != null && order.getUser().getUserId() != null) {
            order.setUser(userRepository.findById(order.getUser().getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + order.getUser().getUserId())));
        }
        
        // Use pessimistic lock to prevent concurrent reservation
        if (order.getInventory() != null && order.getInventory().getInventoryId() != null) {
            VehicleInventory inventory = vehicleInventoryRepository.lockById(order.getInventory().getInventoryId())
                    .orElseThrow(() -> new RuntimeException("Vehicle inventory not found with id: " + order.getInventory().getInventoryId()));
            
            // Validate inventory availability
            if (inventory.getStatus() != VehicleStatus.AVAILABLE) {
                throw new RuntimeException("Vehicle inventory is not available. Current status: " + 
                    (inventory.getStatus() != null ? inventory.getStatus().getValue() : "null"));
            }
            
            // Reserve inventory
            inventory.setStatus(VehicleStatus.RESERVED);
            if (order.getCustomer() != null) {
                inventory.setReservedForCustomer(order.getCustomer());
            }
            inventory.setReservedDate(java.time.LocalDateTime.now());
            vehicleInventoryRepository.save(inventory);
            
            order.setInventory(inventory);
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
        
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));
        }
        
        // userId is optional
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        }
        
        // Validate inventory availability with pessimistic lock to prevent race condition
        VehicleInventory inventory = null;
        if (request.getInventoryId() != null) {
            // Use pessimistic lock to prevent concurrent reservation
            inventory = vehicleInventoryRepository.lockById(request.getInventoryId())
                    .orElseThrow(() -> new RuntimeException("Vehicle inventory not found with ID: " + request.getInventoryId()));
            
            // Validate inventory availability
            if (inventory.getStatus() != VehicleStatus.AVAILABLE) {
                throw new RuntimeException("Vehicle inventory is not available. Current status: " + 
                    (inventory.getStatus() != null ? inventory.getStatus().getValue() : "null"));
            }
        }
        
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
        // Set status = "pending" (yêu cầu mua bán, chưa phải order chính thức)
        if (request.getStatus() != null) {
            order.setStatus(OrderStatus.fromString(request.getStatus()));
        } else {
            order.setStatus(OrderStatus.PENDING); // Yêu cầu mua bán
        }
        
        // totalAmount = null lúc này (chưa có báo giá)
        // Chỉ set totalAmount khi có Quotation và đã accept
        order.setTotalAmount(null);
        order.setDepositAmount(request.getDepositAmount() != null ? request.getDepositAmount() : BigDecimal.ZERO);
        order.setBalanceAmount(request.getBalanceAmount());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNotes(request.getNotes());
        order.setDeliveryDate(request.getDeliveryDate());
        order.setSpecialRequests(request.getSpecialRequests());
        
        // Update inventory status when creating order
        if (inventory != null) {
            // Set inventory status to "reserved" when order is created
            inventory.setStatus(VehicleStatus.RESERVED);
            if (customer != null) {
                inventory.setReservedForCustomer(customer);
            }
            inventory.setReservedDate(java.time.LocalDateTime.now());
            vehicleInventoryRepository.save(inventory);
        }
        
        return orderRepository.save(order);
    }
    
    private String generateOrderNumber() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        int maxAttempts = 10;
        
        // Retry mechanism với entropy cao hơn (6 chữ số thay vì 4)
        for (int i = 0; i < maxAttempts; i++) {
            String randomStr = String.format("%06d", (int) (Math.random() * 1000000));
            String orderNumber = "ORD-" + dateStr + "-" + randomStr;
            
            // Check if order number already exists
            if (!orderRepository.existsByOrderNumber(orderNumber)) {
                return orderNumber;
            }
        }
        
        // Fallback: dùng UUID nếu vẫn trùng sau maxAttempts lần
        String uuidSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + dateStr + "-" + uuidSuffix;
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
        // VALIDATION: Chỉ cho phép set totalAmount nếu Order đã có Quotation accepted
        // totalAmount chỉ được set khi accept quotation (trong QuotationService.acceptQuotation)
        // Hoặc nếu đang update và Order đã có quotation accepted
        if (request.getTotalAmount() != null) {
            // Kiểm tra nếu Order đã có quotation accepted hoặc converted
            if (order.getQuotation() != null && 
                (order.getQuotation().getStatus().equals(DealerQuotationStatus.ACCEPTED.getValue()) || 
                 order.getQuotation().getStatus().equals(DealerQuotationStatus.CONVERTED.getValue()))) {
                order.setTotalAmount(request.getTotalAmount());
            } else if (order.getStatus() == OrderStatus.CONFIRMED || 
                       order.getStatus() == OrderStatus.PAID) {
                // Cho phép update totalAmount nếu Order đã confirmed hoặc paid
                order.setTotalAmount(request.getTotalAmount());
            } else {
                // Không cho phép set totalAmount nếu chưa có quotation accepted
                throw new RuntimeException(
                    "Cannot set totalAmount. Order must have an accepted quotation first. " +
                    "Current order status: " + order.getStatus().getValue() +
                    (order.getQuotation() != null ? ", Quotation status: " + order.getQuotation().getStatus() : ", No quotation")
                );
            }
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
        // Kiểm tra xem order có tồn tại không
        if (orderId == null) {
            throw new RuntimeException("Order ID cannot be null");
        }
        
        // Thử tìm order bằng nhiều cách để debug
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        
        Order order = orderOpt
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        try {
            orderRepository.delete(order);
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete order: " + e.getMessage() + ". Order may be referenced by other records (payments, contracts, etc.).");
        }
    }
    
    public Order updateOrderStatus(UUID orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setStatus(OrderStatus.fromString(status));
        return orderRepository.save(order);
    }
    
    /**
     * Cancel an order and update inventory status
     */
    public Order cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        // Check if order can be cancelled
        OrderStatus currentStatus = order.getStatus() != null ? order.getStatus() : OrderStatus.PENDING;
        
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }
        
        if (currentStatus == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel a delivered order");
        }
        
        // Update order status
        order.setStatus(OrderStatus.CANCELLED);
        order.setDeliveryStatus(DeliveryStatus.CANCELLED);
        
        // Nếu Order có Quotation, không cần xử lý gì đặc biệt
        // Quotation vẫn giữ nguyên để lưu lịch sử
        // Order.quotation_id sẽ vẫn reference đến quotation (nullable = true)
        
        // Update inventory status if order has inventory (use lock to prevent race condition)
        if (order.getInventory() != null) {
            VehicleInventory inventory = vehicleInventoryRepository.lockById(order.getInventory().getInventoryId())
                    .orElseThrow(() -> new RuntimeException("Vehicle inventory not found with id: " + order.getInventory().getInventoryId()));
            
            // Only revert to available if inventory was reserved or sold for this order
            if (inventory.getStatus() == VehicleStatus.RESERVED || inventory.getStatus() == VehicleStatus.SOLD) {
                inventory.setStatus(VehicleStatus.AVAILABLE);
                inventory.setReservedForCustomer(null);
                inventory.setReservedDate(null);
                inventory.setReservedExpiryDate(null);
                vehicleInventoryRepository.save(inventory);
            }
        }
        
        return orderRepository.save(order);
    }
}
