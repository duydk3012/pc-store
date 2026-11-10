package com.datn.pc_store.modules.order.dto;

import com.datn.pc_store.modules.order.enums.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu tạo đơn hàng mới")
public class OrderCreateRequest {
    @Schema(description = "Mã đơn hàng tùy chọn (nếu bỏ trống hệ thống tự sinh mã duy nhất)", example = "DH-20261105-001")
    private String code;

    @Schema(description = "ID khách hàng (chỉ áp dụng cho Admin/Nhân viên bán hàng tạo hộ đơn; với khách hàng tự đặt hệ thống lấy theo tài khoản đăng nhập)", example = "10011")
    private Long userId;

    @Schema(description = "Hình thức mua hàng (IN_STORE hoặc ONLINE, mặc định ONLINE)")
    private OrderType type;

    @Schema(description = "Tên người nhận hàng", example = "Nguyễn Văn A")
    private String recipientName;

    @Schema(description = "Số điện thoại người nhận", example = "0901234567")
    private String recipientPhone;

    @Schema(description = "Địa chỉ nhận hàng", example = "Số 123 Đường Cầu Giấy, Phường Quan Hoa, Cầu Giấy, Hà Nội")
    private String address;

    @Schema(description = "Ghi chú đơn hàng", example = "Giao giờ hành chính")
    private String note;

    @Schema(description = "Số tiền giảm giá chung của đơn (voucher / khuyến mãi)", example = "50000")
    private Long discountAmount;

    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    @Valid
    @Schema(description = "Danh sách sản phẩm đặt mua")
    private List<@jakarta.validation.constraints.NotNull OrderItemRequest> items;
}

