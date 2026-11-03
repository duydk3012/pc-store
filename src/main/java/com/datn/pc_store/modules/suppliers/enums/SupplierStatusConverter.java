package com.datn.pc_store.modules.suppliers.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SupplierStatusConverter implements AttributeConverter<SupplierStatus, Byte> {

    @Override
    public Byte convertToDatabaseColumn(SupplierStatus attribute) {
        return attribute != null ? (byte) attribute.getValue() : null;
    }

    @Override
    public SupplierStatus convertToEntityAttribute(Byte dbData) {
        return dbData != null ? SupplierStatus.fromValue(Byte.toUnsignedInt(dbData)) : null;
    }
}
