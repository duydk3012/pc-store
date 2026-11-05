package com.datn.pc_store.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu gán mã serial vào các dòng sản phẩm của đơn hàng")
public class OrderAssignSerialRequest {
    @NotEmpty(message = "Danh sách gán serial không được để trống")
    @Schema(description = "Map giữa orderItemId và danh sách serialNumbers được quét gán theo FIFO")
    private Map<Long, List<String>> itemSerials;
}

