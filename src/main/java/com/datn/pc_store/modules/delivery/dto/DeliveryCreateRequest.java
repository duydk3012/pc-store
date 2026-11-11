package com.datn.pc_store.modules.delivery.dto;

import com.datn.pc_store.modules.delivery.enums.DeliveryResourceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record DeliveryCreateRequest(
        @NotNull DeliveryResourceType resourceType,
        @NotNull @Positive Long resourceId,
        @Size(max = 255) String carrierName,
        @Size(max = 100) String trackingCode,
        @PositiveOrZero Long shippingFee
) {}
