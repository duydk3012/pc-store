package com.datn.pc_store.modules.user.dto;

import com.datn.pc_store.modules.user.enums.RoleType;
import com.datn.pc_store.modules.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin người dùng dành cho quản trị viên, không bao gồm mật khẩu và token")
public class UserResponse {

    @Schema(description = "ID người dùng", example = "1")
    private Long id;

    @Schema(description = "Tên đăng nhập", example = "customer01")
    private String username;

    @Schema(description = "Họ và tên", example = "Nguyễn Văn An")
    private String fullName;

    @Schema(description = "Địa chỉ email", example = "customer01@example.com")
    private String email;

    private boolean emailVerified;

    @Schema(description = "Số điện thoại", example = "0912345678")
    private String phoneNumber;

    @Schema(description = "Địa chỉ liên hệ", example = "123 Nguyễn Trãi, Hà Nội")
    private String address;

    @Schema(description = "Số căn cước công dân", example = "001099012345")
    private String citizenIdentificationCard;

    @Schema(description = "Vai trò tài khoản", example = "CUSTOMER")
    private RoleType role;

    @Schema(description = "Trạng thái tài khoản", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Thời gian tạo")
    private Instant createdDate;

    @Schema(description = "ID người tạo")
    private Long createdBy;

    @Schema(description = "Thời gian cập nhật gần nhất")
    private Instant lastModifiedDate;

    @Schema(description = "ID người cập nhật gần nhất")
    private Long lastModifiedBy;
}
