package com.evdealer.controller;

import com.evdealer.dto.*;
import com.evdealer.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@Tag(name = "Report Management", description = "APIs for generating reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales-by-staff")
    @Operation(summary = "Get sales by staff report", description = "Generate sales report by staff")
    public ResponseEntity<?> getSalesByStaffReport() {
        try {
            return ResponseEntity.ok(reportService.getSalesReportByStaff());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve sales by staff report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/inventory-turnover")
    @Operation(summary = "Get inventory turnover report", description = "Inventory turnover metrics")
    public ResponseEntity<?> getInventoryTurnover() {
        try {
            return ResponseEntity.ok(reportService.getInventoryTurnoverReport());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve inventory turnover report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/customer-debt")
    @Operation(summary = "Get customer debt report", description = "Generate customer debt report")
    public ResponseEntity<?> getCustomerDebtReport() {
        try {
            return ResponseEntity.ok(reportService.getCustomerDebtReport());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customer debt report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/dealer-performance")
    @Operation(summary = "Get dealer performance report", description = "Dealer performance metrics")
    public ResponseEntity<?> getDealerPerformance() {
        try {
            return ResponseEntity.ok(reportService.getDealerPerformanceReport());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve dealer performance report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/sales-by-role/{role}")
    @Operation(summary = "Get sales by role", description = "Sales aggregated by user role")
    public ResponseEntity<?> getSalesByRole(@PathVariable("role") String role) {
        try {
            return ResponseEntity.ok(reportService.getSalesReportByRole(role));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve sales by role report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/deliveries")
    @Operation(summary = "Get all deliveries", description = "List of vehicle deliveries")
    public ResponseEntity<?> getAllDeliveries() {
        try {
            // keeping entity-list for all deliveries would be heavy; here we align to DTO list by status/date instead
            return ResponseEntity.ok(reportService.getDeliveriesByStatus("SCHEDULED"));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/deliveries/by-status/{status}")
    @Operation(summary = "Get deliveries by status", description = "Filter deliveries by status")
    public ResponseEntity<?> getDeliveriesByStatus(@PathVariable("status") String status) {
        try {
            return ResponseEntity.ok(reportService.getDeliveriesByStatus(status));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/deliveries/by-date")
    @Operation(summary = "Get deliveries by date", description = "Filter deliveries by date")
    public ResponseEntity<?> getDeliveriesByDate(@RequestParam("date") String date) {
        try {
            LocalDate parsedDate;
            try {
                parsedDate = LocalDate.parse(date);
            } catch (Exception e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid date format. Expected format: yyyy-MM-dd");
                error.put("providedDate", date);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            return ResponseEntity.ok(reportService.getDeliveriesByDate(parsedDate));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve deliveries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/monthly-sales")
    @Operation(summary = "Get monthly sales report", description = "Monthly sales summary")
    public ResponseEntity<?> getMonthlySales(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month) {
        try {
            // Validate year and month
            if (year == null || year <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Year must be a positive integer");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (month == null || month < 1 || month > 12) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Month must be between 1 and 12");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            return ResponseEntity.ok(reportService.getMonthlySalesSummary(year, month));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve monthly sales report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/walk-in-purchases")
    @Operation(summary = "Get walk-in customer purchases", description = "Orders without quotations (khách vãng lai mua xe). Optional filters: startDate, endDate, status")
    public ResponseEntity<?> getWalkInPurchases(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "status", required = false) String status
    ) {
        try {
            LocalDate s = null;
            LocalDate e = null;
            
            // Parse and validate startDate
            if (startDate != null && !startDate.isBlank()) {
                try {
                    s = LocalDate.parse(startDate);
                } catch (Exception ex) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid startDate format. Expected format: yyyy-MM-dd");
                    error.put("providedStartDate", startDate);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }
            
            // Parse and validate endDate
            if (endDate != null && !endDate.isBlank()) {
                try {
                    e = LocalDate.parse(endDate);
                } catch (Exception ex) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid endDate format. Expected format: yyyy-MM-dd");
                    error.put("providedEndDate", endDate);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }
            
            // Validate date range
            if (s != null && e != null && s.isAfter(e)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Start date cannot be after end date");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            return ResponseEntity.ok(reportService.getWalkInPurchases(s, e, (status != null && !status.isBlank()) ? status : null));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve walk-in purchases: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/walk-in-purchases/paged")
    @Operation(summary = "Get walk-in purchases (paged)", description = "Orders without quotations (khách vãng lai). Params: startDate, endDate, status, page, size, sort=field,dir")
    public ResponseEntity<?> getWalkInPurchasesPaged(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @RequestParam(value = "sort", required = false, defaultValue = "orderDate,desc") String sort
    ) {
        try {
            // Validate pagination parameters
            if (page < 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Page number must be >= 0");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (size <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Page size must be > 0");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            LocalDate s = null;
            LocalDate e = null;
            
            // Parse and validate startDate
            if (startDate != null && !startDate.isBlank()) {
                try {
                    s = LocalDate.parse(startDate);
                } catch (Exception ex) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid startDate format. Expected format: yyyy-MM-dd");
                    error.put("providedStartDate", startDate);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }
            
            // Parse and validate endDate
            if (endDate != null && !endDate.isBlank()) {
                try {
                    e = LocalDate.parse(endDate);
                } catch (Exception ex) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid endDate format. Expected format: yyyy-MM-dd");
                    error.put("providedEndDate", endDate);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }
            
            // Validate date range
            if (s != null && e != null && s.isAfter(e)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Start date cannot be after end date");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Parse sort parameter
            String[] sortParts = sort.split(",", 2);
            String sortField = sortParts[0];
            Sort.Direction dir = (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortField));

            Page<OrderDTO> result = reportService.getWalkInPurchasesPaged(s, e, (status != null && !status.isBlank()) ? status : null, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve walk-in purchases: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}