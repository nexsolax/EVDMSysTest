package com.evdealer.enums;

/**
 * Enum định nghĩa các phương thức thanh toán
 * Dùng cho Order.paymentMethod và CustomerPayment.paymentMethod
 */
public enum PaymentMethod {
    BANK_TRANSFER("BANK_TRANSFER", "Chuyển khoản"),
    CASH("CASH", "Tiền mặt"),
    CREDIT_CARD("CREDIT_CARD", "Thẻ tín dụng"),
    CHEQUE("CHEQUE", "Séc");
    
    private final String value;
    private final String description;
    
    PaymentMethod(String value, String description) {
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
     * Chuyển đổi string thành PaymentMethod enum
     * @param methodString chuỗi phương thức thanh toán
     * @return PaymentMethod enum hoặc BANK_TRANSFER nếu không tìm thấy
     */
    public static PaymentMethod fromString(String methodString) {
        if (methodString == null || methodString.trim().isEmpty()) {
            return BANK_TRANSFER;
        }
        
        String normalized = methodString.trim().toUpperCase();
        
        for (PaymentMethod method : values()) {
            if (method.value.equals(normalized)) {
                return method;
            }
        }
        
        return BANK_TRANSFER; // Default fallback
    }
    
    /**
     * Kiểm tra xem phương thức thanh toán có hợp lệ không
     * @param methodString chuỗi phương thức thanh toán
     * @return true nếu hợp lệ
     */
    public static boolean isValid(String methodString) {
        if (methodString == null || methodString.trim().isEmpty()) {
            return false;
        }
        
        String normalized = methodString.trim().toUpperCase();
        
        for (PaymentMethod method : values()) {
            if (method.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

