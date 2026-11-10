package com.datn.pc_store.modules.delivery.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Trạng thái giao hàng: PICKED_UP (0), IN_TRANSIT (1), DELIVERED (2), RETURNED (3)")
public enum DeliveryStatus {
    PICKED_UP(0),
    IN_TRANSIT(1),
    DELIVERED(2),
    RETURNED(3);

    private final int value;

    DeliveryStatus(int value) {
        this.value = value;
    }

    public static DeliveryStatus fromValue(int value) {
        for (DeliveryStatus status : values()) {
            if (status.value == value) return status;
        }
        throw new IllegalArgumentException("Trạng thái giao hàng không hợp lệ: " + value);
    }

    @JsonCreator
    public static DeliveryStatus fromJson(Object input) {
        if (input == null) return null;
        if (input instanceof Number number) return fromValue(number.intValue());
        String value = input.toString().trim();
        for (DeliveryStatus status : values()) {
            if (status.name().equalsIgnoreCase(value) || String.valueOf(status.value).equals(value)) return status;
        }
        throw new IllegalArgumentException("Trạng thái giao hàng không hợp lệ: " + value);
    }
}
