package com.datn.pc_store.modules.warranty.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class WarrantyPolicyStatusConverter implements AttributeConverter<WarrantyPolicyStatus, Byte> {

    @Override
    public Byte convertToDatabaseColumn(WarrantyPolicyStatus attribute) {
        return attribute != null ? (byte) attribute.getValue() : null;
    }

    @Override
    public WarrantyPolicyStatus convertToEntityAttribute(Byte dbData) {
        return dbData != null ? WarrantyPolicyStatus.fromValue(Byte.toUnsignedInt(dbData)) : null;
    }
}
