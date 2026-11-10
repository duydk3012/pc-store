package com.datn.pc_store.modules.comment.dto;

import jakarta.validation.constraints.*;

public record CommentRequest(@NotBlank @Size(max = 10000) String content) {}
