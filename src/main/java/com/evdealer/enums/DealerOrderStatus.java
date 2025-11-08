package com.evdealer.enums;

/**
 * Enum định nghĩa các trạng thái của đơn hàng đại lý
 */
public enum DealerOrderStatus {
    PENDING("PENDING", "Đơn hàng mới, chờ duyệt"),
    APPROVED("APPROVED", "Đã được duyệt"),
    REJECTED("REJECTED", "Đã từ chối"),
    CONFIRMED("CONFIRMED", "Đã xác nhận"),
    WAITING_FOR_QUOTATION("WAITING_FOR_QUOTATION", "Chờ báo giá"),
    IN_PRODUCTION("IN_PRODUCTION", "Đang sản xuất"),
    READY_FOR_DELIVERY("READY_FOR_DELIVERY", "Sẵn sàng giao hàng"),
    DELIVERED("DELIVERED", "Đã giao hàng"),
    CANCELLED("CANCELLED", "Đã hủy");
    
    private final String value;
    private final String description;
    
    DealerOrderStatus(String value, String description) {
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
     * Chuyển đổi string thành DealerOrderStatus enum
     * @param statusString chuỗi trạng thái
     * @return DealerOrderStatus enum hoặc PENDING nếu không tìm thấy
     */
    public static DealerOrderStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return PENDING;
        }
        
        String normalized = statusString.trim().toUpperCase();
        
        for (DealerOrderStatus status : values()) {
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
        
        for (DealerOrderStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

