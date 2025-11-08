package com.evdealer.enums;

/**
 * Enum định nghĩa các trạng thái của hóa đơn đại lý
 * Lưu ý: Giá trị là lowercase, snake_case
 */
public enum DealerInvoiceStatus {
    ISSUED("issued", "Đã phát hành"),
    PARTIALLY_PAID("partially_paid", "Đã thanh toán một phần"),
    PAID("paid", "Đã thanh toán đủ"),
    OVERDUE("overdue", "Quá hạn"),
    CANCELLED("cancelled", "Đã hủy");
    
    private final String value;
    private final String description;
    
    DealerInvoiceStatus(String value, String description) {
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
     * Chuyển đổi string thành DealerInvoiceStatus enum
     * @param statusString chuỗi trạng thái
     * @return DealerInvoiceStatus enum hoặc ISSUED nếu không tìm thấy
     */
    public static DealerInvoiceStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return ISSUED;
        }
        
        String normalized = statusString.trim().toLowerCase();
        
        for (DealerInvoiceStatus status : values()) {
            if (status.value.equals(normalized)) {
                return status;
            }
        }
        
        return ISSUED; // Default fallback
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
        
        for (DealerInvoiceStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

