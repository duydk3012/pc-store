package com.datn.pc_store.modules.review.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.auth.security.CustomUserDetails;
import com.datn.pc_store.modules.review.dto.ReviewRequest;
import com.datn.pc_store.modules.review.dto.ReviewResponse;
import com.datn.pc_store.modules.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/{productId}/reviews")
@Tag(name = "Product reviews")
public class ReviewController {
    private final ReviewService service;

    @GetMapping
    @SecurityRequirements
    public PageResponse<ReviewResponse> list(@PathVariable Long productId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(productId, page, size);
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    public ReviewResponse get(@PathVariable Long productId, @PathVariable Long id) {
        return service.get(productId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Đánh giá sản phẩm đã mua trong đơn COMPLETED, mỗi người một bài cho mỗi sản phẩm")
    public ReviewResponse create(@PathVariable Long productId, @AuthenticationPrincipal CustomUserDetails user,
                                 @Valid @RequestBody ReviewRequest request) {
        return service.create(productId, user.getId(), request);
    }

    @PutMapping("/{id}")
    public ReviewResponse update(@PathVariable Long productId, @PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody ReviewRequest request) {
        return service.update(productId, id, user.getId(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long productId, @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {
        service.delete(productId, id, user);
    }
}
