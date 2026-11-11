package com.datn.pc_store.modules.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Thông tin vận đơn được lưu trong bảng delivery")
public class DeliveryMetadataUpdateRequest {
    @Size(max = 100)
    private String trackingCode;

    @Size(max = 255)
    private String carrierName;

    @PositiveOrZero
    private Long shippingFee;
}
