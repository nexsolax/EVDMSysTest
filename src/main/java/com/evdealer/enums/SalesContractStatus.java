package com.evdealer.enums;

/**
 * Enum định nghĩa trạng thái hợp đồng bán hàng (Sales Contract)
 * Dùng cho SalesContract.contractStatus
 * Lưu ý: Giá trị là lowercase (khác với ContractStatus dùng uppercase cho DealerContract)
 */
public enum SalesContractStatus {
    DRAFT("draft", "Hợp đồng nháp, chưa ký"),
    PENDING("pending", "Chờ ký"),
    SIGNED("signed", "Đã ký"),
    ACTIVE("active", "Đang có hiệu lực"),
    COMPLETED("completed", "Đã hoàn thành"),
    CANCELLED("cancelled", "Đã hủy");
    
    private final String value;
    private final String description;
    
    SalesContractStatus(String value, String description) {
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
     * Chuyển đổi string thành SalesContractStatus enum
     * @param statusString chuỗi trạng thái
     * @return SalesContractStatus enum hoặc DRAFT nếu không tìm thấy
     */
    public static SalesContractStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return DRAFT;
        }
        
        String normalized = statusString.trim().toLowerCase();
        
        for (SalesContractStatus status : values()) {
            if (status.value.equals(normalized)) {
                return status;
            }
        }
        
        return DRAFT; // Default fallback
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
        
        for (SalesContractStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

