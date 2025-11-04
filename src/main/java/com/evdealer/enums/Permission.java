package com.evdealer.enums;

/**
 * Enum định nghĩa các quyền hạn trong hệ thống
 */
public enum Permission {
    
    // User Management
    USER_READ("users", "read", "Xem danh sách người dùng"),
    USER_WRITE("users", "write", "Tạo/sửa người dùng"),
    USER_DELETE("users", "delete", "Xóa người dùng"),
    
    // Dealer Management
    DEALER_READ("dealers", "read", "Xem danh sách đại lý"),
    DEALER_WRITE("dealers", "write", "Tạo/sửa đại lý"),
    DEALER_DELETE("dealers", "delete", "Xóa đại lý"),
    
    // Customer Management
    CUSTOMER_READ("customers", "read", "Xem danh sách khách hàng"),
    CUSTOMER_WRITE("customers", "write", "Tạo/sửa khách hàng"),
    CUSTOMER_DELETE("customers", "delete", "Xóa khách hàng"),
    
    // Vehicle Management
    VEHICLE_READ("vehicles", "read", "Xem danh sách xe"),
    VEHICLE_WRITE("vehicles", "write", "Tạo/sửa xe"),
    VEHICLE_DELETE("vehicles", "delete", "Xóa xe"),
    
    // Order Management
    ORDER_READ("orders", "read", "Xem danh sách đơn hàng"),
    ORDER_WRITE("orders", "write", "Tạo/sửa đơn hàng"),
    ORDER_DELETE("orders", "delete", "Xóa đơn hàng"),
    
    // Quotation Management
    QUOTATION_READ("quotations", "read", "Xem danh sách báo giá"),
    QUOTATION_WRITE("quotations", "write", "Tạo/sửa báo giá"),
    QUOTATION_DELETE("quotations", "delete", "Xóa báo giá"),
    
    // Report Management
    REPORT_READ("reports", "read", "Xem báo cáo"),
    REPORT_EXPORT("reports", "export", "Xuất báo cáo"),
    
    // Settings Management
    SETTINGS_READ("settings", "read", "Xem cài đặt"),
    SETTINGS_WRITE("settings", "write", "Sửa cài đặt");
    
    private final String module;
    private final String action;
    private final String description;
    
    Permission(String module, String action, String description) {
        this.module = module;
        this.action = action;
        this.description = description;
    }
    
    public String getModule() {
        return module;
    }
    
    public String getAction() {
        return action;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getPermissionKey() {
        return module + ":" + action;
    }
    
    @Override
    public String toString() {
        return getPermissionKey();
    }
}
