package com.datn.pc_store.modules.order.dto;

import com.datn.pc_store.modules.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu cập nhật trạng thái đơn hàng")
public class OrderStatusUpdateRequest {
    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    @Schema(description = "Trạng thái mới cần cập nhật", example = "CONFIRMED")
    private OrderStatus status;
}

