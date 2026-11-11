package com.datn.pc_store.modules.warranty.dto;

import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyStatus;
import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin phản hồi của chính sách bảo hành")
public class WarrantyPolicyResponse {

    @Schema(description = "ID chính sách bảo hành", example = "1")
    private Long id;

    @Schema(description = "Tên chính sách bảo hành", example = "Bảo hành tiêu chuẩn")
    private String name;


    @Schema(description = "Mô tả chính sách bảo hành", example = "Bảo hành lỗi nhà sản xuất")
    private String description;

    private Integer durationDays;

    @Schema(description = "CATEGORY (0): danh mục, PRODUCT (1): sản phẩm, BRAND (2): thương hiệu; bỏ trống cùng targetId để áp dụng chung")
    private WarrantyPolicyTargetType targetType;

    @Schema(description = "ID danh mục, sản phẩm hoặc thương hiệu tương ứng với targetType")
    private Long targetId;


    @Schema(description = "Trạng thái chính sách bảo hành", example = "ACTIVE")
    private WarrantyPolicyStatus status;

    @Schema(description = "Thời gian tạo")
    private Instant createdDate;

    @Schema(description = "ID người tạo")
    private Long createdBy;

    @Schema(description = "Thời gian cập nhật gần nhất")
    private Instant lastModifiedDate;

    @Schema(description = "ID người cập nhật gần nhất")
    private Long lastModifiedBy;
}
