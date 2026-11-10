package com.datn.pc_store.modules.comment.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.comment.dto.*;
import com.datn.pc_store.modules.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/products/{productId}/comments")
@Tag(name = "Product comments")
public class CommentController {
    private final CommentService service;

    @GetMapping @SecurityRequirements
    @Operation(summary = "Danh sách bình luận gốc; truyền parentId để lấy các trả lời trực tiếp")
    public PageResponse<CommentResponse> list(@PathVariable Long productId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(productId, parentId, page, size);
    }

    @GetMapping("/{id}") @SecurityRequirements
    public CommentResponse get(@PathVariable Long productId, @PathVariable Long id) {
        return service.get(productId, id);
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(@PathVariable Long productId, @Valid @RequestBody CommentRequest request) {
        return service.create(productId, null, request);
    }

    @PostMapping("/{id}/replies") @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse reply(@PathVariable Long productId, @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {
        return service.create(productId, id, request);
    }

    @PutMapping("/{id}")
    public CommentResponse update(@PathVariable Long productId, @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {
        return service.update(productId, id, request);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long productId, @PathVariable Long id) {
        service.delete(productId, id);
    }
}
