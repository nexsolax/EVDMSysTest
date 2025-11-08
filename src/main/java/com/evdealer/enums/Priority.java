package com.evdealer.enums;

/**
 * Enum định nghĩa mức độ ưu tiên
 */
public enum Priority {
    LOW("LOW", "Thấp"),
    NORMAL("NORMAL", "Bình thường"),
    HIGH("HIGH", "Cao"),
    URGENT("URGENT", "Khẩn cấp");
    
    private final String value;
    private final String description;
    
    Priority(String value, String description) {
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
     * Chuyển đổi string thành Priority enum
     * @param priorityString chuỗi mức độ ưu tiên
     * @return Priority enum hoặc NORMAL nếu không tìm thấy
     */
    public static Priority fromString(String priorityString) {
        if (priorityString == null || priorityString.trim().isEmpty()) {
            return NORMAL;
        }
        
        String normalized = priorityString.trim().toUpperCase();
        
        for (Priority priority : values()) {
            if (priority.value.equals(normalized)) {
                return priority;
            }
        }
        
        return NORMAL; // Default fallback
    }
    
    /**
     * Kiểm tra xem mức độ ưu tiên có hợp lệ không
     * @param priorityString chuỗi mức độ ưu tiên
     * @return true nếu hợp lệ
     */
    public static boolean isValid(String priorityString) {
        if (priorityString == null || priorityString.trim().isEmpty()) {
            return false;
        }
        
        String normalized = priorityString.trim().toUpperCase();
        
        for (Priority priority : values()) {
            if (priority.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

