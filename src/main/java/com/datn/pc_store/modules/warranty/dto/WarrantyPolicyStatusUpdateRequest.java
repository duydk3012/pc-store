package com.datn.pc_store.modules.warranty.dto;

import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu cập nhật trạng thái chính sách bảo hành")
public class WarrantyPolicyStatusUpdateRequest {

    @NotNull(message = "Trạng thái không được để trống")
    @Schema(description = "Trạng thái mới của chính sách bảo hành: INACTIVE (0), ACTIVE (1)", example = "ACTIVE")
    private WarrantyPolicyStatus status;
}
