package com.datn.pc_store.modules.warranty.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Trạng thái chính sách bảo hành: INACTIVE (0), ACTIVE (1)")
public enum WarrantyPolicyStatus {
    INACTIVE(0),
    ACTIVE(1);

    private final int value;

    WarrantyPolicyStatus(int value) {
        this.value = value;
    }

    public static WarrantyPolicyStatus fromValue(int value) {
        for (WarrantyPolicyStatus s : values()) {
            if (s.value == value) {
                return s;
            }
        }
        throw new IllegalArgumentException("Trạng thái chính sách bảo hành không hợp lệ: " + value);
    }

    public static WarrantyPolicyStatus fromValue(Object input) {
        return fromJson(input);
    }

    @JsonCreator
    public static WarrantyPolicyStatus fromJson(Object input) {
        if (input == null) return null;
        if (input instanceof Number number) {
            return fromValue(number.intValue());
        }
        String str = input.toString().trim();
        for (WarrantyPolicyStatus s : values()) {
            if (s.name().equalsIgnoreCase(str) || String.valueOf(s.value).equals(str)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Trạng thái chính sách bảo hành không hợp lệ: " + str);
    }
}
