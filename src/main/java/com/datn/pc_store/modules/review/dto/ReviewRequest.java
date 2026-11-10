package com.datn.pc_store.modules.review.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ReviewRequest(
        @NotNull @Digits(integer = 1, fraction = 0) @Min(0) @Max(5) BigDecimal rating,
        @Size(max = 10000) String content) {
}
