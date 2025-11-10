package com.evdealer.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum định nghĩa các trạng thái thanh toán đại lý
 * Lưu ý: Giá trị là lowercase
 */
public enum DealerPaymentStatus {
    PENDING("pending", "Chờ xử lý"),
    COMPLETED("completed", "Hoàn tất"),
    FAILED("failed", "Thất bại"),
    REFUNDED("refunded", "Đã hoàn tiền");
    
    private final String value;
    private final String description;
    
    DealerPaymentStatus(String value, String description) {
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
     * Chuyển đổi string thành DealerPaymentStatus enum
     * @param statusString chuỗi trạng thái
     * @return DealerPaymentStatus enum hoặc PENDING nếu không tìm thấy
     */
    public static DealerPaymentStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return PENDING;
        }
        
        String normalized = statusString.trim().toLowerCase();
        
        for (DealerPaymentStatus status : values()) {
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
        
        String normalized = statusString.trim().toLowerCase();
        
        for (DealerPaymentStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

