package com.evdealer.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum định nghĩa trạng thái thực hiện đơn hàng (Order fulfillment)
 * Dùng cho Order.fulfillmentStatus
 */
public enum FulfillmentStatus {
    PENDING("PENDING", "Chờ xử lý"),
    PROCESSING("PROCESSING", "Đang xử lý"),
    SHIPPED("SHIPPED", "Đã gửi hàng"),
    FULFILLED("FULFILLED", "Đã hoàn thành"),
    CANCELLED("CANCELLED", "Đã hủy"),
    FAILED("FAILED", "Thất bại");
    
    private final String value;
    private final String description;
    
    FulfillmentStatus(String value, String description) {
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
     * Chuyển đổi string thành FulfillmentStatus enum
     * @param statusString chuỗi trạng thái
     * @return FulfillmentStatus enum hoặc PENDING nếu không tìm thấy
     */
    public static FulfillmentStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return PENDING;
        }
        
        String normalized = statusString.trim().toUpperCase();
        
        for (FulfillmentStatus status : values()) {
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
        
        for (FulfillmentStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

