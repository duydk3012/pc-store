package com.datn.pc_store.modules.category.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CategoryStatusConverter implements AttributeConverter<CategoryStatus, Byte> {

    @Override
    public Byte convertToDatabaseColumn(CategoryStatus attribute) {
        return attribute != null ? (byte) attribute.getValue() : null;
    }

    @Override
    public CategoryStatus convertToEntityAttribute(Byte dbData) {
        return dbData != null ? CategoryStatus.fromValue(Byte.toUnsignedInt(dbData)) : null;
    }
}
