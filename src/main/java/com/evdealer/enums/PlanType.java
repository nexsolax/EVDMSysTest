package com.evdealer.enums;

/**
 * Enum định nghĩa các loại kế hoạch trả góp
 */
public enum PlanType {
    DEALER("dealer", "Đại lý"),
    CUSTOMER("customer", "Khách hàng");
    
    private final String value;
    private final String description;
    
    PlanType(String value, String description) {
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
     * Chuyển đổi string thành PlanType enum
     * @param planTypeString chuỗi loại kế hoạch
     * @return PlanType enum hoặc CUSTOMER nếu không tìm thấy
     */
    public static PlanType fromString(String planTypeString) {
        if (planTypeString == null || planTypeString.trim().isEmpty()) {
            return CUSTOMER;
        }
        
        String normalized = planTypeString.toLowerCase().trim();
        
        for (PlanType planType : values()) {
            if (planType.value.equals(normalized)) {
                return planType;
            }
        }
        
        // Fallback cho các giá trị cũ
        switch (normalized) {
            case "dealer":
            case "DEALER":
                return DEALER;
            case "customer":
            case "CUSTOMER":
                return CUSTOMER;
            default:
                return CUSTOMER;
        }
    }
    
    /**
     * Kiểm tra xem loại kế hoạch có hợp lệ không
     * @param planTypeString chuỗi loại kế hoạch
     * @return true nếu hợp lệ
     */
    public static boolean isValid(String planTypeString) {
        if (planTypeString == null || planTypeString.trim().isEmpty()) {
            return false;
        }
        
        String normalized = planTypeString.toLowerCase().trim();
        
        for (PlanType planType : values()) {
            if (planType.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

