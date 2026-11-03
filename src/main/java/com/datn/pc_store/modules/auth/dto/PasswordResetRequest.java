package com.datn.pc_store.modules.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record PasswordResetRequest(
        @NotNull @Valid AccountChallengeRequest account,
        @NotBlank @Pattern(regexp = "\\d{6}") String code,
        @NotBlank @Size(min = 8, max = 72) String newPassword) {
}
