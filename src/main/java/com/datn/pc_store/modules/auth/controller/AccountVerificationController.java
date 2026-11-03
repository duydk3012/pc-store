package com.datn.pc_store.modules.auth.controller;

import com.datn.pc_store.modules.auth.dto.*;
import com.datn.pc_store.modules.auth.security.CustomUserDetails;
import com.datn.pc_store.modules.auth.service.AccountVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Account verification")
public class AccountVerificationController {
    private final AccountVerificationService service;

    @PostMapping("/email-verification/request") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Gửi OTP xác thực email tài khoản hiện tại")
    public void requestEmail(@AuthenticationPrincipal CustomUserDetails principal) {
        service.requestEmailVerification(principal.getId());
    }

    @PostMapping("/email-verification/confirm") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Xác thực email bằng OTP")
    public void verifyEmail(@AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody VerifyCodeRequest request) {
        service.verifyEmail(principal.getId(), request.code());
    }

    @PostMapping("/forgot-password") @ResponseStatus(HttpStatus.ACCEPTED) @SecurityRequirements
    @Operation(summary = "Gửi OTP khôi phục mật khẩu qua email", description = "Luôn trả 202 nếu yêu cầu hợp lệ; không tiết lộ tài khoản tồn tại.")
    public void forgotPassword(@Valid @RequestBody AccountChallengeRequest request) {
        service.requestPasswordReset(request.identifier());
    }

    @PostMapping("/reset-password") @ResponseStatus(HttpStatus.NO_CONTENT) @SecurityRequirements
    @Operation(summary = "Đặt mật khẩu mới bằng OTP; đăng xuất mọi thiết bị")
    public void resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        service.resetPassword(request.account().identifier(), request.code(), request.newPassword());
    }

    @PostMapping("/change-password/request") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Gửi OTP đổi mật khẩu tới email của tài khoản hiện tại")
    public void requestChange(@AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody AccountChallengeRequest request) {
        service.requestPasswordChange(principal.getId(), request.identifier());
    }

    @PostMapping("/change-password") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Đổi mật khẩu bằng mật khẩu hiện tại và OTP; đăng xuất mọi thiết bị")
    public void changePassword(@AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody PasswordChangeRequest request) {
        service.changePassword(principal.getId(), request.account().identifier(), request.currentPassword(), request.code(), request.newPassword());
    }
}
