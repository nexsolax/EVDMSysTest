package com.evdealer.enums;

/**
 * Enum định nghĩa các trạng thái cuộc hẹn
 * Lưu ý: Giá trị là lowercase
 */
public enum AppointmentStatus {
    SCHEDULED("scheduled", "Đã lên lịch"),
    CONFIRMED("confirmed", "Đã xác nhận"),
    COMPLETED("completed", "Hoàn tất"),
    CANCELLED("cancelled", "Đã hủy");
    
    private final String value;
    private final String description;
    
    AppointmentStatus(String value, String description) {
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
     * Chuyển đổi string thành AppointmentStatus enum
     * @param statusString chuỗi trạng thái
     * @return AppointmentStatus enum hoặc SCHEDULED nếu không tìm thấy
     */
    public static AppointmentStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return SCHEDULED;
        }
        
        String normalized = statusString.trim().toLowerCase();
        
        for (AppointmentStatus status : values()) {
            if (status.value.equals(normalized)) {
                return status;
            }
        }
        
        return SCHEDULED; // Default fallback
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
        
        for (AppointmentStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
}

