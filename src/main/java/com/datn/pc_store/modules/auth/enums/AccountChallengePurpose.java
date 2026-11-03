package com.datn.pc_store.modules.auth.enums;

public enum AccountChallengePurpose {
    VERIFY_EMAIL(0),
    RESET_PASSWORD(1),
    CHANGE_PASSWORD(2);

    private final int value;

    AccountChallengePurpose(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AccountChallengePurpose fromValue(int value) {
        for (AccountChallengePurpose purpose : values()) {
            if (purpose.value == value) return purpose;
        }
        throw new IllegalArgumentException("Invalid purpose database code: " + value);
    }
}
