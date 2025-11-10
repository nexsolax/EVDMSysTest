package com.evdealer.service;

import com.evdealer.entity.Customer;
import com.evdealer.dto.CustomerRequest;
import com.evdealer.repository.CustomerRepository;
import com.evdealer.repository.OrderRepository;
import com.evdealer.repository.VehicleInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private VehicleInventoryRepository vehicleInventoryRepository;
    
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        try {
            List<Customer> customers = customerRepository.findAll();
            // Fix any ContactMethod enum issues
            for (Customer customer : customers) {
                try {
                    if (customer.getPreferredContactMethod() != null) {
                        // Ensure enum is valid
                        customer.getPreferredContactMethod().getValue();
                    }
                } catch (Exception e) {
                    // If enum is invalid, set to default
                    customer.setPreferredContactMethod(com.evdealer.enums.ContactMethod.EMAIL);
                }
            }
            return customers;
        } catch (Exception e) {
            // Return empty list if there's an issue
            return new java.util.ArrayList<>();
        }
    }
    
    public Optional<Customer> getCustomerById(UUID customerId) {
        return customerRepository.findById(customerId);
    }
    
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }
    
    public Optional<Customer> getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }
    
    public List<Customer> searchCustomersByName(String name) {
        return customerRepository.findByNameContaining(name);
    }
    
    public List<Customer> getCustomersByCity(String city) {
        return customerRepository.findByCity(city);
    }
    
    public List<Customer> getCustomersByProvince(String province) {
        return customerRepository.findByProvince(province);
    }
    
    public Customer createCustomer(Customer customer) {
        if (customer.getEmail() != null && customerRepository.existsByEmail(customer.getEmail())) {
            throw new RuntimeException("Email already exists: " + customer.getEmail());
        }
        if (customer.getPhone() != null && customerRepository.existsByPhone(customer.getPhone())) {
            throw new RuntimeException("Phone already exists: " + customer.getPhone());
        }
        return customerRepository.save(customer);
    }
    
    public Customer createCustomerFromRequest(CustomerRequest request) {
        // Validate required fields
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Last name is required");
        }
        
        // Check for duplicate email
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        
        // Check for duplicate phone
        if (request.getPhone() != null && customerRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already exists: " + request.getPhone());
        }
        
        // Create Customer entity
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName().trim());
        customer.setLastName(request.getLastName().trim());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setProvince(request.getProvince());
        customer.setPostalCode(request.getPostalCode());
        customer.setCreditScore(request.getCreditScore());
        customer.setPreferredContactMethod(request.getPreferredContactMethod());
        customer.setNotes(request.getNotes());
        
        return customerRepository.save(customer);
    }
    
    public Customer updateCustomer(UUID customerId, Customer customerDetails) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        // Check for duplicate email (excluding current customer)
        if (customerDetails.getEmail() != null && 
            !customer.getEmail().equals(customerDetails.getEmail()) && 
            customerRepository.existsByEmail(customerDetails.getEmail())) {
            throw new RuntimeException("Email already exists: " + customerDetails.getEmail());
        }
        
        // Check for duplicate phone (excluding current customer)
        if (customerDetails.getPhone() != null && 
            !customer.getPhone().equals(customerDetails.getPhone()) && 
            customerRepository.existsByPhone(customerDetails.getPhone())) {
            throw new RuntimeException("Phone already exists: " + customerDetails.getPhone());
        }
        
        customer.setFirstName(customerDetails.getFirstName());
        customer.setLastName(customerDetails.getLastName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setDateOfBirth(customerDetails.getDateOfBirth());
        customer.setAddress(customerDetails.getAddress());
        customer.setCity(customerDetails.getCity());
        customer.setProvince(customerDetails.getProvince());
        customer.setPostalCode(customerDetails.getPostalCode());
        customer.setCreditScore(customerDetails.getCreditScore());
        customer.setPreferredContactMethod(customerDetails.getPreferredContactMethod());
        customer.setNotes(customerDetails.getNotes());
        
        return customerRepository.save(customer);
    }
    
    public Customer updateCustomerFromRequest(UUID customerId, CustomerRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        // Check for duplicate email (excluding current customer)
        if (request.getEmail() != null && 
            !customer.getEmail().equals(request.getEmail()) && 
            customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        
        // Check for duplicate phone (excluding current customer)
        if (request.getPhone() != null && 
            !customer.getPhone().equals(request.getPhone()) && 
            customerRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already exists: " + request.getPhone());
        }
        
        // Update fields
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            customer.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            customer.setLastName(request.getLastName().trim());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getDateOfBirth() != null) {
            customer.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            customer.setCity(request.getCity());
        }
        if (request.getProvince() != null) {
            customer.setProvince(request.getProvince());
        }
        if (request.getPostalCode() != null) {
            customer.setPostalCode(request.getPostalCode());
        }
        if (request.getCreditScore() != null) {
            customer.setCreditScore(request.getCreditScore());
        }
        if (request.getPreferredContactMethod() != null) {
            customer.setPreferredContactMethod(request.getPreferredContactMethod());
        }
        if (request.getNotes() != null) {
            customer.setNotes(request.getNotes());
        }
        
        return customerRepository.save(customer);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearCustomerReferences(UUID customerId) {
        // Bước 1: Set null reserved_for_customer trong vehicle_inventory
        try {
            vehicleInventoryRepository.clearReservedForCustomer(customerId);
        } catch (Exception e) {
            System.err.println("Warning: Could not clear reserved_for_customer for customer " + customerId + ": " + e.getMessage());
        }
        
        // Bước 2: Xóa các bảng con của Orders trước (theo thứ tự foreign key)
        try {
            // Xóa CustomerPayments
            orderRepository.deleteCustomerPaymentsByCustomerId(customerId);
        } catch (Exception e) {
            System.err.println("Warning: Could not delete customer payments: " + e.getMessage());
        }
        
        try {
            // Xóa SalesContracts
            orderRepository.deleteSalesContractsByCustomerId(customerId);
        } catch (Exception e) {
            System.err.println("Warning: Could not delete sales contracts: " + e.getMessage());
        }
        
        try {
            // Xóa VehicleDeliveries (quan trọng vì nullable = false)
            orderRepository.deleteVehicleDeliveriesByCustomerId(customerId);
        } catch (Exception e) {
            System.err.println("Warning: Could not delete vehicle deliveries: " + e.getMessage());
        }
        
        try {
            // Xóa InstallmentPlans
            orderRepository.deleteInstallmentPlansByCustomerId(customerId);
        } catch (Exception e) {
            System.err.println("Warning: Could not delete installment plans: " + e.getMessage());
        }
        
        // Bước 3: Xóa tất cả Orders của Customer
        try {
            orderRepository.deleteByCustomerIdNative(customerId);
        } catch (Exception e) {
            System.err.println("Warning: Native delete failed, trying individual delete: " + e.getMessage());
            // Thử cách khác: tìm và xóa từng order
            try {
                List<com.evdealer.entity.Order> orders = orderRepository.findByCustomerIdNative(customerId);
                if (orders != null && !orders.isEmpty()) {
                    for (com.evdealer.entity.Order order : orders) {
                        try {
                            orderRepository.delete(order);
                        } catch (Exception e2) {
                            System.err.println("Warning: Could not delete order " + order.getOrderId() + ": " + e2.getMessage());
                        }
                    }
                }
            } catch (Exception e2) {
                System.err.println("Warning: Could not find or delete orders for customer " + customerId + ": " + e2.getMessage());
            }
        }
    }
    
    public void deleteCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        // Xóa các references trước (trong transaction riêng)
        try {
            clearCustomerReferences(customerId);
        } catch (Exception e) {
            System.err.println("Warning: Could not clear references for customer " + customerId + ": " + e.getMessage());
        }
        
        // Xóa Customer
        try {
            customerRepository.delete(customer);
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete customer: " + e.getMessage() + ". Customer may be referenced by other records (payments, feedback, etc.).");
        }
    }
}

