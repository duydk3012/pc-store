package com.datn.pc_store.modules.brand.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.brand.dto.BrandCreateRequest;
import com.datn.pc_store.modules.brand.dto.BrandResponse;
import com.datn.pc_store.modules.brand.dto.BrandStatusUpdateRequest;
import com.datn.pc_store.modules.brand.dto.BrandUpdateRequest;
import com.datn.pc_store.modules.brand.enums.BrandStatus;
import com.datn.pc_store.modules.brand.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/brands")
@RequiredArgsConstructor
@Tag(name = "Brands (Admin)", description = "Quản lý thương hiệu sản phẩm dành cho Quản trị viên (Admin)")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Xem danh sách và tìm kiếm thương hiệu",
               description = "Lấy danh sách thương hiệu có phân trang, hỗ trợ tìm kiếm theo từ khóa và lọc theo trạng thái.")
    public ResponseEntity<PageResponse<BrandResponse>> getAllBrands(
            @Parameter(description = "Từ khóa tìm kiếm (tên, slug, mô tả)")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo trạng thái (ACTIVE, INACTIVE)")
            @RequestParam(required = false) BrandStatus status,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(brandService.getAllBrands(keyword, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết thương hiệu theo ID",
               description = "Lấy thông tin chi tiết của thương hiệu theo ID.")
    public ResponseEntity<BrandResponse> getBrandById(
            @Parameter(description = "ID thương hiệu")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @GetMapping("/by-slug/{slug}")
    @Operation(summary = "Xem chi tiết thương hiệu theo Slug",
               description = "Lấy thông tin chi tiết của thương hiệu theo đường dẫn thân thiện (slug).")
    public ResponseEntity<BrandResponse> getBrandBySlug(
            @Parameter(description = "Slug thương hiệu")
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(brandService.getBrandBySlug(slug));
    }

    @PostMapping
    @Operation(summary = "Thêm mới thương hiệu",
               description = "Tạo mới thương hiệu sản phẩm (tự động tạo slug nếu không nhập).")
    public ResponseEntity<BrandResponse> createBrand(
            @Valid @RequestBody BrandCreateRequest req
    ) {
        BrandResponse response = brandService.createBrand(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Chỉnh sửa thông tin thương hiệu",
               description = "Cập nhật thông tin của thương hiệu theo ID.")
    public ResponseEntity<BrandResponse> updateBrand(
            @Parameter(description = "ID thương hiệu cần sửa")
            @PathVariable Long id,
            @Valid @RequestBody BrandUpdateRequest req
    ) {
        return ResponseEntity.ok(brandService.updateBrand(id, req));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Chuyển trạng thái thương hiệu",
               description = "Cập nhật trạng thái hoạt động (ACTIVE hoặc INACTIVE) của thương hiệu.")
    public ResponseEntity<BrandResponse> updateStatus(
            @Parameter(description = "ID thương hiệu")
            @PathVariable Long id,
            @Valid @RequestBody BrandStatusUpdateRequest req
    ) {
        return ResponseEntity.ok(brandService.updateStatus(id, req.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa thương hiệu (Xóa mềm)",
               description = "Đánh dấu xóa mềm thương hiệu khỏi hệ thống.")
    public ResponseEntity<Void> deleteBrand(
            @Parameter(description = "ID thương hiệu cần xóa")
            @PathVariable Long id
    ) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}
