package com.datn.pc_store.modules.product.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.product.dto.*;
import com.datn.pc_store.modules.product.enums.*;
import com.datn.pc_store.modules.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Products (Admin)", description = "Quản lý sản phẩm dành cho Quản trị viên (Admin)")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Xem danh sách và tìm kiếm sản phẩm",
            description = "Lấy sản phẩm chưa bị xóa có phân trang, tìm theo tên/mã/slug, lọc trạng thái, loại, thương hiệu và danh mục. "
                    + "Sắp xếp bằng sort=createdDate,desc; có thể lặp lại tham số sort. Các trường cho phép: id, name, code, slug, price, productType, status, brandId, createdDate, createdBy, lastModifiedDate, lastModifiedBy. Sort sai trả về 400.")
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @Parameter(description = "Từ khóa tìm kiếm theo tên, mã hoặc slug") @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo trạng thái") @RequestParam(required = false) ProductStatus status,
            @Parameter(description = "Lọc theo loại sản phẩm") @RequestParam(required = false) ProductType productType,
            @Parameter(description = "Lọc theo ID thương hiệu") @RequestParam(required = false) Long brandId,
            @Parameter(description = "Lọc theo ID danh mục") @RequestParam(required = false) Long categoryId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getAllProducts(keyword, status, productType, brandId, categoryId, false, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết sản phẩm theo ID", description = "Lấy đầy đủ thông tin sản phẩm chưa bị xóa, bao gồm ID thương hiệu, danh mục và linh kiện cấu hình (nếu là PC). Trả về 404 nếu không tìm thấy.")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID sản phẩm") @PathVariable Long id
    ) {
        return ResponseEntity.ok(productService.getProductById(id, false));
    }

    @GetMapping("/by-slug/{slug}")
    @Operation(summary = "Xem chi tiết sản phẩm theo slug", description = "Lấy thông tin sản phẩm theo đường dẫn thân thiện. Trả về 404 nếu không tìm thấy.")
    public ResponseEntity<ProductResponse> getProductBySlug(
            @Parameter(description = "Slug sản phẩm") @PathVariable String slug
    ) {
        return ResponseEntity.ok(productService.getProductBySlug(slug, false));
    }

    @PostMapping
    @Operation(summary = "Thêm mới sản phẩm", description = "Tạo sản phẩm và liên kết danh mục, tự tạo slug từ tên nếu không nhập. Mặc định loại OTHER (0 - sản phẩm khác) và trạng thái ACTIVE (1). Khi loại là PC (1), bắt buộc gửi kèm danh sách linh kiện items. Trả về 201 khi thành công, 400 khi dữ liệu sai, 404 khi thương hiệu/danh mục/linh kiện không tồn tại, 409 khi mã hoặc slug trùng.")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Chỉnh sửa thông tin sản phẩm", description = "Cập nhật sản phẩm theo ID. Tên và mã bắt buộc; slug tự tạo lại từ tên nếu không nhập. Không cho phép sửa productType. Các trường tùy chọn null sẽ xóa giá trị cũ, riêng trạng thái và price giữ nguyên nếu không gửi. categoryIds thay thế toàn bộ danh sách danh mục. Đối với PC, có thể gửi items để thay thế cấu hình linh kiện. Khi price thay đổi, cập nhật giá sản phẩm và tự động ghi nhận lịch sử giá mới. Trả về 404 nếu không tìm thấy, 409 nếu dữ liệu xung đột.")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID sản phẩm cần sửa") @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest req
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, req));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Chuyển trạng thái sản phẩm", description = "Cập nhật INACTIVE (ẩn), ACTIVE (hoạt động) hoặc OUT_OF_STOCK (hết hàng). Trả về 404 nếu sản phẩm không tồn tại.")
    public ResponseEntity<ProductResponse> updateStatus(
            @Parameter(description = "ID sản phẩm") @PathVariable Long id,
            @Valid @RequestBody ProductStatusUpdateRequest req
    ) {
        return ResponseEntity.ok(productService.updateStatus(id, req.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa sản phẩm (Xóa mềm)", description = "Đánh dấu xóa mềm sản phẩm, giữ dữ liệu liên quan và giữ chỗ mã/slug. Sản phẩm không còn hiển thị qua API. Trả về 204 khi thành công hoặc 404 nếu không tìm thấy.")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID sản phẩm cần xóa") @PathVariable Long id
    ) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
