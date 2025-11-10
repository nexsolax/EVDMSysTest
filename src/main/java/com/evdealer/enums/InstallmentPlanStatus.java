package com.evdealer.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum định nghĩa trạng thái kế hoạch trả góp (Installment Plan)
 * Dùng cho DealerInstallmentPlan.planStatus và InstallmentPlan.planStatus
 */
public enum InstallmentPlanStatus {
    ACTIVE("active", "Đang hoạt động"),
    COMPLETED("completed", "Hoàn tất"),
    CANCELLED("cancelled", "Đã hủy"),
    OVERDUE("overdue", "Quá hạn");
    
    private final String value;
    private final String description;
    
    InstallmentPlanStatus(String value, String description) {
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
     * Chuyển đổi string thành InstallmentPlanStatus enum
     * @param statusString chuỗi trạng thái
     * @return InstallmentPlanStatus enum hoặc ACTIVE nếu không tìm thấy
     */
    public static InstallmentPlanStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return ACTIVE;
        }
        
        String normalized = statusString.trim().toLowerCase();
        
        for (InstallmentPlanStatus status : values()) {
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
        
        for (InstallmentPlanStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

