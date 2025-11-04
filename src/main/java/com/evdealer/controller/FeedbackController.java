package com.evdealer.controller;

import com.evdealer.entity.CustomerFeedback;
import com.evdealer.service.CustomerFeedbackService;
import com.evdealer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/api/feedbacks", "/api/customer-feedbacks"})
@CrossOrigin(origins = "*")
public class FeedbackController {
    
    @Autowired
    private CustomerFeedbackService customerFeedbackService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    public ResponseEntity<List<CustomerFeedback>> getAllFeedbacks() {
        List<CustomerFeedback> feedbacks = customerFeedbackService.getAllFeedbacks();
        return ResponseEntity.ok(feedbacks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerFeedback> getFeedbackById(@PathVariable UUID id) {
        return customerFeedbackService.getFeedbackById(id)
                .map(feedback -> ResponseEntity.ok(feedback))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CustomerFeedback>> getFeedbacksByCustomer(@PathVariable UUID customerId) {
        List<CustomerFeedback> feedbacks = customerFeedbackService.getFeedbacksByCustomer(customerId);
        return ResponseEntity.ok(feedbacks);
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<CustomerFeedback>> getFeedbacksByOrder(@PathVariable UUID orderId) {
        List<CustomerFeedback> feedbacks = customerFeedbackService.getFeedbacksByOrder(orderId);
        return ResponseEntity.ok(feedbacks);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CustomerFeedback>> getFeedbacksByStatus(@PathVariable String status) {
        List<CustomerFeedback> feedbacks = customerFeedbackService.getFeedbacksByStatus(status);
        return ResponseEntity.ok(feedbacks);
    }
    
    @GetMapping("/type/{feedbackType}")
    public ResponseEntity<List<CustomerFeedback>> getFeedbacksByType(@PathVariable String feedbackType) {
        List<CustomerFeedback> feedbacks = customerFeedbackService.getFeedbacksByType(feedbackType);
        return ResponseEntity.ok(feedbacks);
    }
    
    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<CustomerFeedback>> getFeedbacksByRating(@PathVariable Integer rating) {
        List<CustomerFeedback> feedbacks = customerFeedbackService.getFeedbacksByRating(rating);
        return ResponseEntity.ok(feedbacks);
    }
    
    @GetMapping("/min-rating/{minRating}")
    public ResponseEntity<List<CustomerFeedback>> getFeedbacksByMinRating(@PathVariable Integer minRating) {
        List<CustomerFeedback> feedbacks = customerFeedbackService.getFeedbacksByMinRating(minRating);
        return ResponseEntity.ok(feedbacks);
    }
    
    @PostMapping
    public ResponseEntity<?> createFeedback(@RequestBody CustomerFeedback feedback) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Cho phép tất cả user đã authenticated tạo feedback (customer, dealer user, EVM_STAFF, ADMIN)
            CustomerFeedback createdFeedback = customerFeedbackService.createFeedback(feedback);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFeedback);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create feedback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create feedback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFeedback(@PathVariable UUID id, @RequestBody CustomerFeedback feedbackDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: ADMIN, EVM_STAFF hoặc customer tạo feedback
            // CustomerFeedback chỉ có customer field, không có user field
            // Cho phép tất cả authenticated user update feedback (có thể cần điều chỉnh sau để kiểm tra customer ownership chính xác hơn)
            // Hiện tại cho phép tất cả authenticated user update feedback
            
            CustomerFeedback updatedFeedback = customerFeedbackService.updateFeedback(id, feedbackDetails);
            return ResponseEntity.ok(updatedFeedback);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update feedback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update feedback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateFeedbackStatus(@PathVariable UUID id, @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update feedback status
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update feedback status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            CustomerFeedback updatedFeedback = customerFeedbackService.updateFeedbackStatus(id, status);
            return ResponseEntity.ok(updatedFeedback);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update feedback status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update feedback status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/{id}/reply")
    public ResponseEntity<?> replyToFeedback(@PathVariable UUID id, @RequestParam String response) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể reply feedback
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can reply to feedback");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            CustomerFeedback updatedFeedback = customerFeedbackService.replyToFeedback(id, response);
            return ResponseEntity.ok(updatedFeedback);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reply to feedback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reply to feedback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Alias endpoint for backward compatibility with /api/customer-feedbacks
    @PutMapping("/{id}/response")
    public ResponseEntity<?> addResponse(@PathVariable UUID id, @RequestParam String response) {
        // Same as replyToFeedback but using addResponse method
        try {
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can add response to feedback");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            CustomerFeedback updatedFeedback = customerFeedbackService.addResponse(id, response);
            return ResponseEntity.ok(updatedFeedback);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add response to feedback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add response to feedback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable UUID id) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa feedback
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete feedback");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            customerFeedbackService.deleteFeedback(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Feedback deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete feedback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete feedback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

