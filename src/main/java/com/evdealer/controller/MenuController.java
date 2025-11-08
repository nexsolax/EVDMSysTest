package com.evdealer.controller;

import com.evdealer.enums.Role;
import com.evdealer.util.RolePermissionManager;
import com.evdealer.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
@Tag(name = "Menu Management", description = "APIs để lấy menu items theo role của user")
public class MenuController {
    
    @Autowired
    private SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "Lấy menu items theo role", description = "Trả về danh sách menu items dựa trên role của user hiện tại")
    public ResponseEntity<?> getMenuItems() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Lấy role hiện tại
            Optional<Role> roleOpt = securityUtils.getCurrentUserRoleEnum();
            if (!roleOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unable to determine user role");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Role role = roleOpt.get();
            List<Map<String, Object>> menuItems = getMenuItemsByRole(role);
            
            Map<String, Object> response = new HashMap<>();
            response.put("role", role.getValue());
            response.put("menuItems", menuItems);
            response.put("permissions", getPermissionsByRole(role));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get menu items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/permissions")
    @Operation(summary = "Lấy permissions theo role", description = "Trả về danh sách permissions dựa trên role của user hiện tại")
    public ResponseEntity<?> getPermissions() {
        try {
            // Kiểm tra authentication
            if (!securityUtils.getCurrentUser().isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Lấy role hiện tại
            Optional<Role> roleOpt = securityUtils.getCurrentUserRoleEnum();
            if (!roleOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unable to determine user role");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Role role = roleOpt.get();
            Map<String, List<String>> permissions = getPermissionsByRole(role);
            
            Map<String, Object> response = new HashMap<>();
            response.put("role", role.getValue());
            response.put("permissions", permissions);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get permissions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Lấy menu items theo role
     */
    private List<Map<String, Object>> getMenuItemsByRole(Role role) {
        List<Map<String, Object>> menuItems = new ArrayList<>();
        
        switch (role) {
            case ADMIN:
                menuItems = getAdminMenuItems();
                break;
            case EVM_STAFF:
                menuItems = getEVMStaffMenuItems();
                break;
            case DEALER_MANAGER:
                menuItems = getDealerManagerMenuItems();
                break;
            case DEALER_STAFF:
                menuItems = getDealerStaffMenuItems();
                break;
            case CUSTOMER:
                menuItems = getCustomerMenuItems();
                break;
            default:
                menuItems = getDefaultMenuItems();
        }
        
        return menuItems;
    }
    
    /**
     * Lấy permissions theo role
     */
    private Map<String, List<String>> getPermissionsByRole(Role role) {
        String permissionsJson = RolePermissionManager.createPermissionsForRole(role.getValue());
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.type.MapType mapType = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance()
                .constructMapType(HashMap.class, String.class, List.class);
            return mapper.readValue(permissionsJson, mapType);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    
    /**
     * Menu items cho ADMIN
     */
    private List<Map<String, Object>> getAdminMenuItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        
        // Dashboard
        items.add(createMenuItem("dashboard", "Dashboard", "/dashboard", "dashboard", null));
        
        // Quản lý người dùng
        items.add(createMenuItem("users", "Quản lý người dùng", "/users", "users", Arrays.asList(
            createSubMenuItem("list", "Danh sách", "/users"),
            createSubMenuItem("create", "Tạo mới", "/users/create")
        )));
        
        // Quản lý đại lý
        items.add(createMenuItem("dealers", "Quản lý đại lý", "/dealers", "dealers", Arrays.asList(
            createSubMenuItem("list", "Danh sách", "/dealers"),
            createSubMenuItem("create", "Tạo mới", "/dealers/create"),
            createSubMenuItem("targets", "Mục tiêu", "/dealer-targets")
        )));
        
        // Quản lý khách hàng
        items.add(createMenuItem("customers", "Quản lý khách hàng", "/customers", "customers", Arrays.asList(
            createSubMenuItem("list", "Danh sách", "/customers"),
            createSubMenuItem("create", "Tạo mới", "/customers/create")
        )));
        
        // Quản lý xe
        items.add(createMenuItem("vehicles", "Quản lý xe", "/vehicles", "vehicles", Arrays.asList(
            createSubMenuItem("brands", "Thương hiệu", "/vehicles/brands"),
            createSubMenuItem("models", "Dòng xe", "/vehicles/models"),
            createSubMenuItem("variants", "Phiên bản", "/vehicles/variants"),
            createSubMenuItem("inventory", "Kho hàng", "/inventory")
        )));
        
        // Quản lý đơn hàng
        items.add(createMenuItem("orders", "Quản lý đơn hàng", "/orders", "orders", Arrays.asList(
            createSubMenuItem("list", "Danh sách", "/orders"),
            createSubMenuItem("dealer-orders", "Đơn hàng đại lý", "/dealer-orders")
        )));
        
        // Báo giá
        items.add(createMenuItem("quotations", "Báo giá", "/quotations", "quotations", Arrays.asList(
            createSubMenuItem("list", "Danh sách", "/quotations"),
            createSubMenuItem("dealer-quotations", "Báo giá đại lý", "/dealer-quotations")
        )));
        
        // Thanh toán
        items.add(createMenuItem("payments", "Thanh toán", "/payments", "payments", Arrays.asList(
            createSubMenuItem("customer-payments", "Thanh toán khách hàng", "/customer-payments"),
            createSubMenuItem("dealer-payments", "Thanh toán đại lý", "/dealer-payments")
        )));
        
        // Giao hàng
        items.add(createMenuItem("deliveries", "Giao hàng", "/vehicle-deliveries", "vehicle-deliveries", null));
        
        // Hợp đồng bán hàng
        items.add(createMenuItem("sales-contracts", "Hợp đồng bán hàng", "/sales-contracts", "sales-contracts", null));
        
        // Lịch hẹn
        items.add(createMenuItem("appointments", "Lịch hẹn", "/appointments", "appointments", null));
        
        // Phản hồi
        items.add(createMenuItem("feedbacks", "Phản hồi", "/feedbacks", "feedbacks", null));
        
        // Trả góp
        items.add(createMenuItem("installments", "Trả góp", "/installments", "installments", Arrays.asList(
            createSubMenuItem("plans", "Kế hoạch", "/installment-plans"),
            createSubMenuItem("schedules", "Lịch trả", "/installment-schedules")
        )));
        
        // Báo cáo
        items.add(createMenuItem("reports", "Báo cáo", "/reports", "reports", Arrays.asList(
            createSubMenuItem("sales-by-staff", "Bán hàng theo nhân viên", "/reports/sales-by-staff"),
            createSubMenuItem("inventory-turnover", "Vòng quay kho", "/reports/inventory-turnover"),
            createSubMenuItem("customer-debt", "Công nợ khách hàng", "/reports/customer-debt"),
            createSubMenuItem("dealer-performance", "Hiệu suất đại lý", "/reports/dealer-performance"),
            createSubMenuItem("monthly-sales", "Bán hàng theo tháng", "/reports/monthly-sales"),
            createSubMenuItem("deliveries", "Giao hàng", "/reports/deliveries")
        )));
        
        // Cài đặt
        items.add(createMenuItem("settings", "Cài đặt", "/settings", "settings", Arrays.asList(
            createSubMenuItem("pricing-policies", "Chính sách giá", "/pricing-policies"),
            createSubMenuItem("warehouses", "Kho hàng", "/warehouses"),
            createSubMenuItem("promotions", "Khuyến mãi", "/promotions")
        )));
        
        return items;
    }
    
    /**
     * Menu items cho EVM_STAFF
     */
    private List<Map<String, Object>> getEVMStaffMenuItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        
        items.add(createMenuItem("dashboard", "Dashboard", "/dashboard", "dashboard", null));
        items.add(createMenuItem("vehicles", "Quản lý xe", "/vehicles", "vehicles", Arrays.asList(
            createSubMenuItem("brands", "Thương hiệu", "/vehicles/brands"),
            createSubMenuItem("models", "Dòng xe", "/vehicles/models"),
            createSubMenuItem("variants", "Phiên bản", "/vehicles/variants"),
            createSubMenuItem("inventory", "Kho hàng", "/inventory")
        )));
        items.add(createMenuItem("orders", "Đơn hàng", "/orders", "orders", null));
        items.add(createMenuItem("quotations", "Báo giá", "/quotations", "quotations", null));
        items.add(createMenuItem("deliveries", "Giao hàng", "/vehicle-deliveries", "vehicle-deliveries", null));
        items.add(createMenuItem("sales-contracts", "Hợp đồng bán hàng", "/sales-contracts", "sales-contracts", null));
        items.add(createMenuItem("appointments", "Lịch hẹn", "/appointments", "appointments", null));
        items.add(createMenuItem("feedbacks", "Phản hồi", "/feedbacks", "feedbacks", null));
        items.add(createMenuItem("reports", "Báo cáo", "/reports", "reports", null));
        
        return items;
    }
    
    /**
     * Menu items cho DEALER_MANAGER
     */
    private List<Map<String, Object>> getDealerManagerMenuItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        
        items.add(createMenuItem("dashboard", "Dashboard", "/dashboard", "dashboard", null));
        items.add(createMenuItem("dealer-orders", "Đơn hàng", "/dealer-orders", "dealer-orders", Arrays.asList(
            createSubMenuItem("list", "Danh sách", "/dealer-orders"),
            createSubMenuItem("create", "Tạo mới", "/dealer-orders/create")
        )));
        items.add(createMenuItem("dealer-quotations", "Báo giá", "/dealer-quotations", "dealer-quotations", null));
        items.add(createMenuItem("dealer-invoices", "Hóa đơn", "/dealer-invoices", "dealer-invoices", null));
        items.add(createMenuItem("dealer-payments", "Thanh toán", "/dealer-payments", "dealer-payments", null));
        items.add(createMenuItem("dealer-contracts", "Hợp đồng đại lý", "/dealer-contracts", "dealer-contracts", null));
        items.add(createMenuItem("deliveries", "Giao hàng", "/vehicle-deliveries", "vehicle-deliveries", null));
        items.add(createMenuItem("installments", "Trả góp", "/installments", "installments", Arrays.asList(
            createSubMenuItem("plans", "Kế hoạch", "/installment-plans"),
            createSubMenuItem("schedules", "Lịch trả", "/installment-schedules")
        )));
        items.add(createMenuItem("targets", "Mục tiêu", "/dealer-targets", "dealer-targets", null));
        items.add(createMenuItem("reports", "Báo cáo", "/reports", "reports", null));
        
        return items;
    }
    
    /**
     * Menu items cho DEALER_STAFF
     */
    private List<Map<String, Object>> getDealerStaffMenuItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        
        items.add(createMenuItem("dashboard", "Dashboard", "/dashboard", "dashboard", null));
        items.add(createMenuItem("customers", "Khách hàng", "/customers", "customers", null));
        items.add(createMenuItem("orders", "Đơn hàng", "/orders", "orders", null));
        items.add(createMenuItem("quotations", "Báo giá", "/quotations", "quotations", null));
        items.add(createMenuItem("vehicles", "Xe", "/vehicles", "vehicles", null));
        
        return items;
    }
    
    /**
     * Menu items cho CUSTOMER
     */
    private List<Map<String, Object>> getCustomerMenuItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        
        items.add(createMenuItem("catalog", "Danh mục xe", "/catalog", "vehicles", null));
        items.add(createMenuItem("my-orders", "Đơn hàng của tôi", "/my-orders", "orders", null));
        items.add(createMenuItem("my-quotations", "Báo giá của tôi", "/my-quotations", "quotations", null));
        items.add(createMenuItem("my-payments", "Thanh toán", "/my-payments", "payments", null));
        items.add(createMenuItem("appointments", "Lịch hẹn", "/appointments", "appointments", null));
        
        return items;
    }
    
    /**
     * Menu items mặc định
     */
    private List<Map<String, Object>> getDefaultMenuItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(createMenuItem("dashboard", "Dashboard", "/dashboard", "dashboard", null));
        return items;
    }
    
    /**
     * Tạo menu item
     */
    private Map<String, Object> createMenuItem(String key, String label, String path, String icon, List<Map<String, Object>> children) {
        Map<String, Object> item = new HashMap<>();
        item.put("key", key);
        item.put("label", label);
        item.put("path", path);
        item.put("icon", icon);
        if (children != null && !children.isEmpty()) {
            item.put("children", children);
        }
        return item;
    }
    
    /**
     * Tạo sub menu item
     */
    private Map<String, Object> createSubMenuItem(String key, String label, String path) {
        Map<String, Object> item = new HashMap<>();
        item.put("key", key);
        item.put("label", label);
        item.put("path", path);
        return item;
    }
}

