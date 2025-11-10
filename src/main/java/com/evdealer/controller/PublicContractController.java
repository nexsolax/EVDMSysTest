package com.evdealer.controller;

import com.evdealer.entity.SalesContract;
import com.evdealer.enums.SalesContractStatus;
import com.evdealer.service.SalesContractService;
import com.evdealer.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/contracts")
@CrossOrigin(origins = "*")
@Tag(name = "Public Contract Management", description = "APIs hợp đồng điện tử cho khách vãng lai - không cần đăng nhập")
public class PublicContractController {
    
    @Autowired
    private SalesContractService salesContractService;
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Xem hợp đồng theo đơn hàng", description = "Khách vãng lai có thể xem hợp đồng theo đơn hàng")
    public ResponseEntity<?> getContractByOrder(@PathVariable UUID orderId) {
        try {
            List<SalesContract> contracts = salesContractService.getContractsByOrder(orderId);
            
            if (contracts.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No contract found for this order");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("contracts", contracts);
            response.put("contractCount", contracts.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{contractId}")
    @Operation(summary = "Xem chi tiết hợp đồng", description = "Khách vãng lai có thể xem chi tiết hợp đồng")
    public ResponseEntity<?> getContractById(@PathVariable UUID contractId) {
        try {
            return salesContractService.getContractById(contractId)
                    .map(contract -> {
                        Map<String, Object> contractDetails = new HashMap<>();
                        contractDetails.put("contractId", contract.getContractId());
                        contractDetails.put("contractNumber", contract.getContractNumber());
                        contractDetails.put("status", contract.getContractStatus() != null ? contract.getContractStatus().getValue() : null);
                        contractDetails.put("contractDate", contract.getContractDate());
                        contractDetails.put("totalAmount", contract.getContractValue());
                        contractDetails.put("orderId", contract.getOrder().getOrderId());
                        contractDetails.put("orderNumber", contract.getOrder().getOrderNumber());
                        contractDetails.put("customerName", contract.getOrder().getCustomer() != null ?
                            contract.getOrder().getCustomer().getFirstName() + " " + 
                            contract.getOrder().getCustomer().getLastName() : "N/A");
                        contractDetails.put("vehicleInfo", contract.getOrder().getQuotation() != null && 
                            contract.getOrder().getQuotation().getVariant() != null ?
                            contract.getOrder().getQuotation().getVariant().getModel().getBrand().getBrandName() + " " +
                            contract.getOrder().getQuotation().getVariant().getModel().getModelName() + " " +
                            contract.getOrder().getQuotation().getVariant().getVariantName() : "N/A");
                        contractDetails.put("terms", contract.getPaymentTerms());
                        contractDetails.put("notes", contract.getNotes());
                        
                        return ResponseEntity.ok(contractDetails);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{contractId}/download")
    @Operation(summary = "Tải hợp đồng PDF", description = "Khách vãng lai có thể tải hợp đồng dưới dạng PDF")
    public ResponseEntity<?> downloadContract(@PathVariable UUID contractId) {
        try {
            return salesContractService.getContractById(contractId)
                    .map(contract -> {
                        // In a real implementation, you would generate PDF here
                        Map<String, Object> downloadInfo = new HashMap<>();
                        downloadInfo.put("contractId", contractId);
                        downloadInfo.put("contractNumber", contract.getContractNumber());
                        downloadInfo.put("downloadUrl", "/api/public/contracts/" + contractId + "/pdf");
                        downloadInfo.put("message", "PDF generation initiated. Download will be available shortly.");
                        downloadInfo.put("expiresAt", LocalDateTime.now().plusHours(24));
                        
                        return ResponseEntity.ok(downloadInfo);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/{contractId}/sign")
    @Operation(summary = "Ký hợp đồng điện tử", description = "Khách vãng lai có thể ký hợp đồng điện tử")
    public ResponseEntity<?> signContract(
            @PathVariable UUID contractId,
            @RequestParam String customerSignature,
            @RequestParam(required = false) String signatureMethod) {
        try {
            var contract = salesContractService.getContractById(contractId);
            if (contract.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Contract not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            SalesContract contractEntity = contract.get();
            
            // Update contract with signature
            contractEntity.setContractStatus(SalesContractStatus.SIGNED);
            contractEntity.setSignedDate(LocalDateTime.now().toLocalDate());
            contractEntity.setNotes((contractEntity.getNotes() != null ? contractEntity.getNotes() + "\n" : "") + 
                                  "Customer signature: " + customerSignature + 
                                  (signatureMethod != null ? " (Method: " + signatureMethod + ")" : ""));
            
            salesContractService.updateContract(contractId, contractEntity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Contract signed successfully");
            response.put("contractId", contractId);
            response.put("status", "signed");
            response.put("signatureDate", LocalDateTime.now().toLocalDate());
            response.put("signatureMethod", signatureMethod != null ? signatureMethod : "electronic");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Contract signing failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{contractId}/status")
    @Operation(summary = "Trạng thái ký hợp đồng", description = "Khách vãng lai có thể xem trạng thái ký hợp đồng")
    public ResponseEntity<?> getContractSignatureStatus(@PathVariable UUID contractId) {
        try {
            return salesContractService.getContractById(contractId)
                    .map(contract -> {
                        Map<String, Object> status = new HashMap<>();
                        status.put("contractId", contractId);
                        status.put("contractNumber", contract.getContractNumber());
                        status.put("status", contract.getContractStatus() != null ? contract.getContractStatus().getValue() : null);
                        status.put("isSigned", contract.getContractStatus() == SalesContractStatus.SIGNED);
                        status.put("signatureDate", contract.getSignedDate());
                        status.put("signatureMethod", "electronic");
                        status.put("canSign", contract.getContractStatus() == SalesContractStatus.DRAFT || contract.getContractStatus() == SalesContractStatus.PENDING);
                        
                        return ResponseEntity.ok(status);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve contract status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/{contractId}/reject")
    @Operation(summary = "Từ chối hợp đồng", description = "Khách vãng lai có thể từ chối hợp đồng")
    public ResponseEntity<?> rejectContract(
            @PathVariable UUID contractId,
            @RequestParam String reason) {
        try {
            var contract = salesContractService.getContractById(contractId);
            if (contract.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Contract not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            SalesContract contractEntity = contract.get();
            contractEntity.setContractStatus(SalesContractStatus.CANCELLED);
            contractEntity.setNotes((contractEntity.getNotes() != null ? contractEntity.getNotes() + "\n" : "") + 
                                  "Rejection reason: " + reason);
            
            salesContractService.updateContract(contractId, contractEntity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Contract rejected successfully");
            response.put("contractId", contractId);
            response.put("status", "rejected");
            response.put("reason", reason);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Contract rejection failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/template")
    @Operation(summary = "Mẫu hợp đồng", description = "Khách vãng lai có thể xem mẫu hợp đồng")
    public ResponseEntity<?> getContractTemplate() {
        try {
            Map<String, Object> template = new HashMap<>();
            template.put("title", "HỢP ĐỒNG MUA BÁN XE ĐIỆN");
            template.put("sections", new String[]{
                "Thông tin các bên",
                "Thông tin xe",
                "Giá cả và thanh toán",
                "Điều khoản giao hàng",
                "Bảo hành và bảo trì",
                "Điều khoản chung"
            });
            template.put("requiredFields", new String[]{
                "customerName", "customerId", "vehicleInfo", "totalAmount", "paymentTerms"
            });
            template.put("signatureRequired", true);
            template.put("validityPeriod", "30 days");
            
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve contract template: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
