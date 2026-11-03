package com.datn.pc_store.modules.user.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleTypeConverter implements AttributeConverter<RoleType, Byte> {
    @Override
    public Byte convertToDatabaseColumn(RoleType role) {
        return role == null ? null : (byte) role.getValue();
    }

    @Override
    public RoleType convertToEntityAttribute(Byte value) {
        return value == null ? null : RoleType.fromValue(Byte.toUnsignedInt(value));
    }
}
