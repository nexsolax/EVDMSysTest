package com.evdealer.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum định nghĩa các trạng thái của xe trong kho
 */
public enum VehicleStatus {
    AVAILABLE("available", "Có sẵn"),
    RESERVED("reserved", "Đã đặt"),
    SOLD("sold", "Đã bán"),
    MAINTENANCE("maintenance", "Bảo trì"),
    DAMAGED("damaged", "Hư hỏng"),
    IN_TRANSIT("in_transit", "Đang vận chuyển"),
    PENDING_DELIVERY("pending_delivery", "Chờ giao hàng");
    
    private final String value;
    private final String description;
    
    VehicleStatus(String value, String description) {
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
     * Chuyển đổi string thành VehicleStatus enum
     * @param statusString chuỗi trạng thái
     * @return VehicleStatus enum hoặc AVAILABLE nếu không tìm thấy
     */
    public static VehicleStatus fromString(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return AVAILABLE;
        }
        
        String normalized = statusString.toLowerCase().trim();
        
        for (VehicleStatus status : values()) {
            if (status.value.equals(normalized)) {
                return status;
            }
        }
        
        // Fallback cho các giá trị cũ
        switch (normalized) {
            case "available":
            case "AVAILABLE":
                return AVAILABLE;
            case "reserved":
            case "RESERVED":
                return RESERVED;
            case "sold":
            case "SOLD":
                return SOLD;
            case "maintenance":
            case "MAINTENANCE":
                return MAINTENANCE;
            default:
                return AVAILABLE;
        }
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
        
        String normalized = statusString.toLowerCase().trim();
        
        for (VehicleStatus status : values()) {
            if (status.value.equals(normalized)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Lấy danh sách tất cả các trạng thái hợp lệ
     * @return mảng các giá trị trạng thái
     */
    public static String[] getAllValues() {
        VehicleStatus[] statuses = values();
        String[] values = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            values[i] = statuses[i].value;
        }
        return values;
    }
    
    /**
     * Lấy danh sách tất cả các trạng thái với mô tả
     * @return Map chứa value và description
     */
    public static java.util.Map<String, String> getAllWithDescriptions() {
        java.util.Map<String, String> result = new java.util.HashMap<>();
        for (VehicleStatus status : values()) {
            result.put(status.value, status.description);
        }
        return result;
    }
}
