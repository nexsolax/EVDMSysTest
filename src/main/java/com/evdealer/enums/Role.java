package com.evdealer.enums;

/**
 * Enum định nghĩa các role trong hệ thống
 * Chuẩn hóa role mapping từ JWT → GrantedAuthority
 */
public enum Role {
    ADMIN("ADMIN", "ROLE_ADMIN"),
    EVM_STAFF("EVM_STAFF", "ROLE_EVM_STAFF"),
    DEALER_MANAGER("DEALER_MANAGER", "ROLE_DEALER_MANAGER"),
    DEALER_STAFF("DEALER_STAFF", "ROLE_DEALER_STAFF"),
    CUSTOMER("CUSTOMER", "ROLE_CUSTOMER");
    
    private final String value;
    private final String authority;
    
    Role(String value, String authority) {
        this.value = value;
        this.authority = authority;
    }
    
    /**
     * Trả về giá trị role (không có prefix ROLE_)
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Trả về authority name (có prefix ROLE_)
     */
    public String getAuthority() {
        return authority;
    }
    
    /**
     * Chuyển đổi từ string sang Role enum
     * Hỗ trợ cả role có và không có prefix ROLE_
     */
    public static Role fromString(String roleStr) {
        if (roleStr == null || roleStr.isEmpty()) {
            return null;
        }
        
        // Remove prefix ROLE_ nếu có
        String normalized = roleStr.toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        
        // Tìm enum matching
        for (Role role : Role.values()) {
            if (role.value.equals(normalized)) {
                return role;
            }
        }
        
        return null;
    }
    
    /**
     * Kiểm tra xem string có phải là role hợp lệ không
     */
    public static boolean isValid(String roleStr) {
        return fromString(roleStr) != null;
    }
    
    /**
     * Normalize role string (remove ROLE_ prefix, uppercase)
     */
    public static String normalize(String roleStr) {
        if (roleStr == null || roleStr.isEmpty()) {
            return null;
        }
        
        String normalized = roleStr.toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        
        return normalized;
    }
}

