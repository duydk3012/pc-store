package com.datn.pc_store.modules.warranty.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class WarrantyPolicyTargetTypeConverter implements AttributeConverter<WarrantyPolicyTargetType, Byte> {

    @Override
    public Byte convertToDatabaseColumn(WarrantyPolicyTargetType attribute) {
        return attribute != null ? (byte) attribute.getValue() : null;
    }

    @Override
    public WarrantyPolicyTargetType convertToEntityAttribute(Byte dbData) {
        return dbData != null ? WarrantyPolicyTargetType.fromValue(Byte.toUnsignedInt(dbData)) : null;
    }
}
