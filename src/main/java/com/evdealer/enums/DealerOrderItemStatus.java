package com.evdealer.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum định nghĩa các trạng thái của item trong đơn hàng đại lý
 */
public enum DealerOrderItemStatus {
    PENDING("PENDING", "Chờ xử lý"),
    CONFIRMED("CONFIRMED", "Đã xác nhận"),
    CANCELLED("CANCELLED", "Đã hủy"),
    DELIVERED("DELIVERED", "Đã giao hàng");
    
    private final String value;
    private final String description;
    
    DealerOrderItemStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Chuyển đổi string thành DealerOrderItemStatus enum
     * @param statusString chuỗi trạng thái
     * @return DealerOrderItemStatus enum hoặc PENDING nếu không tìm thấy
     */
    public static DealerOrderItemStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return PENDING;
        }
        
        String normalized = statusString.trim().toUpperCase();
        
        for (DealerOrderItemStatus status : values()) {
            if (status.value.equals(normalized)) {
                return status;
            }
        }
        
        return PENDING; // Default fallback
    }
    
    /**
     * Kiểm tra xem trạng thái có hợp lệ không
     * @param statusString chuỗi trạng thái
     * @return true nếu hợp lệ
     */
    public static boolean isValid(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return false;
        }
        
        String normalized = statusString.trim().toUpperCase();
        
        for (DealerOrderItemStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

