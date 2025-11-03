package com.evdealer.controller;

import com.evdealer.dto.QuotationDTO;
import com.evdealer.dto.QuotationRequest;
import com.evdealer.entity.Quotation;
import com.evdealer.service.QuotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotations")
@CrossOrigin(origins = "*")
@Tag(name = "Quotation Management", description = "APIs quản lý báo giá")
public class QuotationController {
    
    @Autowired
    private QuotationService quotationService;
    
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
    public ResponseEntity<QuotationDTO> createQuotation(@RequestBody QuotationRequest request) {
        try {
            Quotation createdQuotation = quotationService.createQuotationFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdQuotation));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/legacy")
    @Operation(summary = "Tạo báo giá mới (Legacy)", description = "Tạo báo giá mới từ Quotation entity (legacy method)")
    public ResponseEntity<QuotationDTO> createQuotationLegacy(@RequestBody Quotation quotation) {
        try {
            Quotation createdQuotation = quotationService.createQuotation(quotation);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdQuotation));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{quotationId}")
    @Operation(summary = "Cập nhật báo giá", description = "Cập nhật báo giá")
    public ResponseEntity<QuotationDTO> updateQuotation(
            @PathVariable UUID quotationId, 
            @RequestBody QuotationRequest request) {
        try {
            Quotation updatedQuotation = quotationService.updateQuotationFromRequest(quotationId, request);
            return ResponseEntity.ok(toDTO(updatedQuotation));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{quotationId}/status")
    @Operation(summary = "Cập nhật trạng thái báo giá", description = "Cập nhật trạng thái báo giá")
    public ResponseEntity<QuotationDTO> updateQuotationStatus(
            @PathVariable UUID quotationId, 
            @RequestParam String status) {
        try {
            Quotation updatedQuotation = quotationService.updateQuotationStatus(quotationId, status);
            return ResponseEntity.ok(toDTO(updatedQuotation));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{quotationId}")
    @Operation(summary = "Xóa báo giá", description = "Xóa báo giá")
    public ResponseEntity<Void> deleteQuotation(@PathVariable UUID quotationId) {
        try {
            quotationService.deleteQuotation(quotationId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
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
