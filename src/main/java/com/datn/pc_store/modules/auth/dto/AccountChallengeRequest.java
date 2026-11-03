package com.datn.pc_store.modules.auth.dto;

import jakarta.validation.constraints.*;

public record AccountChallengeRequest(
        @NotBlank @Size(max = 255)
        @Email(message = "Email không hợp lệ") String identifier) {
}
