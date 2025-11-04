package com.evdealer.controller;

import com.evdealer.entity.DealerContract;
import com.evdealer.service.DealerContractService;
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
@RequestMapping("/api/dealer-contracts")
@CrossOrigin(origins = "*")
@Tag(name = "Dealer Contract Management", description = "APIs quản lý hợp đồng đại lý")
public class DealerContractController {
    
    @Autowired
    private DealerContractService dealerContractService;
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy danh sách hợp đồng", description = "Lấy tất cả hợp đồng đại lý")
    public ResponseEntity<?> getAllContracts() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<DealerContract> contracts = dealerContractService.getAllContracts();
            
            // Filter theo dealer nếu là dealer user
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    contracts = contracts.stream()
                        .filter(contract -> contract.getDealer() != null && contract.getDealer().getDealerId().equals(userDealerId))
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get contracts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{contractId}")
    @Operation(summary = "Lấy hợp đồng theo ID", description = "Lấy thông tin hợp đồng theo ID")
    public ResponseEntity<?> getContractById(@PathVariable UUID contractId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            DealerContract contract = dealerContractService.getContractById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
            
            // Dealer user chỉ có thể xem contract của dealer mình
            if (securityUtils.isDealerUser() && !securityUtils.isAdmin()) {
                var currentUserOpt = securityUtils.getCurrentUser();
                if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                    UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                    if (contract.getDealer() != null && !contract.getDealer().getDealerId().equals(userDealerId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. You can only view contracts for your own dealer");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            return ResponseEntity.ok(contract);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/contract-number/{contractNumber}")
    @Operation(summary = "Lấy hợp đồng theo số", description = "Lấy thông tin hợp đồng theo số hợp đồng")
    public ResponseEntity<DealerContract> getContractByNumber(@PathVariable String contractNumber) {
        return dealerContractService.getContractByNumber(contractNumber)
                .map(contract -> ResponseEntity.ok(contract))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/type/{contractType}")
    @Operation(summary = "Lấy hợp đồng theo loại", description = "Lấy danh sách hợp đồng theo loại")
    public ResponseEntity<List<DealerContract>> getContractsByType(@PathVariable String contractType) {
        List<DealerContract> contracts = dealerContractService.getContractsByType(contractType);
        return ResponseEntity.ok(contracts);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Lấy hợp đồng theo trạng thái", description = "Lấy danh sách hợp đồng theo trạng thái")
    public ResponseEntity<List<DealerContract>> getContractsByStatus(@PathVariable String status) {
        List<DealerContract> contracts = dealerContractService.getContractsByStatus(status);
        return ResponseEntity.ok(contracts);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Lấy hợp đồng đang hoạt động", description = "Lấy danh sách hợp đồng đang hoạt động")
    public ResponseEntity<List<DealerContract>> getActiveContracts() {
        List<DealerContract> contracts = dealerContractService.getActiveContracts();
        return ResponseEntity.ok(contracts);
    }
    
    @GetMapping("/active/date/{date}")
    @Operation(summary = "Lấy hợp đồng hoạt động theo ngày", description = "Lấy hợp đồng hoạt động theo ngày")
    public ResponseEntity<List<DealerContract>> getActiveContractsByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DealerContract> contracts = dealerContractService.getActiveContractsByDate(date);
        return ResponseEntity.ok(contracts);
    }
    
    @GetMapping("/type/{contractType}/status/{status}")
    @Operation(summary = "Lấy hợp đồng theo loại và trạng thái", description = "Lấy hợp đồng theo loại và trạng thái")
    public ResponseEntity<List<DealerContract>> getContractsByTypeAndStatus(@PathVariable String contractType, @PathVariable String status) {
        List<DealerContract> contracts = dealerContractService.getContractsByTypeAndStatus(contractType, status);
        return ResponseEntity.ok(contracts);
    }
    
    @GetMapping("/territory/{territory}")
    @Operation(summary = "Lấy hợp đồng theo khu vực", description = "Lấy hợp đồng theo khu vực")
    public ResponseEntity<List<DealerContract>> getContractsByTerritory(@PathVariable String territory) {
        List<DealerContract> contracts = dealerContractService.getContractsByTerritory(territory);
        return ResponseEntity.ok(contracts);
    }
    
    @PostMapping
    @Operation(summary = "Tạo hợp đồng mới", description = "Tạo hợp đồng đại lý mới")
    public ResponseEntity<?> createContract(@RequestBody DealerContract contract) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể tạo contract
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can create contracts");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerContract createdContract = dealerContractService.createContract(contract);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdContract);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{contractId}")
    @Operation(summary = "Cập nhật hợp đồng", description = "Cập nhật thông tin hợp đồng")
    public ResponseEntity<?> updateContract(@PathVariable UUID contractId, @RequestBody DealerContract contractDetails) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN hoặc EVM_STAFF mới có thể update contract
            if (!securityUtils.hasAnyRole("ADMIN", "EVM_STAFF")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin or EVM staff can update contracts");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerContract updatedContract = dealerContractService.updateContract(contractId, contractDetails);
            return ResponseEntity.ok(updatedContract);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{contractId}/status")
    @Operation(summary = "Cập nhật trạng thái hợp đồng", description = "Cập nhật trạng thái hợp đồng")
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
                error.put("error", "Access denied. Only admin or EVM staff can update contract status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            DealerContract updatedContract = dealerContractService.updateContractStatus(contractId, status);
            return ResponseEntity.ok(updatedContract);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update contract status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update contract status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{contractId}/sign")
    @Operation(summary = "Ký hợp đồng", description = "Ký hợp đồng")
    public ResponseEntity<?> signContract(@PathVariable UUID contractId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate signedDate) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Lấy contract hiện tại để kiểm tra ownership
            DealerContract existingContract = dealerContractService.getContractById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
            
            // Kiểm tra phân quyền: ADMIN, EVM_STAFF hoặc dealer user của chính dealer đó
            if (!securityUtils.isAdmin() && !securityUtils.isEvmStaff()) {
                // Kiểm tra dealer user chỉ có thể sign contract của dealer mình
                if (securityUtils.isDealerUser()) {
                    var currentUserOpt = securityUtils.getCurrentUser();
                    if (currentUserOpt.isPresent() && currentUserOpt.get().getDealer() != null) {
                        UUID userDealerId = currentUserOpt.get().getDealer().getDealerId();
                        if (existingContract.getDealer() != null && !existingContract.getDealer().getDealerId().equals(userDealerId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("error", "Access denied. You can only sign contracts for your own dealer");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                        }
                    } else {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Access denied. Only admin, EVM staff or dealer users can sign contracts");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Access denied. Only admin, EVM staff or dealer users can sign contracts");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            DealerContract updatedContract = dealerContractService.signContract(contractId, signedDate);
            return ResponseEntity.ok(updatedContract);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to sign contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to sign contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/{contractId}")
    @Operation(summary = "Xóa hợp đồng", description = "Xóa hợp đồng")
    public ResponseEntity<?> deleteContract(@PathVariable UUID contractId) {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Chỉ ADMIN mới có thể xóa contract
            if (!securityUtils.isAdmin()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Only admin can delete contracts");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            dealerContractService.deleteContract(contractId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Contract deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
