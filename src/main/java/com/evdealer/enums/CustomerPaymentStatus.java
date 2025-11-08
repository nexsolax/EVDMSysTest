package com.evdealer.enums;

/**
 * Enum định nghĩa các trạng thái thanh toán của khách hàng
 * Lưu ý: Giá trị là lowercase
 */
public enum CustomerPaymentStatus {
    PENDING("pending", "Chờ xử lý"),
    COMPLETED("completed", "Hoàn tất"),
    FAILED("failed", "Thất bại"),
    REFUNDED("refunded", "Đã hoàn tiền");
    
    private final String value;
    private final String description;
    
    CustomerPaymentStatus(String value, String description) {
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
     * Chuyển đổi string thành CustomerPaymentStatus enum
     * @param statusString chuỗi trạng thái
     * @return CustomerPaymentStatus enum hoặc PENDING nếu không tìm thấy
     */
    public static CustomerPaymentStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return PENDING;
        }
        
        String normalized = statusString.trim().toLowerCase();
        
        for (CustomerPaymentStatus status : values()) {
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
        
        for (CustomerPaymentStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

