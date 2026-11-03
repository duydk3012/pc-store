package com.datn.pc_store.modules.product.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductStatusConverter implements AttributeConverter<ProductStatus, Byte> {
    @Override
    public Byte convertToDatabaseColumn(ProductStatus attribute) {
        return attribute == null ? null : (byte) attribute.getValue();
    }

    @Override
    public ProductStatus convertToEntityAttribute(Byte value) {
        return value == null ? null : ProductStatus.fromValue(Byte.toUnsignedInt(value));
    }
}
