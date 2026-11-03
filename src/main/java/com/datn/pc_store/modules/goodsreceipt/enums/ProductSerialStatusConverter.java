package com.datn.pc_store.modules.goodsreceipt.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductSerialStatusConverter implements AttributeConverter<ProductSerialStatus, Byte> {
    @Override
    public Byte convertToDatabaseColumn(ProductSerialStatus attribute) {
        return attribute == null ? null : (byte) attribute.getValue();
    }

    @Override
    public ProductSerialStatus convertToEntityAttribute(Byte dbData) {
        return dbData == null ? null : ProductSerialStatus.fromValue(Byte.toUnsignedInt(dbData));
    }
}
