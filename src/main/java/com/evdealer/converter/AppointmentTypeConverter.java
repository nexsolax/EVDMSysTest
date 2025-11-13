package com.evdealer.converter;

import com.evdealer.enums.AppointmentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter để lưu AppointmentType enum vào DB dưới dạng value (lowercase, snake_case)
 * thay vì tên constant (UPPERCASE).
 * 
 * Lý do: Dữ liệu DB hiện tại lưu lowercase, snake_case ("test_drive", "delivery", etc.),
 * và database constraint yêu cầu lowercase, nên cần converter để tương thích.
 */
@Converter(autoApply = false)
public class AppointmentTypeConverter implements AttributeConverter<AppointmentType, String> {
    
    @Override
    public String convertToDatabaseColumn(AppointmentType type) {
        if (type == null) {
            return null;
        }
        // Lưu value (lowercase, snake_case) thay vì tên constant (UPPERCASE)
        return type.getValue();
    }
    
    @Override
    public AppointmentType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return AppointmentType.CONSULTATION; // Default
        }
        // Đọc value (lowercase, snake_case) từ DB và convert sang enum
        return AppointmentType.fromString(dbData);
    }
}

