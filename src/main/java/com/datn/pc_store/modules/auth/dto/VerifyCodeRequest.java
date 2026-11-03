package com.datn.pc_store.modules.auth.dto;

import jakarta.validation.constraints.*;

public record VerifyCodeRequest(@NotBlank @Pattern(regexp = "\\d{6}") String code) {
}
