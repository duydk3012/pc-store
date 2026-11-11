package com.datn.pc_store.modules.delivery.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DeliveryStatusConverter implements AttributeConverter<DeliveryStatus, Byte> {
    @Override
    public Byte convertToDatabaseColumn(DeliveryStatus attribute) {
        return attribute == null ? null : (byte) attribute.getValue();
    }

    @Override
    public DeliveryStatus convertToEntityAttribute(Byte dbData) {
        return dbData == null ? null : DeliveryStatus.fromValue(Byte.toUnsignedInt(dbData));
    }
}
