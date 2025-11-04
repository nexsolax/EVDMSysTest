package com.evdealer.controller;

import com.evdealer.dto.QuotationDTO;
import com.evdealer.dto.QuotationRequest;
import com.evdealer.entity.Quotation;
import com.evdealer.service.QuotationService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotations")
@CrossOrigin(origins = "*")
@Tag(name = "Quotation Management", description = "APIs quản lý báo giá")
public class QuotationController {
    
    @Autowired
    private QuotationService quotationService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy danh sách báo giá", description = "Lấy tất cả báo giá")
    public ResponseEntity<List<QuotationDTO>> getAllQuotations() {
        List<Quotation> quotations = quotationService.getAllQuotations();
        return ResponseEntity.ok(quotations.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/{quotationId}")
    @Operation(summary = "Lấy báo giá theo ID", description = "Lấy thông tin báo giá theo ID")
    public ResponseEntity<QuotationDTO> getQuotationById(@PathVariable @Parameter(description = "Quotation ID") UUID quotationId) {
        return quotationService.getQuotationById(quotationId)
                .map(quotation -> ResponseEntity.ok(toDTO(quotation)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/number/{quotationNumber}")
    @Operation(summary = "Lấy báo giá theo số", description = "Lấy thông tin báo giá theo số báo giá")
    public ResponseEntity<QuotationDTO> getQuotationByNumber(@PathVariable String quotationNumber) {
        return quotationService.getQuotationByNumber(quotationNumber)
                .map(quotation -> ResponseEntity.ok(toDTO(quotation)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Lấy báo giá theo trạng thái", description = "Lấy báo giá theo trạng thái")
    public ResponseEntity<List<QuotationDTO>> getQuotationsByStatus(@PathVariable String status) {
        List<Quotation> quotations = quotationService.getQuotationsByStatus(status);
        return ResponseEntity.ok(quotations.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Lấy báo giá theo khách hàng", description = "Lấy báo giá theo khách hàng")
    public ResponseEntity<List<QuotationDTO>> getQuotationsByCustomer(@PathVariable UUID customerId) {
        List<Quotation> quotations = quotationService.getQuotationsByCustomer(customerId);
        return ResponseEntity.ok(quotations.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Lấy báo giá theo người dùng", description = "Lấy báo giá theo người dùng")
    public ResponseEntity<List<QuotationDTO>> getQuotationsByUser(@PathVariable UUID userId) {
        List<Quotation> quotations = quotationService.getQuotationsByUser(userId);
        return ResponseEntity.ok(quotations.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Lấy báo giá theo khoảng ngày", description = "Lấy báo giá theo khoảng ngày")
    public ResponseEntity<List<QuotationDTO>> getQuotationsByDateRange(
            @RequestParam @Parameter(description = "Start date") LocalDate startDate,
            @RequestParam @Parameter(description = "End date") LocalDate endDate) {
        List<Quotation> quotations = quotationService.getQuotationsByDateRange(startDate, endDate);
        return ResponseEntity.ok(quotations.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/expired")
    @Operation(summary = "Lấy báo giá đã hết hạn", description = "Lấy báo giá đã hết hạn")
    public ResponseEntity<List<QuotationDTO>> getExpiredQuotations() {
        List<Quotation> quotations = quotationService.getExpiredQuotations();
        return ResponseEntity.ok(quotations.stream().map(this::toDTO).toList());
    }
    
    @PostMapping
    @Operation(summary = "Tạo báo giá mới", description = "Tạo báo giá mới từ QuotationRequest DTO")
    public ResponseEntity<?> createQuotation(@RequestBody QuotationRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Cho phép tất cả user đã authenticated tạo quotation (bao gồm customer, dealer user, EVM_STAFF, ADMIN)
            Quotation createdQuotation = quotationService.createQuotationFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdQuotation));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/legacy")
    @Operation(summary = "Tạo báo giá mới (Legacy)", description = "Tạo báo giá mới từ Quotation entity (legacy method)")
    public ResponseEntity<?> createQuotationLegacy(@RequestBody Quotation quotation) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Cho phép tất cả user đã authenticated tạo quotation (bao gồm customer, dealer user, EVM_STAFF, ADMIN)
            Quotation createdQuotation = quotationService.createQuotation(quotation);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdQuotation));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{quotationId}")
    @Operation(summary = "Cập nhật báo giá", description = "Cập nhật báo giá")
    public ResponseEntity<?> updateQuotation(
            @PathVariable UUID quotationId, 
            @RequestBody QuotationRequest request) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Lấy quotation hiện tại để kiểm tra ownership
            Quotation existingQuotation = quotationService.getQuotationById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
            
            // Kiểm tra phân quyền: ADMIN, EVM_STAFF hoặc user tạo quotation
            if (!securityUtils.isAdmin() && !securityUtils.isEvmStaff()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent()) {
                    UUID currentUserId = currentUserOpt.get().getUserId();
                    // User chỉ có thể update quotation của chính mình (nếu quotation có user)
                    if (existingQuotation.getUser() != null && !existingQuotation.getUser().getUserId().equals(currentUserId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only update your own quotations");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only admin, EVM staff or the quotation creator can update quotations");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            Quotation updatedQuotation = quotationService.updateQuotationFromRequest(quotationId, request);
            return ResponseEntity.ok(toDTO(updatedQuotation));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{quotationId}/status")
    @Operation(summary = "Cập nhật trạng thái báo giá", description = "Cập nhật trạng thái báo giá")
    public ResponseEntity<?> updateQuotationStatus(
            @PathVariable UUID quotationId, 
            @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update quotation status
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update quotation status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Quotation updatedQuotation = quotationService.updateQuotationStatus(quotationId, status);
            return ResponseEntity.ok(toDTO(updatedQuotation));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update quotation status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update quotation status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{quotationId}")
    @Operation(summary = "Xóa báo giá", description = "Xóa báo giá")
    public ResponseEntity<?> deleteQuotation(@PathVariable UUID quotationId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa quotation
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete quotations");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            quotationService.deleteQuotation(quotationId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Quotation deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete quotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private QuotationDTO toDTO(Quotation q) {
        QuotationDTO dto = new QuotationDTO();
        dto.setQuotationId(q.getQuotationId());
        dto.setQuotationNumber(q.getQuotationNumber());
        dto.setCustomerId(q.getCustomer() != null ? q.getCustomer().getCustomerId() : null);
        dto.setUserId(q.getUser() != null ? q.getUser().getUserId() : null);
        dto.setVariantId(q.getVariant() != null ? q.getVariant().getVariantId() : null);
        dto.setColorId(q.getColor() != null ? q.getColor().getColorId() : null);
        dto.setQuotationDate(q.getQuotationDate());
        dto.setTotalPrice(q.getTotalPrice());
        dto.setFinalPrice(q.getFinalPrice());
        dto.setStatus(q.getStatus());
        return dto;
    }
}
