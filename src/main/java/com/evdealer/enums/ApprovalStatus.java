package com.evdealer.enums;

/**
 * Enum định nghĩa trạng thái duyệt
 */
public enum ApprovalStatus {
    PENDING("PENDING", "Chờ duyệt"),
    APPROVED("APPROVED", "Đã duyệt"),
    REJECTED("REJECTED", "Đã từ chối");
    
    private final String value;
    private final String description;
    
    ApprovalStatus(String value, String description) {
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
     * Chuyển đổi string thành ApprovalStatus enum
     * @param statusString chuỗi trạng thái duyệt
     * @return ApprovalStatus enum hoặc PENDING nếu không tìm thấy
     */
    public static ApprovalStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return PENDING;
        }
        
        String normalized = statusString.trim().toUpperCase();
        
        for (ApprovalStatus status : values()) {
            if (status.value.equals(normalized)) {
                return status;
            }
        }
        
        return PENDING; // Default fallback
    }
    
    /**
     * Kiểm tra xem trạng thái duyệt có hợp lệ không
     * @param statusString chuỗi trạng thái duyệt
     * @return true nếu hợp lệ
     */
    public static boolean isValid(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return false;
        }
        
        String normalized = statusString.trim().toUpperCase();
        
        for (ApprovalStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

