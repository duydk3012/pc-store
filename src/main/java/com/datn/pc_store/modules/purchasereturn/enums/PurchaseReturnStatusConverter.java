package com.datn.pc_store.modules.purchasereturn.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PurchaseReturnStatusConverter implements AttributeConverter<PurchaseReturnStatus, Byte> {
    @Override
    public Byte convertToDatabaseColumn(PurchaseReturnStatus attribute) {
        return attribute == null ? null : (byte) attribute.getValue();
    }

    @Override
    public PurchaseReturnStatus convertToEntityAttribute(Byte dbData) {
        return dbData == null ? null : PurchaseReturnStatus.fromValue(Byte.toUnsignedInt(dbData));
    }
}
