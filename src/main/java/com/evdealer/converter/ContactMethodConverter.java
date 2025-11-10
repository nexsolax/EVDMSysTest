package com.evdealer.converter;

import com.evdealer.enums.ContactMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ContactMethodConverter implements AttributeConverter<ContactMethod, String> {
    
    @Override
    public String convertToDatabaseColumn(ContactMethod contactMethod) {
        if (contactMethod == null) {
            return null;
        }
        return contactMethod.getValue();
    }
    
    @Override
    public ContactMethod convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return ContactMethod.EMAIL; // Default
        }
        return ContactMethod.fromString(dbData);
    }
}

