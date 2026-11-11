package com.datn.pc_store.modules.warranty.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record WarrantyRequestCreateRequest(
        @NotNull @Positive Long warrantyId,
        @NotNull @Positive Long customerId,
        @NotBlank @Size(max = 500) String issueDescription,
        List<@NotNull @Valid Part> parts
) {
    public record Part(
            @NotNull @Positive Long productId,
            @NotNull @Positive Integer quantity,
            @PositiveOrZero Long unitCost,
            List<@NotNull @Positive Long> productSerialIds
    ) {}
}
