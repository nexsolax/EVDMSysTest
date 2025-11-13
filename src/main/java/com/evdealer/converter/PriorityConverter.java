package com.evdealer.converter;

import com.evdealer.enums.Priority;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter để lưu Priority enum vào DB dưới dạng value (UPPERCASE)
 * và đọc từ DB có thể là lowercase hoặc uppercase.
 * 
 * Lý do: Dữ liệu DB hiện tại có thể lưu "normal" (lowercase) hoặc "NORMAL" (uppercase),
 * nên cần converter để tương thích với cả hai format.
 */
@Converter(autoApply = false)
public class PriorityConverter implements AttributeConverter<Priority, String> {
    
    @Override
    public String convertToDatabaseColumn(Priority priority) {
        if (priority == null) {
            return null;
        }
        // Lưu value (UPPERCASE) thay vì tên constant
        return priority.getValue();
    }
    
    @Override
    public Priority convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return Priority.NORMAL; // Default
        }
        // Đọc từ DB (có thể là lowercase hoặc uppercase) và convert sang enum
        return Priority.fromString(dbData);
    }
}

