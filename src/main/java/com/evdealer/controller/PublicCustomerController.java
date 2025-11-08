package com.evdealer.controller;

import com.evdealer.entity.Customer;
import com.evdealer.dto.CustomerRequest;
import com.evdealer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/customers")
@CrossOrigin(origins = "*")
@Tag(name = "Public Customer Management", description = "APIs khách hàng cho khách vãng lai - không cần đăng nhập")
public class PublicCustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    @Operation(summary = "Tạo khách hàng", description = "Khách vãng lai có thể khai báo thông tin để mua hàng")
    public ResponseEntity<?> createCustomer(@RequestBody CustomerRequest request) {
        try {
            Customer created = customerService.createCustomerFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Customer creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Xem khách hàng", description = "Tra cứu khách hàng theo ID")
    public ResponseEntity<?> getCustomerById(@PathVariable UUID customerId) {
        try {
            return customerService.getCustomerById(customerId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Xem theo email", description = "Tra cứu khách hàng theo email")
    public ResponseEntity<?> getByEmail(@PathVariable String email) {
        try {
            return customerService.getCustomerByEmail(email)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "Xem theo số điện thoại", description = "Tra cứu khách hàng theo số điện thoại")
    public ResponseEntity<?> getByPhone(@PathVariable String phone) {
        try {
            return customerService.getCustomerByPhone(phone)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}


