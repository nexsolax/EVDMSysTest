package com.evdealer.converter;

import com.evdealer.enums.AppointmentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter để lưu AppointmentStatus enum vào DB dưới dạng value (lowercase)
 * thay vì tên constant (UPPERCASE).
 * 
 * Lý do: Dữ liệu DB hiện tại lưu lowercase ("scheduled", "confirmed", etc.),
 * và database constraint yêu cầu lowercase, nên cần converter để tương thích.
 */
@Converter(autoApply = false)
public class AppointmentStatusConverter implements AttributeConverter<AppointmentStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(AppointmentStatus status) {
        if (status == null) {
            return null;
        }
        // Lưu value (lowercase) thay vì tên constant (UPPERCASE)
        return status.getValue();
    }
    
    @Override
    public AppointmentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return AppointmentStatus.SCHEDULED; // Default
        }
        // Đọc value (lowercase) từ DB và convert sang enum
        return AppointmentStatus.fromString(dbData);
    }
}

