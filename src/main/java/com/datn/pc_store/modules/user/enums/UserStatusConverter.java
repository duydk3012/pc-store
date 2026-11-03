package com.datn.pc_store.modules.user.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, Byte> {

    @Override
    public Byte convertToDatabaseColumn(UserStatus attribute) {
        return attribute != null ? (byte) attribute.getValue() : null;
    }

    @Override
    public UserStatus convertToEntityAttribute(Byte dbData) {
        return dbData != null ? UserStatus.fromValue(Byte.toUnsignedInt(dbData)) : null;
    }
}
