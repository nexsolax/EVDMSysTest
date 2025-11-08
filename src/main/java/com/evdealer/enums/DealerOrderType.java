package com.evdealer.enums;

/**
 * Enum định nghĩa các loại đơn hàng đại lý
 * Lưu ý: Khác với OrderType enum (dùng cho Order entity)
 */
public enum DealerOrderType {
    PURCHASE("PURCHASE", "Mua hàng"),
    RESERVE("RESERVE", "Đặt giữ"),
    SAMPLE("SAMPLE", "Mẫu");
    
    private final String value;
    private final String description;
    
    DealerOrderType(String value, String description) {
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
     * Chuyển đổi string thành DealerOrderType enum
     * @param typeString chuỗi loại đơn hàng
     * @return DealerOrderType enum hoặc PURCHASE nếu không tìm thấy
     */
    public static DealerOrderType fromString(String typeString) {
        if (typeString == null || typeString.trim().isEmpty()) {
            return PURCHASE;
        }
        
        String normalized = typeString.trim().toUpperCase();
        
        for (DealerOrderType type : values()) {
            if (type.value.equals(normalized)) {
                return type;
            }
        }
        
        return PURCHASE; // Default fallback
    }
    
    /**
     * Kiểm tra xem loại đơn hàng có hợp lệ không
     * @param typeString chuỗi loại đơn hàng
     * @return true nếu hợp lệ
     */
    public static boolean isValid(String typeString) {
        if (typeString == null || typeString.trim().isEmpty()) {
            return false;
        }
        
        String normalized = typeString.trim().toUpperCase();
        
        for (DealerOrderType type : values()) {
            if (type.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

