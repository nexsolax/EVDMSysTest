package com.evdealer.enums;

/**
 * Enum định nghĩa các trạng thái của đơn hàng (Order entity)
 * Lưu ý: Giá trị là lowercase (khác với DealerOrderStatus dùng uppercase)
 */
public enum OrderStatus {
    PENDING("pending", "Đơn hàng mới, chờ xử lý"),
    QUOTED("quoted", "Đã có báo giá"),
    CONFIRMED("confirmed", "Khách đã xác nhận"),
    PAID("paid", "Đã thanh toán"),
    DELIVERED("delivered", "Đã giao hàng"),
    COMPLETED("completed", "Hoàn tất"),
    REJECTED("rejected", "Đã từ chối"),
    CANCELLED("cancelled", "Đã hủy");
    
    private final String value;
    private final String description;
    
    OrderStatus(String value, String description) {
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
     * Chuyển đổi string thành OrderStatus enum
     * @param statusString chuỗi trạng thái
     * @return OrderStatus enum hoặc PENDING nếu không tìm thấy
     */
    public static OrderStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return PENDING;
        }
        
        String normalized = statusString.trim().toLowerCase();
        
        for (OrderStatus status : values()) {
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
        
        for (OrderStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

