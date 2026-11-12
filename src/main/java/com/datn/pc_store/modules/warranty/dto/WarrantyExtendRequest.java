package com.datn.pc_store.modules.warranty.dto;

import jakarta.validation.constraints.NotNull;

public record WarrantyExtendRequest(
        @NotNull(message = "Số ngày gia hạn không được để trống")
        Integer additionalDays,
        String note
) {}

