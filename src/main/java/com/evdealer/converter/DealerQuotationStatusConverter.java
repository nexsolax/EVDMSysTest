package com.evdealer.converter;

import com.evdealer.enums.DealerQuotationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter để lưu DealerQuotationStatus enum vào DB dưới dạng value (lowercase)
 * thay vì tên constant (UPPERCASE).
 * 
 * Lý do: Dữ liệu DB hiện tại lưu lowercase ("pending", "sent", etc.),
 * nên cần converter để tương thích.
 */
@Converter(autoApply = false)
public class DealerQuotationStatusConverter implements AttributeConverter<DealerQuotationStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(DealerQuotationStatus status) {
        if (status == null) {
            return null;
        }
        // Lưu value (lowercase) thay vì tên constant (UPPERCASE)
        return status.getValue();
    }
    
    @Override
    public DealerQuotationStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return DealerQuotationStatus.PENDING; // Default
        }
        // Đọc value (lowercase) từ DB và convert sang enum
        return DealerQuotationStatus.fromString(dbData);
    }
}

