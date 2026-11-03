package com.datn.pc_store.modules.goodsreceipt.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GoodsReceiptStatusConverter implements AttributeConverter<GoodsReceiptStatus, Byte> {
    @Override
    public Byte convertToDatabaseColumn(GoodsReceiptStatus attribute) {
        return attribute == null ? null : (byte) attribute.getValue();
    }

    @Override
    public GoodsReceiptStatus convertToEntityAttribute(Byte dbData) {
        return dbData == null ? null : GoodsReceiptStatus.fromValue(Byte.toUnsignedInt(dbData));
    }
}
