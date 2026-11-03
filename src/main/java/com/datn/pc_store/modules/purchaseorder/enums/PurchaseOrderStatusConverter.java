package com.datn.pc_store.modules.purchaseorder.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PurchaseOrderStatusConverter implements AttributeConverter<PurchaseOrderStatus, Byte> {
    @Override
    public Byte convertToDatabaseColumn(PurchaseOrderStatus status) {
        return status == null ? null : (byte) status.getValue();
    }

    @Override
    public PurchaseOrderStatus convertToEntityAttribute(Byte value) {
        return value == null ? null : PurchaseOrderStatus.fromValue(Byte.toUnsignedInt(value));
    }
}
