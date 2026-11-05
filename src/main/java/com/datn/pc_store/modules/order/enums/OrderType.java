package com.datn.pc_store.modules.order.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Hình thức mua hàng: IN_STORE (0 - Bán tại quầy), ONLINE (1 - Đặt trực tuyến)")
public enum OrderType {
    IN_STORE(0),
    ONLINE(1);

    private final int value;

    OrderType(int value) {
        this.value = value;
    }

    public static OrderType fromValue(int value) {
        for (var type : values()) {
            if (type.value == value) return type;
        }
        throw new IllegalArgumentException("Hình thức đơn hàng không hợp lệ: " + value);
    }

    @JsonCreator
    public static OrderType fromJson(Object input) {
        if (input == null) return null;
        if (input instanceof Number number) {
            return fromValue(number.intValue());
        }
        String str = input.toString().trim();
        for (var type : values()) {
            if (type.name().equalsIgnoreCase(str) || String.valueOf(type.value).equals(str)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Hình thức đơn hàng không hợp lệ: " + str);
    }
}

