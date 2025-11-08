package com.evdealer.controller;

import com.evdealer.entity.DealerTarget;
import com.evdealer.service.DealerTargetService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dealer-targets")
@CrossOrigin(origins = "*")
@Tag(name = "Dealer Target Management", description = "APIs quản lý mục tiêu đại lý")
public class DealerTargetController {
    
    @Autowired
    private DealerTargetService dealerTargetService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    private Map<String, Object> targetToMap(DealerTarget target) {
        Map<String, Object> map = new HashMap<>();
        map.put("targetId", target.getTargetId());
        map.put("targetYear", target.getTargetYear());
        map.put("targetMonth", target.getTargetMonth());
        map.put("targetType", target.getTargetType());
        map.put("targetAmount", target.getTargetAmount());
        map.put("targetQuantity", target.getTargetQuantity());
        map.put("achievedAmount", target.getAchievedAmount());
        map.put("achievedQuantity", target.getAchievedQuantity());
        map.put("achievementRate", target.getAchievementRate());
        map.put("targetStatus", target.getTargetStatus());
        map.put("targetScope", target.getTargetScope());
        map.put("notes", target.getNotes());
        map.put("createdAt", target.getCreatedAt());
        map.put("updatedAt", target.getUpdatedAt());
        if (target.getDealer() != null) {
            map.put("dealerId", target.getDealer().getDealerId());
        }
        return map;
    }
    
    @GetMapping
    @Operation(summary = "Lấy danh sách mục tiêu", description = "Lấy tất cả mục tiêu đại lý")
    public ResponseEntity<?> getAllTargets() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerTarget> targets = dealerTargetService.getAllTargets();
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    targets = targets.stream()
                        .filter(target -> target.getDealer() != null && target.getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{targetId}")
    @Operation(summary = "Lấy mục tiêu theo ID", description = "Lấy thông tin mục tiêu theo ID")
    public ResponseEntity<?> getTargetById(@PathVariable UUID targetId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerTarget target = dealerTargetService.getTargetById(targetId)
                .orElseThrow(() -> new RuntimeException("Target not found"));
            
            // Dealer user chỉ có thể xem target của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (target.getDealer() != null && !target.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view targets for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            return ResponseEntity.ok(targetToMap(target));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get target: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get target: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/year/{targetYear}")
    public ResponseEntity<?> getTargetsByYear(@PathVariable Integer targetYear) {
        try {
            List<DealerTarget> targets = dealerTargetService.getTargetsByYear(targetYear);
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/month/{targetMonth}")
    public ResponseEntity<?> getTargetsByMonth(@PathVariable Integer targetMonth) {
        try {
            List<DealerTarget> targets = dealerTargetService.getTargetsByMonth(targetMonth);
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/type/{targetType}")
    public ResponseEntity<?> getTargetsByType(@PathVariable String targetType) {
        try {
            List<DealerTarget> targets = dealerTargetService.getTargetsByType(targetType);
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/status/{targetStatus}")
    public ResponseEntity<?> getTargetsByStatus(@PathVariable String targetStatus) {
        try {
            List<DealerTarget> targets = dealerTargetService.getTargetsByStatus(targetStatus);
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/dealer/{dealerId}")
    @Operation(summary = "Lấy mục tiêu theo đại lý", description = "Lấy tất cả mục tiêu của một đại lý cụ thể")
    public ResponseEntity<?> getTargetsByDealer(@PathVariable UUID dealerId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem targets của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view targets for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            List<DealerTarget> targets = dealerTargetService.getTargetsByDealer(dealerId);
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/scope/{targetScope}")
    @Operation(summary = "Lấy mục tiêu theo phạm vi", description = "Lấy mục tiêu theo phạm vi (dealer, global)")
    public ResponseEntity<?> getTargetsByScope(@PathVariable String targetScope) {
        try {
            List<DealerTarget> targets = dealerTargetService.getTargetsByScope(targetScope);
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/dealer-specific")
    @Operation(summary = "Lấy mục tiêu đại lý cụ thể", description = "Lấy tất cả mục tiêu dành riêng cho đại lý")
    public ResponseEntity<?> getDealerSpecificTargets() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerTarget> targets = dealerTargetService.getTargetsByScope("dealer");
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    targets = targets.stream()
                        .filter(target -> target.getDealer() != null && target.getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get dealer-specific targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/dealer/{dealerId}/year/{targetYear}")
    @Operation(summary = "Lấy mục tiêu đại lý theo năm", description = "Lấy mục tiêu của đại lý trong một năm cụ thể")
    public ResponseEntity<?> getTargetsByDealerAndYear(@PathVariable UUID dealerId, @PathVariable Integer targetYear) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra dealer user chỉ có thể xem targets của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                if (currentUser.getDealer() != null) {
                    UUID userDealerId = currentUser.getDealer().getDealerId();
                    if (!dealerId.equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view targets for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            List<DealerTarget> targets = dealerTargetService.getTargetsByDealerAndYear(dealerId, targetYear);
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/year/{targetYear}/month/{targetMonth}")
    public ResponseEntity<?> getTargetsByYearAndMonth(@PathVariable Integer targetYear, @PathVariable Integer targetMonth) {
        try {
            List<DealerTarget> targets = dealerTargetService.getTargetsByYearAndMonth(targetYear, targetMonth);
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/year/{targetYear}/type/{targetType}")
    public ResponseEntity<?> getTargetsByYearAndType(@PathVariable Integer targetYear, @PathVariable String targetType) {
        try {
            List<DealerTarget> targets = dealerTargetService.getTargetsByYearAndType(targetYear, targetType);
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/achievement-rate/min/{minRate}")
    public ResponseEntity<?> getTargetsByAchievementRateGreaterThanEqual(@PathVariable Double minRate) {
        try {
            List<DealerTarget> targets = dealerTargetService.getTargetsByAchievementRateGreaterThanEqual(minRate);
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/achievement-rate/max/{maxRate}")
    public ResponseEntity<?> getTargetsByAchievementRateLessThan(@PathVariable Double maxRate) {
        try {
            List<DealerTarget> targets = dealerTargetService.getTargetsByAchievementRateLessThan(maxRate);
            List<Map<String, Object>> targetList = targets.stream().map(this::targetToMap).collect(Collectors.toList());
            return ResponseEntity.ok(targetList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    @Operation(summary = "Tạo mục tiêu mới", description = "Tạo mục tiêu đại lý mới")
    public ResponseEntity<?> createTarget(@RequestBody DealerTarget target) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo target
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create targets");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerTarget createdTarget = dealerTargetService.createTarget(target);
            return ResponseEntity.status(HttpStatus.CREATED).body(targetToMap(createdTarget));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create target: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create target: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{targetId}")
    @Operation(summary = "Cập nhật mục tiêu", description = "Cập nhật thông tin mục tiêu")
    public ResponseEntity<?> updateTarget(@PathVariable UUID targetId, @RequestBody DealerTarget targetDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update target
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update targets");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerTarget updatedTarget = dealerTargetService.updateTarget(targetId, targetDetails);
            return ResponseEntity.ok(targetToMap(updatedTarget));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update target: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update target: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{targetId}/status")
    public ResponseEntity<?> updateTargetStatus(@PathVariable UUID targetId, @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update target status
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update target status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerTarget updatedTarget = dealerTargetService.updateTargetStatus(targetId, status);
            return ResponseEntity.ok(targetToMap(updatedTarget));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update target status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update target status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{targetId}/achievement")
    public ResponseEntity<?> updateAchievement(@PathVariable UUID targetId, @RequestParam BigDecimal achievedAmount, @RequestParam Integer achievedQuantity) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Lấy target hiện tại để kiểm tra ownership
            DealerTarget existingTarget = dealerTargetService.getTargetById(targetId)
                .orElseThrow(() -> new RuntimeException("Target not found"));
            
            // Kiểm tra phân quyền: ADMIN, EVM_STAFF hoặc dealer user của chính dealer đó
            if (!securityUtils.isAdmin() && !securityUtils.isEvmStaff()) {
                // Kiểm tra dealer user chỉ có thể update achievement của target của dealer mình
                if (securityUtils.isDealerUser()) {
                    var currentUser = securityUtils.getCurrentUser()
                        .orElseThrow(() -> new RuntimeException("User not authenticated"));
                    if (currentUser.getDealer() != null) {
                        UUID userDealerId = currentUser.getDealer().getDealerId();
                        if (existingTarget.getDealer() != null && !existingTarget.getDealer().getDealerId().equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only update achievement for targets of your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    } else {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. Only admin, EVM staff or dealer users can update achievement");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only admin, EVM staff or dealer users can update achievement");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            DealerTarget updatedTarget = dealerTargetService.updateAchievement(targetId, achievedAmount, achievedQuantity);
            return ResponseEntity.ok(targetToMap(updatedTarget));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update achievement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update achievement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{targetId}")
    @Operation(summary = "Xóa mục tiêu", description = "Xóa mục tiêu")
    public ResponseEntity<?> deleteTarget(@PathVariable UUID targetId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa target
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete targets");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            dealerTargetService.deleteTarget(targetId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Target deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete target: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete target: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
