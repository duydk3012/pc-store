package com.datn.pc_store.modules.auth.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AccountChallengePurposeConverter implements AttributeConverter<AccountChallengePurpose, Byte> {
    @Override
    public Byte convertToDatabaseColumn(AccountChallengePurpose purpose) {
        return purpose == null ? null : (byte) purpose.getValue();
    }

    @Override
    public AccountChallengePurpose convertToEntityAttribute(Byte value) {
        return value == null ? null : AccountChallengePurpose.fromValue(Byte.toUnsignedInt(value));
    }
}
