package com.datn.pc_store.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin dòng sản phẩm trong đơn hàng")
public class OrderItemResponse {
    @Schema(description = "ID dòng mặt hàng", example = "10001")
    private Long id;

    @Schema(description = "ID sản phẩm", example = "10001")
    private Long productId;

    @Schema(description = "Mã sản phẩm", example = "PROD-CPU-001")
    private String productCode;

    @Schema(description = "Tên sản phẩm", example = "Intel Core i5 13400")
    private String productName;

    @Schema(description = "Ảnh minh họa sản phẩm")
    private String productImageUrl;

    @Schema(description = "Số lượng đặt mua", example = "1")
    private Integer quantity;

    @Schema(description = "Đơn giá", example = "4800000")
    private Long unitPrice;

    @Schema(description = "Số tiền giảm giá", example = "10000")
    private Long discountAmount;

    @Schema(description = "Thành tiền của dòng sản phẩm", example = "4790000")
    private Long subtotal;

    @Schema(description = "Danh sách mã serial đã gán cho dòng sản phẩm")
    private List<String> serialNumbers;

    @Schema(description = "Thời điểm tạo")
    private Instant createdDate;

    @Schema(description = "Thời điểm cập nhật")
    private Instant lastModifiedDate;
}

