package com.evdealer.controller;

import com.evdealer.dto.SalesContractDTO;
import com.evdealer.entity.SalesContract;
import com.evdealer.service.SalesContractService;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales-contracts")
@CrossOrigin(origins = "*")
@Tag(name = "Sales Contract Management", description = "APIs quản lý hợp đồng bán hàng")
public class SalesContractController {
    
    @Autowired
    private SalesContractService salesContractService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy danh sách hợp đồng bán hàng", description = "Lấy tất cả hợp đồng bán hàng")
    public ResponseEntity<?> getAllContracts() {
        try {
            List<SalesContract> contracts = salesContractService.getAllContracts();
            if (contracts == null) {
                contracts = new java.util.ArrayList<>();
            }
            List<SalesContractDTO> contractList = contracts.stream()
                .map(contract -> {
                    try {
                        return toDTO(contract);
                    } catch (Exception e) {
                        // Return basic DTO if mapping fails
                        SalesContractDTO errorDTO = new SalesContractDTO();
                        try {
                            errorDTO.setContractId(contract.getContractId());
                        } catch (Exception e2) {
                            // Skip if contract is null
                        }
                        return errorDTO;
                    }
                })
                .toList();
            return ResponseEntity.ok(contractList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve contracts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{contractId}")
    public ResponseEntity<SalesContractDTO> getContractById(@PathVariable UUID contractId) {
        return salesContractService.getContractById(contractId)
                .map(contract -> ResponseEntity.ok(toDTO(contract)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/contract-number/{contractNumber}")
    public ResponseEntity<SalesContractDTO> getContractByNumber(@PathVariable String contractNumber) {
        return salesContractService.getContractByNumber(contractNumber)
                .map(contract -> ResponseEntity.ok(toDTO(contract)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<SalesContractDTO>> getContractsByOrder(@PathVariable UUID orderId) {
        List<SalesContract> contracts = salesContractService.getContractsByOrder(orderId);
        return ResponseEntity.ok(contracts.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SalesContractDTO>> getContractsByCustomer(@PathVariable UUID customerId) {
        List<SalesContract> contracts = salesContractService.getContractsByCustomer(customerId);
        return ResponseEntity.ok(contracts.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SalesContractDTO>> getContractsByUser(@PathVariable UUID userId) {
        List<SalesContract> contracts = salesContractService.getContractsByUser(userId);
        return ResponseEntity.ok(contracts.stream().map(this::toDTO).toList());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getContractsByStatus(@PathVariable String status) {
        try {
            // Validate và convert status string to enum
            com.evdealer.enums.SalesContractStatus statusEnum = com.evdealer.enums.SalesContractStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.SalesContractStatus.values())
                    .map(com.evdealer.enums.SalesContractStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<SalesContract> contracts = salesContractService.getContractsByStatus(statusEnum.getValue());
            return ResponseEntity.ok(contracts.stream().map(this::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get contracts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<SalesContractDTO>> getContractsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<SalesContract> contracts = salesContractService.getContractsByDateRange(startDate, endDate);
        return ResponseEntity.ok(contracts.stream().map(this::toDTO).toList());
    }
    
    @PostMapping
    @Operation(summary = "Tạo hợp đồng bán hàng mới", description = "Tạo hợp đồng bán hàng mới")
    public ResponseEntity<?> createContract(@RequestBody SalesContract contract) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo sales contract
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create sales contracts");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            SalesContract createdContract = salesContractService.createContract(contract);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdContract));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create sales contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create sales contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{contractId}")
    public ResponseEntity<?> updateContract(@PathVariable UUID contractId, @RequestBody SalesContract contractDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update sales contract
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update sales contracts");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            SalesContract updatedContract = salesContractService.updateContract(contractId, contractDetails);
            return ResponseEntity.ok(toDTO(updatedContract));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update sales contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update sales contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{contractId}/status")
    public ResponseEntity<?> updateContractStatus(@PathVariable UUID contractId, @RequestParam String status) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update contract status
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update sales contract status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validate và convert status string to enum
            com.evdealer.enums.SalesContractStatus statusEnum = com.evdealer.enums.SalesContractStatus.fromString(status);
            if (statusEnum == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + status);
                error.put("validStatuses", String.join(", ", java.util.Arrays.stream(com.evdealer.enums.SalesContractStatus.values())
                    .map(com.evdealer.enums.SalesContractStatus::getValue)
                    .collect(java.util.stream.Collectors.toList())));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            SalesContract updatedContract = salesContractService.updateContractStatus(contractId, statusEnum.getValue());
            return ResponseEntity.ok(toDTO(updatedContract));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update sales contract status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update sales contract status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{contractId}/sign")
    public ResponseEntity<?> signContract(@PathVariable UUID contractId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate signedDate) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Lấy contract hiện tại để kiểm tra ownership
            SalesContract existingContract = salesContractService.getContractById(contractId)
                .orElseThrow(() -> new RuntimeException("Sales contract not found"));
            
            // Kiểm tra phân quyền: ADMIN, EVM_STAFF hoặc user tạo contract
            if (!securityUtils.isAdmin() && !securityUtils.isEvmStaff()) {
                var currentUser = securityUtils.getCurrentUser()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
                UUID currentUserId = currentUser.getUserId();
                // User chỉ có thể sign contract của chính mình (nếu contract có user)
                if (existingContract.getUser() != null && !existingContract.getUser().getUserId().equals(currentUserId)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. You can only sign your own contracts");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            SalesContract updatedContract = salesContractService.signContract(contractId, signedDate);
            return ResponseEntity.ok(toDTO(updatedContract));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to sign sales contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to sign sales contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{contractId}")
    public ResponseEntity<?> deleteContract(@PathVariable UUID contractId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa sales contract
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete sales contracts");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            salesContractService.deleteContract(contractId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Sales contract deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete sales contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete sales contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    private SalesContractDTO toDTO(SalesContract c) {
        if (c == null) {
            return new SalesContractDTO();
        }
        SalesContractDTO dto = new SalesContractDTO();
        try {
            dto.setContractId(c.getContractId());
        } catch (Exception e) {
            // Skip if error
        }
        try {
            dto.setContractNumber(c.getContractNumber());
        } catch (Exception e) {
            // Skip if error
        }
        try {
            dto.setOrderId(c.getOrder() != null ? c.getOrder().getOrderId() : null);
        } catch (Exception e) {
            // Relationship not loaded, skip
        }
        try {
            dto.setCustomerId(c.getCustomer() != null ? c.getCustomer().getCustomerId() : null);
        } catch (Exception e) {
            // Relationship not loaded, skip
        }
        try {
            dto.setUserId(c.getUser() != null ? c.getUser().getUserId() : null);
        } catch (Exception e) {
            // Relationship not loaded, skip
        }
        try {
            dto.setContractDate(c.getContractDate());
        } catch (Exception e) {
            // Skip if error
        }
        try {
            dto.setDeliveryDate(c.getDeliveryDate());
        } catch (Exception e) {
            // Skip if error
        }
        try {
            dto.setContractValue(c.getContractValue());
        } catch (Exception e) {
            // Skip if error
        }
        try {
            dto.setContractStatus(c.getContractStatus() != null ? c.getContractStatus().getValue() : null);
        } catch (Exception e) {
            // Skip if error
        }
        return dto;
    }
}
