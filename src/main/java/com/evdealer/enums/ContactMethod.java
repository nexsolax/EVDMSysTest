package com.evdealer.enums;

/**
 * Enum định nghĩa các phương thức liên hệ ưa thích
 */
public enum ContactMethod {
    EMAIL("email", "Email"),
    PHONE("phone", "Điện thoại"),
    SMS("sms", "SMS");
    
    private final String value;
    private final String description;
    
    ContactMethod(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Chuyển đổi string thành ContactMethod enum
     * @param methodString chuỗi phương thức liên hệ
     * @return ContactMethod enum hoặc EMAIL nếu không tìm thấy
     */
    public static ContactMethod fromString(String methodString) {
        if (methodString == null || methodString.trim().isEmpty()) {
            return EMAIL;
        }
        
        String normalized = methodString.trim().toLowerCase();
        
        for (ContactMethod method : values()) {
            if (method.value.equals(normalized)) {
                return method;
            }
        }
        
        return EMAIL; // Default fallback
    }
    
    /**
     * Kiểm tra xem phương thức liên hệ có hợp lệ không
     * @param methodString chuỗi phương thức liên hệ
     * @return true nếu hợp lệ
     */
    public static boolean isValid(String methodString) {
        if (methodString == null || methodString.trim().isEmpty()) {
            return false;
        }
        
        String normalized = methodString.trim().toLowerCase();
        
        for (ContactMethod method : values()) {
            if (method.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

