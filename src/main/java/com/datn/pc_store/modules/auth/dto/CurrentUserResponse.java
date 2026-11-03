package com.datn.pc_store.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Thông tin tài khoản đang đăng nhập")
public record CurrentUserResponse(
        @Schema(description = "ID tài khoản", example = "1")
        Long id,
        @Schema(description = "Tên đăng nhập", example = "customer01")
        String username,
        @Schema(description = "Danh sách quyền của tài khoản", example = "[\"ROLE_CUSTOMER\"]")
        List<String> authorities,
        boolean emailVerified
) {
}
