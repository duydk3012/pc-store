package com.datn.pc_store.modules.warranty.enums;

import jakarta.persistence.EnumeratedValue;

public enum WarrantyPartStatus {
    PENDING(0),
    RESERVED(1),
    INSTALLED(2),
    RETURNED(3),
    CANCELLED(4);

    @EnumeratedValue
    private final byte code;

    WarrantyPartStatus(int code) {
        this.code = (byte) code;
    }
}
