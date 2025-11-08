package com.evdealer.enums;

/**
 * Enum định nghĩa tình trạng xe
 */
public enum VehicleCondition {
    NEW,
    USED,
    DEMO,
    DAMAGED;
    
    /**
     * Chuyển đổi string thành VehicleCondition enum
     * @param conditionString chuỗi tình trạng
     * @return VehicleCondition enum hoặc NEW nếu không tìm thấy
     */
    public static VehicleCondition fromString(String conditionString) {
        if (conditionString == null || conditionString.trim().isEmpty()) {
            return NEW;
        }
        
        String normalized = conditionString.trim().toUpperCase();
        
        for (VehicleCondition condition : values()) {
            if (condition.name().equals(normalized)) {
                return condition;
            }
        }
        
        return NEW; // Default fallback
    }
    
    /**
     * Kiểm tra xem tình trạng có hợp lệ không
     * @param conditionString chuỗi tình trạng
     * @return true nếu hợp lệ
     */
    public static boolean isValid(String conditionString) {
        if (conditionString == null || conditionString.trim().isEmpty()) {
            return false;
        }
        
        String normalized = conditionString.trim().toUpperCase();
        
        for (VehicleCondition condition : values()) {
            if (condition.name().equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}
