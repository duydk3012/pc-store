package com.datn.pc_store.modules.delivery.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DeliveryResourceTypeConverter implements AttributeConverter<DeliveryResourceType, Byte> {
    @Override
    public Byte convertToDatabaseColumn(DeliveryResourceType attribute) {
        return attribute == null ? null : (byte) attribute.getValue();
    }

    @Override
    public DeliveryResourceType convertToEntityAttribute(Byte dbData) {
        return dbData == null ? null : DeliveryResourceType.fromValue(Byte.toUnsignedInt(dbData));
    }
}
