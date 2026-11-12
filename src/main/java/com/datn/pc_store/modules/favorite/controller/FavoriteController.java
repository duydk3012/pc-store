package com.datn.pc_store.modules.favorite.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.auth.security.CustomUserDetails;
import com.datn.pc_store.modules.favorite.dto.FavoriteResponse;
import com.datn.pc_store.modules.favorite.dto.FavoriteStatusResponse;
import com.datn.pc_store.modules.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
@Tag(name = "Product favorites", description = "Danh sách sản phẩm yêu thích của khách hàng đang đăng nhập")
public class FavoriteController {
    private final FavoriteService service;

    @GetMapping
    @Operation(summary = "Xem danh sách sản phẩm yêu thích")
    public PageResponse<FavoriteResponse> list(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(user.getId(), page, size);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Kiểm tra sản phẩm có trong danh sách yêu thích")
    public FavoriteStatusResponse getStatus(@PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return service.getStatus(user.getId(), productId);
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Thêm sản phẩm vào danh sách yêu thích; gọi lặp lại không tạo bản ghi trùng")
    public FavoriteResponse add(@PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return service.add(user.getId(), productId);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Gỡ sản phẩm khỏi danh sách yêu thích")
    public void remove(@PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails user) {
        service.remove(user.getId(), productId);
    }
}
