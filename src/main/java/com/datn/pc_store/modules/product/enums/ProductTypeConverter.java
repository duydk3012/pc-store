package com.datn.pc_store.modules.product.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductTypeConverter implements AttributeConverter<ProductType, Byte> {
    @Override
    public Byte convertToDatabaseColumn(ProductType attribute) {
        return attribute == null ? null : (byte) attribute.getValue();
    }

    @Override
    public ProductType convertToEntityAttribute(Byte value) {
        return value == null ? null : ProductType.fromValue(Byte.toUnsignedInt(value));
    }
}
