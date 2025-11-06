package com.evdealer.service;

import com.evdealer.entity.Customer;
import com.evdealer.dto.CustomerRequest;
import com.evdealer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
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
    
    public void deleteCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        try {
            customerRepository.delete(customer);
        } catch (Exception e) {
            throw new RuntimeException("Cannot delete customer: " + e.getMessage() + ". Customer may be referenced by other records (orders, payments, feedback, etc.).");
        }
    }
}

