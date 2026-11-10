package com.datn.pc_store.modules.warranty.dto;

import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyStatus;
import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu thêm mới chính sách bảo hành")
public class WarrantyPolicyCreateRequest {

    @NotBlank(message = "Tên chính sách bảo hành không được để trống")
    @Size(max = 255, message = "Tên chính sách bảo hành tối đa 255 ký tự")
    @Schema(description = "Tên chính sách bảo hành", example = "Bảo hành tiêu chuẩn")
    private String name;


    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    @Schema(description = "Mô tả chính sách bảo hành", example = "Bảo hành lỗi nhà sản xuất")
    private String description;

    @NotNull
    @Positive
    private Integer durationDays;

    @Schema(description = "CATEGORY (0): danh mục, PRODUCT (1): sản phẩm, BRAND (2): thương hiệu; bỏ trống cùng targetId để áp dụng chung")
    private WarrantyPolicyTargetType targetType;

    @Positive
    @Schema(description = "ID danh mục, sản phẩm hoặc thương hiệu tương ứng với targetType")
    private Long targetId;


    @Schema(description = "Trạng thái chính sách bảo hành: INACTIVE (0), ACTIVE (1); mặc định ACTIVE nếu không nhập", example = "ACTIVE")
    private WarrantyPolicyStatus status;
}
