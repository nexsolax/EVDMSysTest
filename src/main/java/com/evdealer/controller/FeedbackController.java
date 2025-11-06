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
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/feedbacks", "/api/customer-feedbacks"})
@CrossOrigin(origins = "*")
public class FeedbackController {
    
    @Autowired
    private CustomerFeedbackService customerFeedbackService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    public ResponseEntity<?> getAllFeedbacks() {
        try {
            List<CustomerFeedback> feedbacks = customerFeedbackService.getAllFeedbacks();
            List<Map<String, Object>> feedbackList = feedbacks.stream().map(this::feedbackToMap).collect(Collectors.toList());
            return ResponseEntity.ok(feedbackList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve feedbacks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    private Map<String, Object> feedbackToMap(CustomerFeedback feedback) {
        Map<String, Object> feedbackMap = new HashMap<>();
        feedbackMap.put("feedbackId", feedback.getFeedbackId());
        feedbackMap.put("rating", feedback.getRating());
        feedbackMap.put("feedbackType", feedback.getFeedbackType());
        feedbackMap.put("message", feedback.getMessage());
        feedbackMap.put("response", feedback.getResponse());
        feedbackMap.put("status", feedback.getStatus());
        feedbackMap.put("createdAt", feedback.getCreatedAt());
        feedbackMap.put("updatedAt", feedback.getUpdatedAt());
        
        if (feedback.getCustomer() != null) {
            feedbackMap.put("customerId", feedback.getCustomer().getCustomerId());
        }
        if (feedback.getOrder() != null) {
            feedbackMap.put("orderId", feedback.getOrder().getOrderId());
        }
        
        return feedbackMap;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getFeedbackById(@PathVariable UUID id) {
        try {
            return customerFeedbackService.getFeedbackById(id)
                    .map(feedback -> ResponseEntity.ok(feedbackToMap(feedback)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve feedback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getFeedbacksByCustomer(@PathVariable UUID customerId) {
        try {
            List<CustomerFeedback> feedbacks = customerFeedbackService.getFeedbacksByCustomer(customerId);
            List<Map<String, Object>> feedbackList = feedbacks.stream().map(this::feedbackToMap).collect(Collectors.toList());
            return ResponseEntity.ok(feedbackList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve feedbacks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getFeedbacksByOrder(@PathVariable UUID orderId) {
        try {
            List<CustomerFeedback> feedbacks = customerFeedbackService.getFeedbacksByOrder(orderId);
            List<Map<String, Object>> feedbackList = feedbacks.stream().map(this::feedbackToMap).collect(Collectors.toList());
            return ResponseEntity.ok(feedbackList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve feedbacks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getFeedbacksByStatus(@PathVariable String status) {
        try {
            List<CustomerFeedback> feedbacks = customerFeedbackService.getFeedbacksByStatus(status);
            List<Map<String, Object>> feedbackList = feedbacks.stream().map(this::feedbackToMap).collect(Collectors.toList());
            return ResponseEntity.ok(feedbackList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve feedbacks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/type/{feedbackType}")
    public ResponseEntity<?> getFeedbacksByType(@PathVariable String feedbackType) {
        try {
            List<CustomerFeedback> feedbacks = customerFeedbackService.getFeedbacksByType(feedbackType);
            List<Map<String, Object>> feedbackList = feedbacks.stream().map(this::feedbackToMap).collect(Collectors.toList());
            return ResponseEntity.ok(feedbackList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve feedbacks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/rating/{rating}")
    public ResponseEntity<?> getFeedbacksByRating(@PathVariable Integer rating) {
        try {
            List<CustomerFeedback> feedbacks = customerFeedbackService.getFeedbacksByRating(rating);
            List<Map<String, Object>> feedbackList = feedbacks.stream().map(this::feedbackToMap).collect(Collectors.toList());
            return ResponseEntity.ok(feedbackList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve feedbacks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/min-rating/{minRating}")
    public ResponseEntity<?> getFeedbacksByMinRating(@PathVariable Integer minRating) {
        try {
            List<CustomerFeedback> feedbacks = customerFeedbackService.getFeedbacksByMinRating(minRating);
            List<Map<String, Object>> feedbackList = feedbacks.stream().map(this::feedbackToMap).collect(Collectors.toList());
            return ResponseEntity.ok(feedbackList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve feedbacks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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
            return ResponseEntity.status(HttpStatus.CREATED).body(feedbackToMap(createdFeedback));
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
            return ResponseEntity.ok(feedbackToMap(updatedFeedback));
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
            return ResponseEntity.ok(feedbackToMap(updatedFeedback));
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
            return ResponseEntity.ok(feedbackToMap(updatedFeedback));
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
            return ResponseEntity.ok(feedbackToMap(updatedFeedback));
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

