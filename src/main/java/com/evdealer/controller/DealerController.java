package com.evdealer.controller;

import com.evdealer.dto.DealerRequest;
import com.evdealer.entity.Dealer;
import com.evdealer.service.DealerService;
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
@RequestMapping("/api/dealers")
@CrossOrigin(origins = "*")
@Tag(name = "Dealer Management", description = "APIs quản lý đại lý")
public class DealerController {
    
    @Autowired
    private DealerService dealerService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả đại lý", description = "Lấy danh sách tất cả đại lý trong hệ thống")
    public ResponseEntity<List<Dealer>> getAllDealers() {
        List<Dealer> dealers = dealerService.getAllDealers();
        return ResponseEntity.ok(dealers);
    }
    
    @GetMapping("/options")
    @Operation(summary = "Lấy danh sách đại lý cho dropdown", description = "Lấy danh sách đại lý dạng đơn giản (ID, Name, Code) để hiển thị trong dropdown/select")
    public ResponseEntity<List<Map<String, Object>>> getDealerOptions() {
        List<Dealer> dealers = dealerService.getAllDealers();
        List<Map<String, Object>> options = dealers.stream()
            .map(dealer -> {
                Map<String, Object> option = new HashMap<>();
                option.put("dealerId", dealer.getDealerId());
                option.put("dealerName", dealer.getDealerName());
                option.put("dealerCode", dealer.getDealerCode());
                return option;
            })
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(options);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getDealerById(@PathVariable UUID id) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Dealer dealer = dealerService.getDealerById(id)
                .orElseThrow(() -> new RuntimeException("Dealer not found"));
            
            // Dealer user chỉ có thể xem thông tin của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    if (!dealer.getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view information of your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            return ResponseEntity.ok(dealer);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get dealer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get dealer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/code/{dealerCode}")
    public ResponseEntity<Dealer> getDealerByCode(@PathVariable String dealerCode) {
        return dealerService.getDealerByCode(dealerCode)
                .map(dealer -> ResponseEntity.ok(dealer))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Dealer>> getDealersByStatus(@PathVariable String status) {
        List<Dealer> dealers = dealerService.getDealersByStatus(status);
        return ResponseEntity.ok(dealers);
    }
    
    @GetMapping("/type/{dealerType}")
    public ResponseEntity<List<Dealer>> getDealersByType(@PathVariable String dealerType) {
        List<Dealer> dealers = dealerService.getDealersByType(dealerType);
        return ResponseEntity.ok(dealers);
    }
    
    @GetMapping("/city/{city}")
    public ResponseEntity<List<Dealer>> getDealersByCity(@PathVariable String city) {
        List<Dealer> dealers = dealerService.getDealersByCity(city);
        return ResponseEntity.ok(dealers);
    }
    
    @GetMapping("/province/{province}")
    public ResponseEntity<List<Dealer>> getDealersByProvince(@PathVariable String province) {
        List<Dealer> dealers = dealerService.getDealersByProvince(province);
        return ResponseEntity.ok(dealers);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Dealer>> getDealersByName(@RequestParam String name) {
        List<Dealer> dealers = dealerService.getDealersByName(name);
        return ResponseEntity.ok(dealers);
    }
    
    @GetMapping("/contact")
    public ResponseEntity<List<Dealer>> getDealersByContactPerson(@RequestParam String contactPerson) {
        List<Dealer> dealers = dealerService.getDealersByContactPerson(contactPerson);
        return ResponseEntity.ok(dealers);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<Dealer> getDealerByEmail(@PathVariable String email) {
        return dealerService.getDealerByEmail(email)
                .map(dealer -> ResponseEntity.ok(dealer))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/phone/{phone}")
    public ResponseEntity<Dealer> getDealerByPhone(@PathVariable String phone) {
        return dealerService.getDealerByPhone(phone)
                .map(dealer -> ResponseEntity.ok(dealer))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Tạo đại lý mới", description = "Tạo đại lý mới")
    public ResponseEntity<?> createDealer(@RequestBody Dealer dealer) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo đại lý
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create dealers");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Dealer createdDealer = dealerService.createDealer(dealer);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDealer);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create dealer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create dealer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/dto")
    @Operation(summary = "Tạo đại lý mới từ DTO", description = "Tạo đại lý mới từ DealerRequest DTO")
    public ResponseEntity<?> createDealerFromRequest(@RequestBody DealerRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo đại lý
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create dealers");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Dealer createdDealer = dealerService.createDealerFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDealer);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create dealer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create dealer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDealer(@PathVariable UUID id, @RequestBody Dealer dealerDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra phân quyền: ADMIN, EVM_STAFF hoặc dealer user của chính dealer đó
            Dealer existingDealer = dealerService.getDealerById(id)
                .orElseThrow(() -> new RuntimeException("Dealer not found"));
            
            if (!securityUtils.isAdmin() && !securityUtils.isEvmStaff()) {
                // Kiểm tra dealer user chỉ có thể update dealer của mình
                if (securityUtils.isDealerUser()) {
                    var currentUserOpt = securityUtils.getCurrentUser();
                    if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                        UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                        if (!existingDealer.getDealerId().equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only update your own dealer information");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    } else {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. Only admin, EVM staff or dealer users can update dealers");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only admin, EVM staff or dealer users can update dealers");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            Dealer updatedDealer = dealerService.updateDealer(id, dealerDetails);
            return ResponseEntity.ok(updatedDealer);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update dealer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update dealer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDealerStatus(@PathVariable UUID id, @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update status
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update dealer status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Dealer updatedDealer = dealerService.updateDealerStatus(id, status);
            return ResponseEntity.ok(updatedDealer);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update dealer status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update dealer status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDealer(@PathVariable UUID id) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa đại lý
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete dealers");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            dealerService.deleteDealer(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Dealer deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete dealer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete dealer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

