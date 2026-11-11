package com.datn.pc_store.modules.delivery.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Nguồn giao hàng: ORDER (0), PURCHASE_RETURN (1)")
public enum DeliveryResourceType {
    ORDER(0),
    PURCHASE_RETURN(1);

    private final int value;

    DeliveryResourceType(int value) {
        this.value = value;
    }

    public static DeliveryResourceType fromValue(int value) {
        for (DeliveryResourceType type : values()) {
            if (type.value == value) return type;
        }
        throw new IllegalArgumentException("Loại nguồn giao hàng không hợp lệ: " + value);
    }

    @JsonCreator
    public static DeliveryResourceType fromJson(Object input) {
        if (input == null) return null;
        if (input instanceof Number number) return fromValue(number.intValue());
        String value = input.toString().trim();
        for (DeliveryResourceType type : values()) {
            if (type.name().equalsIgnoreCase(value) || String.valueOf(type.value).equals(value)) return type;
        }
        throw new IllegalArgumentException("Loại nguồn giao hàng không hợp lệ: " + value);
    }
}
