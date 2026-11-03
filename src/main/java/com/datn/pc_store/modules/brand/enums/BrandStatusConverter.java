package com.datn.pc_store.modules.brand.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BrandStatusConverter implements AttributeConverter<BrandStatus, Byte> {

    @Override
    public Byte convertToDatabaseColumn(BrandStatus attribute) {
        return attribute != null ? (byte) attribute.getValue() : null;
    }

    @Override
    public BrandStatus convertToEntityAttribute(Byte dbData) {
        return dbData != null ? BrandStatus.fromValue(Byte.toUnsignedInt(dbData)) : null;
    }
}
