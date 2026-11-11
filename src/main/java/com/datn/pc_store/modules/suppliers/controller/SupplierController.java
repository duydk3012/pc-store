package com.datn.pc_store.modules.suppliers.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.suppliers.dto.SupplierCreateRequest;
import com.datn.pc_store.modules.suppliers.dto.SupplierResponse;
import com.datn.pc_store.modules.suppliers.dto.SupplierStatusUpdateRequest;
import com.datn.pc_store.modules.suppliers.dto.SupplierUpdateRequest;
import com.datn.pc_store.modules.suppliers.enums.SupplierStatus;
import com.datn.pc_store.modules.suppliers.service.SupplierService;
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
@RequestMapping("/api/admin/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Quản lý nhà cung cấp dành cho Quản trị viên (Admin)")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(summary = "Xem danh sách và tìm kiếm nhà cung cấp",
               description = "Lấy danh sách nhà cung cấp có phân trang, hỗ trợ tìm kiếm theo từ khóa và lọc theo trạng thái.")
    public ResponseEntity<PageResponse<SupplierResponse>> getAllSuppliers(
            @Parameter(description = "Từ khóa tìm kiếm (mã, tên, email, SĐT, MST)")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo trạng thái (ACTIVE, INACTIVE)")
            @RequestParam(required = false) SupplierStatus status,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(supplierService.getAllSuppliers(keyword, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết nhà cung cấp theo ID",
               description = "Lấy thông tin chi tiết của một nhà cung cấp theo ID định danh.")
    public ResponseEntity<SupplierResponse> getSupplierById(
            @Parameter(description = "ID nhà cung cấp")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @GetMapping("/by-code/{code}")
    @Operation(summary = "Xem chi tiết nhà cung cấp theo Mã",
               description = "Lấy thông tin chi tiết của nhà cung cấp theo mã định danh duy nhất.")
    public ResponseEntity<SupplierResponse> getSupplierByCode(
            @Parameter(description = "Mã nhà cung cấp")
            @PathVariable String code
    ) {
        return ResponseEntity.ok(supplierService.getSupplierByCode(code));
    }

    @PostMapping
    @Operation(summary = "Thêm mới nhà cung cấp",
               description = "Tạo mới hồ sơ nhà cung cấp với các thông tin hợp lệ.")
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody SupplierCreateRequest req
    ) {
        SupplierResponse response = supplierService.createSupplier(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Chỉnh sửa thông tin nhà cung cấp",
               description = "Cập nhật thông tin của nhà cung cấp theo ID.")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @Parameter(description = "ID nhà cung cấp cần sửa")
            @PathVariable Long id,
            @Valid @RequestBody SupplierUpdateRequest req
    ) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, req));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Chuyển trạng thái nhà cung cấp",
               description = "Cập nhật trạng thái hoạt động (ACTIVE hoặc INACTIVE) của nhà cung cấp.")
    public ResponseEntity<SupplierResponse> updateStatus(
            @Parameter(description = "ID nhà cung cấp")
            @PathVariable Long id,
            @Valid @RequestBody SupplierStatusUpdateRequest req
    ) {
        return ResponseEntity.ok(supplierService.updateStatus(id, req.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa nhà cung cấp (Xóa mềm)",
               description = "Đánh dấu xóa mềm nhà cung cấp. Bản ghi sẽ được ẩn khỏi các truy vấn thông thường.")
    public ResponseEntity<Void> deleteSupplier(
            @Parameter(description = "ID nhà cung cấp cần xóa")
            @PathVariable Long id
    ) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
