package com.datn.pc_store.modules.goodsreceipt.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.goodsreceipt.dto.*;
import com.datn.pc_store.modules.goodsreceipt.enums.GoodsReceiptStatus;
import com.datn.pc_store.modules.goodsreceipt.service.GoodsReceiptService;
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

@RestController
@RequestMapping("/api/goods-receipts")
@RequiredArgsConstructor
@Tag(name = "Goods Receipts", description = "Quản lý phiếu nhập hàng và serial sản phẩm. Yêu cầu Bearer token")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    @GetMapping
    @Operation(summary = "Xem danh sách và tìm kiếm phiếu nhập hàng",
            description = "Tra cứu phiếu nhập theo mã/tên, lọc theo trạng thái, đơn đặt hàng (PO) và thời gian nhận. "
                    + "Mặc định sắp xếp theo createdDate desc.")
    public ResponseEntity<PageResponse<GoodsReceiptResponse>> getAllReceipts(
            @Parameter(description = "Từ khóa tìm theo mã hoặc tên phiếu") @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo trạng thái: PENDING, CONFIRMED, REJECTED, CANCELLED, DRAFT") @RequestParam(required = false) GoodsReceiptStatus status,
            @Parameter(description = "Lọc theo ID đơn đặt hàng (PO)") @RequestParam(required = false) Long purchaseOrderId,
            @Parameter(description = "Từ thời điểm nhận, ISO 8601", example = "2026-10-01T00:00:00Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @Parameter(description = "Đến thời điểm nhận, ISO 8601", example = "2026-10-31T23:59:59Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(goodsReceiptService.getAllReceipts(keyword, status, purchaseOrderId, fromDate, toDate, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết phiếu nhập hàng theo ID",
            description = "Lấy toàn bộ thông tin phiếu nhập, danh sách dòng hàng và chi tiết các serial sản phẩm (bao gồm cả serial đã bị kết thúc/terminated nếu phiếu bị từ chối/hủy).")
    public ResponseEntity<GoodsReceiptResponse> getReceiptById(
            @Parameter(description = "ID phiếu nhập") @PathVariable Long id
    ) {
        return ResponseEntity.ok(goodsReceiptService.getReceiptById(id));
    }

    @GetMapping("/by-code/{code}")
    @Operation(summary = "Xem chi tiết phiếu nhập hàng theo mã",
            description = "Tra cứu phiếu nhập hàng theo mã duy nhất.")
    public ResponseEntity<GoodsReceiptResponse> getReceiptByCode(
            @Parameter(description = "Mã phiếu nhập") @PathVariable String code
    ) {
        return ResponseEntity.ok(goodsReceiptService.getReceiptByCode(code));
    }

    @GetMapping("/{id}/duplicate-template")
    @Operation(summary = "Lấy dữ liệu mẫu nhân bản từ phiếu nhập đã hủy hoặc bị từ chối",
            description = "Dùng cho nhân viên kho (STAFF_WAREHOUSE, MANAGER, ADMIN) khi muốn tạo phiếu nhập mới dựa trên phiếu đã hủy (CANCELLED) hoặc bị từ chối (REJECTED). "
                    + "API trả về thông tin đã được chuẩn hóa (gợi ý mã mới không trùng, trạng thái DRAFT, thời gian hiện tại, serials được chuẩn hóa GOOD_CONDITION / DEFECTIVE, ...) "
                    + "để FE tự động điền sẵn vào Form tạo mới.")
    public ResponseEntity<GoodsReceiptDuplicateResponse> getDuplicateTemplate(
            @Parameter(description = "ID phiếu nhập gốc (phải ở trạng thái CANCELLED hoặc REJECTED)") @PathVariable Long id
    ) {
        return ResponseEntity.ok(goodsReceiptService.getDuplicateTemplate(id));
    }

    @GetMapping("/purchase-orders/{purchaseOrderId}/order-items")
    @Operation(summary = "Lấy danh sách mặt hàng trong đơn đặt hàng (PO)",
            description = "Trả về danh sách các mặt hàng trong đơn đặt hàng cùng số lượng đã nhận và còn lại để chọn khi tạo phiếu nhập.")
    public ResponseEntity<java.util.List<PurchaseOrderItemReceiptInfo>> getOrderItemsForReceipt(
            @Parameter(description = "ID đơn đặt hàng (PO)") @PathVariable Long purchaseOrderId
    ) {
        return ResponseEntity.ok(goodsReceiptService.getOrderItemsForReceipt(purchaseOrderId));
    }

    @PostMapping
    @Operation(summary = "Tạo phiếu nhập hàng",
            description = "Khởi tạo phiếu nhập hàng. Mặc định mang trạng thái PENDING (hoặc DRAFT nếu người dùng chọn lưu nháp). "
                    + "Các serial được khởi tạo với trạng thái GOOD_CONDITION hoặc DEFECTIVE. Các số lượng nhận, đạt, lỗi được tính tự động từ danh sách serial.")
    public ResponseEntity<GoodsReceiptResponse> createReceipt(
            @Valid @RequestBody GoodsReceiptCreateRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goodsReceiptService.createReceipt(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Chỉnh sửa phiếu nhập hàng lưu nháp",
            description = "Chỉ được phép chỉnh sửa phiếu khi ở trạng thái DRAFT. Thay thế toàn bộ items và danh sách serial.")
    public ResponseEntity<GoodsReceiptResponse> updateReceipt(
            @Parameter(description = "ID phiếu nhập cần sửa") @PathVariable Long id,
            @Valid @RequestBody GoodsReceiptUpdateRequest req
    ) {
        return ResponseEntity.ok(goodsReceiptService.updateReceipt(id, req));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Chuyển trạng thái phiếu nhập hàng",
            description = "Luồng chuyển trạng thái:\n"
                    + "- Từ PENDING: có thể chuyển thành DRAFT, CONFIRMED, REJECTED, CANCELLED.\n"
                    + "- Từ DRAFT: chỉ có thể chuyển thành PENDING khi người dùng gửi.\n"
                    + "- Khi chuyển thành CONFIRMED: serial GOOD_CONDITION -> IN_STOCK, DEFECTIVE -> PENDING_RETURN; cập nhật số lượng đã nhận trong PurchaseOrder.\n"
                    + "- Khi chuyển thành REJECTED hoặc CANCELLED: toàn bộ serial thuộc phiếu chuyển thành is_terminated = true.\n"
                    + "- Chuyển từ DRAFT sang PENDING: không thay đổi trạng thái serial.")
    public ResponseEntity<GoodsReceiptResponse> updateStatus(
            @Parameter(description = "ID phiếu nhập") @PathVariable Long id,
            @Valid @RequestBody GoodsReceiptStatusUpdateRequest req
    ) {
        return ResponseEntity.ok(goodsReceiptService.updateStatus(id, req.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa phiếu nhập hàng nháp",
            description = "Chỉ được phép xóa phiếu nhập khi ở trạng thái DRAFT.")
    public ResponseEntity<Void> deleteReceipt(
            @Parameter(description = "ID phiếu nhập cần xóa") @PathVariable Long id
    ) {
        goodsReceiptService.deleteReceipt(id);
        return ResponseEntity.noContent().build();
    }
}
