package com.evdealer.enums;

/**
 * Enum định nghĩa trạng thái chính sách giá (Pricing Policy)
 * Dùng cho PricingPolicy.status
 */
public enum PricingPolicyStatus {
    ACTIVE("active", "Đang hoạt động"),
    INACTIVE("inactive", "Không hoạt động"),
    EXPIRED("expired", "Hết hạn");
    
    private final String value;
    private final String description;
    
    PricingPolicyStatus(String value, String description) {
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
     * Chuyển đổi string thành PricingPolicyStatus enum
     * @param statusString chuỗi trạng thái
     * @return PricingPolicyStatus enum hoặc ACTIVE nếu không tìm thấy
     */
    public static PricingPolicyStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return ACTIVE;
        }
        
        String normalized = statusString.trim().toLowerCase();
        
        for (PricingPolicyStatus status : values()) {
            if (status.value.equals(normalized)) {
                return status;
            }
        }
        
        return ACTIVE; // Default fallback
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
        
        for (PricingPolicyStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

