package com.datn.pc_store.modules.order.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Trạng thái đơn hàng: PENDING (0 - Chờ xác nhận), CONFIRMED (1 - Đã xác nhận), REJECTED (2 - Từ chối), PROCESSING (3 - Đang xử lý / đóng gói), COMPLETED (4 - Hoàn tất giao dịch), CANCELLED (5 - Đã hủy), RETURNED (6 - Trả hàng / hoàn tiền)")
public enum OrderStatus {
    PENDING(0),
    CONFIRMED(1),
    REJECTED(2),
    PROCESSING(3),
    COMPLETED(4),
    CANCELLED(5),
    RETURNED(6);

    private final int value;

    OrderStatus(int value) {
        this.value = value;
    }

    public static OrderStatus fromValue(int value) {
        for (var status : values()) {
            if (status.value == value) return status;
        }
        throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ: " + value);
    }

    @JsonCreator
    public static OrderStatus fromJson(Object input) {
        if (input == null) return null;
        if (input instanceof Number number) {
            return fromValue(number.intValue());
        }
        String str = input.toString().trim();
        for (var status : values()) {
            if (status.name().equalsIgnoreCase(str) || String.valueOf(status.value).equals(str)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ: " + str);
    }
}

