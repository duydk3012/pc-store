package com.datn.pc_store.common.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persist flags as numeric 0/1 while keeping booleans in entities and APIs. */
@Converter(autoApply = true)
public class TinyIntBooleanConverter implements AttributeConverter<Boolean, Byte> {
    @Override
    public Byte convertToDatabaseColumn(Boolean value) {
        return value == null ? null : (byte) (value ? 1 : 0);
    }

    @Override
    public Boolean convertToEntityAttribute(Byte value) {
        if (value == null) return null;
        return switch (value) {
            case 0 -> false;
            case 1 -> true;
            default -> throw new IllegalArgumentException("Invalid boolean database code: " + Byte.toUnsignedInt(value));
        };
    }
}
