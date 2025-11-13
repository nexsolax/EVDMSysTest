package com.evdealer.converter;

import com.evdealer.enums.VehicleDeliveryStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter để lưu VehicleDeliveryStatus enum vào DB dưới dạng value (lowercase)
 * thay vì tên constant (UPPERCASE).
 * 
 * Lý do: Dữ liệu DB hiện tại lưu lowercase ("pending", "scheduled", "in_transit", "delivered", "cancelled"),
 * và database constraint yêu cầu lowercase, nên cần converter để tương thích.
 */
@Converter(autoApply = false)
public class VehicleDeliveryStatusConverter implements AttributeConverter<VehicleDeliveryStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(VehicleDeliveryStatus status) {
        if (status == null) {
            return null;
        }
        // Lưu value (lowercase) thay vì tên constant (UPPERCASE)
        return status.getValue();
    }
    
    @Override
    public VehicleDeliveryStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return VehicleDeliveryStatus.SCHEDULED; // Default
        }
        // Đọc value (lowercase) từ DB và convert sang enum
        return VehicleDeliveryStatus.fromString(dbData);
    }
}

