package com.datn.pc_store.modules.delivery.dto;

import com.datn.pc_store.modules.delivery.enums.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Yêu cầu cập nhật trạng thái giao hàng")
public class DeliveryStatusUpdateRequest {
    @NotNull
    @Schema(description = "Trạng thái đích", example = "IN_TRANSIT")
    private DeliveryStatus status;
}
