package com.datn.pc_store.modules.purchasereturn.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.purchasereturn.dto.*;
import com.datn.pc_store.modules.purchasereturn.enums.PurchaseReturnStatus;
import com.datn.pc_store.modules.purchasereturn.service.PurchaseReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-returns")
@RequiredArgsConstructor
@Tag(name = "Purchase Returns", description = "Quản lý hoàn trả hàng cho nhà cung cấp. Yêu cầu Bearer token")
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    @GetMapping
    @Operation(summary = "Xem danh sách và tìm kiếm phiếu hoàn trả",
            description = "Tra cứu phiếu hoàn trả theo từ khóa (mã hoặc tên phiếu), lọc theo trạng thái, nhà cung cấp và khoảng ngày. Sắp xếp mặc định theo createdDate desc.")
    public ResponseEntity<PageResponse<PurchaseReturnResponse>> getAllReturns(
            @Parameter(description = "Từ khóa tìm theo mã hoặc tên phiếu") @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo trạng thái: PENDING (0), DELIVERING (1), RETURNED (2), CANCELLED (3), DRAFT (4)") @RequestParam(required = false) PurchaseReturnStatus status,
            @Parameter(description = "Lọc theo ID nhà cung cấp") @RequestParam(required = false) Long supplierId,
            @Parameter(description = "Từ thời điểm, ISO 8601", example = "2026-10-01T00:00:00Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @Parameter(description = "Đến thời điểm, ISO 8601", example = "2026-10-31T23:59:59Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(purchaseReturnService.getAllReturns(keyword, status, supplierId, fromDate, toDate, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết phiếu hoàn trả theo ID",
            description = "Lấy toàn bộ thông tin phiếu hoàn trả, chi tiết từng serial sản phẩm, thông tin nhà cung cấp và giá vốn.")
    public ResponseEntity<PurchaseReturnResponse> getReturnById(
            @Parameter(description = "ID phiếu hoàn trả") @PathVariable Long id
    ) {
        return ResponseEntity.ok(purchaseReturnService.getReturnById(id));
    }

    @GetMapping("/by-code/{code}")
    @Operation(summary = "Xem chi tiết phiếu hoàn trả theo mã Code",
            description = "Tra cứu nhanh thông tin phiếu hoàn trả theo mã duy nhất (code).")
    public ResponseEntity<PurchaseReturnResponse> getReturnByCode(
            @Parameter(description = "Mã phiếu hoàn trả", example = "PR-20261029-001") @PathVariable String code
    ) {
        return ResponseEntity.ok(purchaseReturnService.getReturnByCode(code));
    }

    @PostMapping
    @Operation(summary = "Tạo mới phiếu hoàn trả hàng NCC",
            description = "Tạo phiếu ở trạng thái PENDING hoặc DRAFT kèm danh sách serial. Tự động kiểm tra serial có thuộc về NCC đó không và tính tổng tiền hoàn trả.")
    public ResponseEntity<PurchaseReturnResponse> createReturn(
            @Valid @RequestBody PurchaseReturnCreateRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseReturnService.createReturn(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật phiếu hoàn trả nháp",
            description = "Chỉ cho phép cập nhật khi phiếu đang ở trạng thái DRAFT.")
    public ResponseEntity<PurchaseReturnResponse> updateReturn(
            @Parameter(description = "ID phiếu hoàn trả") @PathVariable Long id,
            @Valid @RequestBody PurchaseReturnUpdateRequest req
    ) {
        return ResponseEntity.ok(purchaseReturnService.updateReturn(id, req));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Chuyển trạng thái phiếu hoàn trả",
            description = "Nhân viên cập nhật trạng thái phiếu hoàn. Khi bàn giao, gửi thông tin vận chuyển qua POST /api/deliveries. Kết quả giao được ghi nhận qua trạng thái vận đơn.")
    public ResponseEntity<PurchaseReturnResponse> updateStatus(
            @Parameter(description = "ID phiếu hoàn trả") @PathVariable Long id,
            @Valid @RequestBody PurchaseReturnStatusUpdateRequest req
    ) {
        return ResponseEntity.ok(purchaseReturnService.updateStatus(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa phiếu hoàn trả nháp",
            description = "Chỉ cho phép xóa khi phiếu ở trạng thái DRAFT.")
    public ResponseEntity<Void> deleteReturn(
            @Parameter(description = "ID phiếu hoàn trả") @PathVariable Long id
    ) {
        purchaseReturnService.deleteReturn(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available-serials")
    @Operation(summary = "Lấy danh sách serial lỗi khả dụng để xuất trả theo NCC",
            description = "Tìm các serial có trạng thái DEFECTIVE, PENDING_RETURN hoặc IN_STOCK của một NCC cụ thể chưa bị khóa trong phiếu hoàn trả nào khác.")
    public ResponseEntity<List<AvailableSerialResponse>> getAvailableSerials(
            @Parameter(description = "ID nhà cung cấp", required = true) @RequestParam Long supplierId,
            @Parameter(description = "ID sản phẩm (tùy chọn)") @RequestParam(required = false) Long productId,
            @Parameter(description = "Từ khóa tìm kiếm theo serial number") @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(purchaseReturnService.getAvailableSerialsForReturn(supplierId, productId, keyword));
    }

    @GetMapping("/validate-serial")
    @Operation(summary = "Kiểm tra nhanh tính hợp lệ của mã serial khi quét máy barcode",
            description = "Xác thực một serial cụ thể có thuộc về NCC đang chọn và có đủ điều kiện hoàn trả hay không. Trả về thông tin chi tiết để giao diện tự điền vào bảng.")
    public ResponseEntity<AvailableSerialResponse> validateSerial(
            @Parameter(description = "ID nhà cung cấp", required = true) @RequestParam Long supplierId,
            @Parameter(description = "Mã serial quét được", required = true) @RequestParam String serialNumber
    ) {
        return ResponseEntity.ok(purchaseReturnService.validateSerialForReturn(supplierId, serialNumber));
    }
}
