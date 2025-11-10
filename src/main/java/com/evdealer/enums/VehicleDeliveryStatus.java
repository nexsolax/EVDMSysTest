package com.evdealer.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum định nghĩa các trạng thái giao hàng xe
 * Lưu ý: Giá trị là lowercase, snake_case
 * Khác với DeliveryStatus enum (dùng cho Order, uppercase)
 */
public enum VehicleDeliveryStatus {
    PENDING("pending", "Chờ xử lý"),
    SCHEDULED("scheduled", "Đã lên lịch"),
    IN_TRANSIT("in_transit", "Đang vận chuyển"),
    DELIVERED("delivered", "Đã giao hàng"),
    CANCELLED("cancelled", "Đã hủy");
    
    private final String value;
    private final String description;
    
    VehicleDeliveryStatus(String value, String description) {
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
     * Chuyển đổi string thành VehicleDeliveryStatus enum
     * @param statusString chuỗi trạng thái
     * @return VehicleDeliveryStatus enum hoặc SCHEDULED nếu không tìm thấy
     */
    public static VehicleDeliveryStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return SCHEDULED;
        }
        
        String normalized = statusString.trim().toLowerCase();
        
        for (VehicleDeliveryStatus status : values()) {
            if (status.value.equals(normalized)) {
                return status;
            }
        }
        
        return SCHEDULED; // Default fallback
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
        
        String normalized = statusString.trim().toLowerCase();
        
        for (VehicleDeliveryStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

