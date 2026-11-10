package com.datn.pc_store.modules.category.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.category.dto.*;
import com.datn.pc_store.modules.category.enums.CategoryStatus;
import com.datn.pc_store.modules.category.service.CategoryService;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Categories (Admin)", description = "Quản lý danh mục sản phẩm dành cho Quản trị viên (Admin)")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Xem danh sách và tìm kiếm danh mục",
               description = "Lấy danh sách danh mục có phân trang, hỗ trợ tìm kiếm theo từ khóa, lọc theo trạng thái và lọc theo danh mục cha.")
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories(
            @Parameter(description = "Từ khóa tìm kiếm (tên, slug, mô tả)")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo trạng thái (ACTIVE, INACTIVE)")
            @RequestParam(required = false) CategoryStatus status,
            @Parameter(description = "Lọc theo ID danh mục cha")
            @RequestParam(required = false) Long parentId,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(categoryService.getAllCategories(keyword, status, parentId, pageable));
    }

    @GetMapping("/tree")
    @Operation(summary = "Xem toàn bộ cây danh mục phân cấp",
               description = "Lấy cấu trúc cây danh mục đa cấp gồm tất cả các cấp cha - con.")
    public ResponseEntity<List<CategoryTreeResponse>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree(false));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết danh mục theo ID",
               description = "Lấy thông tin chi tiết của một danh mục theo ID định danh.")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "ID danh mục")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/by-slug/{slug}")
    @Operation(summary = "Xem chi tiết danh mục theo Slug",
               description = "Lấy thông tin chi tiết của danh mục theo đường dẫn thân thiện (slug).")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @Parameter(description = "Slug danh mục")
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    @PostMapping
    @Operation(summary = "Thêm mới danh mục",
               description = "Tạo mới danh mục sản phẩm (tự động tạo slug nếu không nhập).")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryCreateRequest req
    ) {
        CategoryResponse response = categoryService.createCategory(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Chỉnh sửa thông tin danh mục",
               description = "Cập nhật thông tin và quan hệ cha-con của danh mục theo ID.")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "ID danh mục cần sửa")
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest req
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(id, req));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Chuyển trạng thái danh mục",
               description = "Cập nhật trạng thái hoạt động (ACTIVE hoặc INACTIVE) của danh mục.")
    public ResponseEntity<CategoryResponse> updateStatus(
            @Parameter(description = "ID danh mục")
            @PathVariable Long id,
            @Valid @RequestBody CategoryStatusUpdateRequest req
    ) {
        return ResponseEntity.ok(categoryService.updateStatus(id, req.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa danh mục (Xóa mềm)",
               description = "Đánh dấu xóa mềm danh mục. Chỉ cho phép xóa khi không còn danh mục con hoạt động.")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID danh mục cần xóa")
            @PathVariable Long id
    ) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
