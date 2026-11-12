package com.datn.pc_store.modules.warranty.dto;

import com.datn.pc_store.modules.warranty.enums.WarrantyPartStatus;
import jakarta.validation.constraints.NotNull;

public record WarrantyPartStatusUpdateRequest(
        @NotNull(message = "Trạng thái linh kiện không được để trống")
        WarrantyPartStatus status
) {}

