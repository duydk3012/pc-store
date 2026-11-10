package com.datn.pc_store.modules.warranty.enums;

import jakarta.persistence.EnumeratedValue;

public enum WarrantyRequestStatus {
    PENDING(0),
    IN_PROGRESS(1),
    REJECTED(2),
    COMPLETED(3);

    @EnumeratedValue
    private final byte code;

    WarrantyRequestStatus(int code) {
        this.code = (byte) code;
    }
}
