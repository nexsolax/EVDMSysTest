package com.evdealer.controller;

import com.evdealer.dto.CustomerDTO;
import com.evdealer.dto.CustomerRequest;
import com.evdealer.entity.Customer;
import com.evdealer.service.CustomerService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
@Tag(name = "Customer Management", description = "APIs quản lý khách hàng")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy danh sách khách hàng", description = "Lấy tất cả khách hàng")
    public ResponseEntity<?> getAllCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            return ResponseEntity.ok(customers.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{customerId}")
    @Operation(summary = "Lấy khách hàng theo ID", description = "Lấy thông tin khách hàng theo ID")
    public ResponseEntity<?> getCustomerById(@PathVariable UUID customerId) {
        try {
            return customerService.getCustomerById(customerId)
                    .map(customer -> ResponseEntity.ok(toDTO(customer)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "Lấy khách hàng theo email", description = "Lấy thông tin khách hàng theo email")
    public ResponseEntity<?> getCustomerByEmail(@PathVariable String email) {
        try {
            return customerService.getCustomerByEmail(email)
                    .map(customer -> ResponseEntity.ok(toDTO(customer)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/phone/{phone}")
    @Operation(summary = "Lấy khách hàng theo số điện thoại", description = "Lấy thông tin khách hàng theo số điện thoại")
    public ResponseEntity<?> getCustomerByPhone(@PathVariable String phone) {
        try {
            return customerService.getCustomerByPhone(phone)
                    .map(customer -> ResponseEntity.ok(toDTO(customer)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm khách hàng", description = "Tìm kiếm khách hàng theo tên")
    public ResponseEntity<?> searchCustomersByName(@RequestParam String name) {
        try {
            List<Customer> customers = customerService.searchCustomersByName(name);
            return ResponseEntity.ok(customers.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/city/{city}")
    @Operation(summary = "Lấy khách hàng theo thành phố", description = "Lấy danh sách khách hàng theo thành phố")
    public ResponseEntity<?> getCustomersByCity(@PathVariable String city) {
        try {
            List<Customer> customers = customerService.getCustomersByCity(city);
            return ResponseEntity.ok(customers.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/province/{province}")
    @Operation(summary = "Lấy khách hàng theo tỉnh", description = "Lấy danh sách khách hàng theo tỉnh")
    public ResponseEntity<?> getCustomersByProvince(@PathVariable String province) {
        try {
            List<Customer> customers = customerService.getCustomersByProvince(province);
            return ResponseEntity.ok(customers.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Tạo khách hàng mới", description = "Tạo khách hàng mới")
    public ResponseEntity<?> createCustomer(@RequestBody CustomerRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Cho phép tất cả user đã authenticated tạo customer (bao gồm customer tự đăng ký, dealer user, EVM_STAFF, ADMIN)
            Customer createdCustomer = customerService.createCustomerFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdCustomer));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{customerId}")
    @Operation(summary = "Cập nhật khách hàng", description = "Cập nhật thông tin khách hàng")
    public ResponseEntity<?> updateCustomer(@PathVariable UUID customerId, @RequestBody CustomerRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Cho phép ADMIN, EVM_STAFF update customer, hoặc customer có thể update chính mình (nếu có user liên quan)
            // Hiện tại cho phép tất cả authenticated user update customer (có thể cần điều chỉnh sau)
            Customer updatedCustomer = customerService.updateCustomerFromRequest(customerId, request);
            return ResponseEntity.ok(toDTO(updatedCustomer));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{customerId}")
    @Operation(summary = "Xóa khách hàng", description = "Xóa khách hàng")
    public ResponseEntity<?> deleteCustomer(@PathVariable UUID customerId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa customer
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete customers");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            customerService.deleteCustomer(customerId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Customer deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            String errorMessage = e.getMessage();
            error.put("error", errorMessage);
            // Phân biệt giữa entity không tồn tại và lỗi foreign key constraint
            if (errorMessage != null && errorMessage.contains("Cannot delete")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else if (errorMessage != null && errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    private CustomerDTO toDTO(Customer c) {
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(c.getCustomerId());
        dto.setFirstName(c.getFirstName());
        dto.setLastName(c.getLastName());
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setCity(c.getCity());
        dto.setProvince(c.getProvince());
        dto.setDateOfBirth(c.getDateOfBirth());
        return dto;
    }
}

