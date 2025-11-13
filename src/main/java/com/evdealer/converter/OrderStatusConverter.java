package com.evdealer.converter;

import com.evdealer.enums.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter để lưu OrderStatus enum vào DB dưới dạng value (lowercase)
 * thay vì tên constant (UPPERCASE).
 * 
 * Lý do: Dữ liệu DB hiện tại lưu lowercase ("pending", "quoted", etc.),
 * và database constraint yêu cầu lowercase, nên cần converter để tương thích.
 */
@Converter(autoApply = false)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(OrderStatus status) {
        if (status == null) {
            return null;
        }
        // Lưu value (lowercase) thay vì tên constant (UPPERCASE)
        return status.getValue();
    }
    
    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return OrderStatus.PENDING; // Default
        }
        // Đọc value (lowercase) từ DB và convert sang enum
        return OrderStatus.fromString(dbData);
    }
}

