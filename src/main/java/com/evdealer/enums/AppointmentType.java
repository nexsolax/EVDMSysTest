package com.evdealer.enums;

/**
 * Enum định nghĩa các loại cuộc hẹn
 * Lưu ý: Giá trị là lowercase, snake_case
 */
public enum AppointmentType {
    CONSULTATION("consultation", "Tư vấn"),
    TEST_DRIVE("test_drive", "Lái thử"),
    DELIVERY("delivery", "Giao hàng"),
    SERVICE("service", "Dịch vụ"),
    MAINTENANCE("maintenance", "Bảo trì");
    
    private final String value;
    private final String description;
    
    AppointmentType(String value, String description) {
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
     * Chuyển đổi string thành AppointmentType enum
     * @param typeString chuỗi loại cuộc hẹn
     * @return AppointmentType enum hoặc CONSULTATION nếu không tìm thấy
     */
    public static AppointmentType fromString(String typeString) {
        if (typeString == null || typeString.trim().isEmpty()) {
            return CONSULTATION;
        }
        
        String normalized = typeString.trim().toLowerCase();
        
        for (AppointmentType type : values()) {
            if (type.value.equals(normalized)) {
                return type;
            }
        }
        
        return CONSULTATION; // Default fallback
    }
    
    /**
     * Kiểm tra xem loại cuộc hẹn có hợp lệ không
     * @param typeString chuỗi loại cuộc hẹn
     * @return true nếu hợp lệ
     */
    public static boolean isValid(String typeString) {
        if (typeString == null || typeString.trim().isEmpty()) {
            return false;
        }
        
        String normalized = typeString.trim().toLowerCase();
        
        for (AppointmentType type : values()) {
            if (type.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

