package com.evdealer.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum định nghĩa các trạng thái của báo giá đại lý
 * Lưu ý: Giá trị là lowercase (khác với các enum khác dùng uppercase)
 */
public enum DealerQuotationStatus {
    PENDING("pending", "Chờ xử lý"),
    SENT("sent", "Đã gửi"),
    ACCEPTED("accepted", "Đã chấp nhận"),
    REJECTED("rejected", "Đã từ chối"),
    EXPIRED("expired", "Hết hạn"),
    CONVERTED("converted", "Đã chuyển đổi thành Invoice");
    
    private final String value;
    private final String description;
    
    DealerQuotationStatus(String value, String description) {
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
     * Chuyển đổi string thành DealerQuotationStatus enum
     * @param statusString chuỗi trạng thái
     * @return DealerQuotationStatus enum hoặc PENDING nếu không tìm thấy
     */
    public static DealerQuotationStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return PENDING;
        }
        
        String normalized = statusString.trim().toLowerCase();
        
        for (DealerQuotationStatus status : values()) {
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
        
        for (DealerQuotationStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

