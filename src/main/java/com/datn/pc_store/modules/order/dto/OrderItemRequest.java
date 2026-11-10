package com.datn.pc_store.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu mặt hàng đặt mua")
public class OrderItemRequest {
    @NotNull(message = "ID sản phẩm không được để trống")
    @Schema(description = "ID sản phẩm", example = "10001")
    private Long productId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    @Schema(description = "Số lượng đặt mua", example = "1")
    private Integer quantity;

    @NotEmpty(message = "Phải nhập serial cho sản phẩm đặt mua")
    @Schema(description = "Mỗi sản phẩm trong dòng hàng phải có một mã serial còn trong kho")
    private List<@NotBlank String> serialNumbers;

    @Min(value = 0, message = "Đơn giá không được âm")
    @Schema(description = "Đơn giá bán (nếu để trống hệ thống sẽ lấy theo giá sản phẩm hiện tại)", example = "4800000")
    private Long unitPrice;

    @Min(value = 0, message = "Giảm giá mặt hàng không được âm")
    @Schema(description = "Số tiền giảm giá cho dòng mặt hàng này", example = "0")
    private Long discountAmount;
}

