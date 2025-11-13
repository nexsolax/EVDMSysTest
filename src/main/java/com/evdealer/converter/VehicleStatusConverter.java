package com.evdealer.converter;

import com.evdealer.enums.VehicleStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter để lưu VehicleStatus enum vào DB dưới dạng value (lowercase)
 * và đọc từ DB có thể là lowercase hoặc uppercase.
 * 
 * Lý do: Dữ liệu DB hiện tại lưu lowercase ("reserved", "available", etc.),
 * nhưng JPA @Enumerated(EnumType.STRING) map theo enum NAME (UPPERCASE),
 * nên cần converter để tương thích.
 */
@Converter(autoApply = false)
public class VehicleStatusConverter implements AttributeConverter<VehicleStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(VehicleStatus status) {
        if (status == null) {
            return null;
        }
        // Lưu value (lowercase) thay vì tên constant (UPPERCASE)
        return status.getValue();
    }
    
    @Override
    public VehicleStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return VehicleStatus.AVAILABLE; // Default
        }
        // Đọc value (lowercase) từ DB và convert sang enum
        return VehicleStatus.fromString(dbData);
    }
}

