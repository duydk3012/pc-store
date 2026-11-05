package com.datn.pc_store.modules.order.dto;

import com.datn.pc_store.modules.order.enums.OrderStatus;
import com.datn.pc_store.modules.order.enums.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin chi tiết đơn hàng")
public class OrderResponse {
    @Schema(description = "ID đơn hàng", example = "10001")
    private Long id;

    @Schema(description = "Mã đơn hàng", example = "DEMO-DH-001")
    private String code;

    @Schema(description = "ID khách hàng", example = "10011")
    private Long userId;

    @Schema(description = "Tài khoản khách hàng", example = "demo_customer01")
    private String customerUsername;

    @Schema(description = "Tên đầy đủ của khách hàng", example = "Nguyễn Minh An")
    private String customerName;

    @Schema(description = "Email khách hàng", example = "demo_customer01@example.com")
    private String customerEmail;

    @Schema(description = "Số điện thoại khách hàng", example = "0901000011")
    private String customerPhone;

    @Schema(description = "Tên người nhận", example = "Nguyễn Minh An")
    private String recipientName;

    @Schema(description = "Số điện thoại người nhận", example = "0901000011")
    private String recipientPhone;

    @Schema(description = "Địa chỉ nhận hàng", example = "1 Đường Demo, Hà Nội")
    private String address;

    @Schema(description = "Ghi chú đơn hàng", example = "Giao hàng cẩn thận")
    private String note;

    @Schema(description = "Hình thức mua hàng: IN_STORE hoặc ONLINE", example = "ONLINE")
    private OrderType type;

    @Schema(description = "Tổng tiền hàng chưa giảm giá", example = "4800000")
    private Long totalAmount;

    @Schema(description = "Số tiền giảm giá", example = "10000")
    private Long discountAmount;

    @Schema(description = "Tổng tiền thanh toán cuối cùng", example = "4790000")
    private Long finalAmount;

    @Schema(description = "Trạng thái đơn hàng", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "Danh sách sản phẩm trong đơn")
    private List<OrderItemResponse> items;

    @Schema(description = "Thời điểm đặt hàng")
    private Instant createdDate;

    @Schema(description = "ID người tạo")
    private Long createdBy;

    @Schema(description = "Thời điểm cập nhật cuối")
    private Instant lastModifiedDate;

    @Schema(description = "ID người cập nhật cuối")
    private Long lastModifiedBy;
}

