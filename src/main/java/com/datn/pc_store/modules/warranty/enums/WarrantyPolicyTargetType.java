package com.datn.pc_store.modules.warranty.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Đối tượng áp dụng chính sách bảo hành: CATEGORY (0), PRODUCT (1), BRAND (2)")
public enum WarrantyPolicyTargetType {
    CATEGORY(0),
    PRODUCT(1),
    BRAND(2);

    private final int value;

    WarrantyPolicyTargetType(int value) {
        this.value = value;
    }

    public static WarrantyPolicyTargetType fromValue(int value) {
        for (WarrantyPolicyTargetType s : values()) {
            if (s.value == value) {
                return s;
            }
        }
        throw new IllegalArgumentException("Đối tượng áp dụng chính sách bảo hành không hợp lệ: " + value);
    }

    @JsonCreator
    public static WarrantyPolicyTargetType fromJson(Object input) {
        if (input == null) return null;
        if (input instanceof Number number) {
            return fromValue(number.intValue());
        }
        String str = input.toString().trim();
        for (WarrantyPolicyTargetType s : values()) {
            if (s.name().equalsIgnoreCase(str) || String.valueOf(s.value).equals(str)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Đối tượng áp dụng chính sách bảo hành không hợp lệ: " + str);
    }
}
