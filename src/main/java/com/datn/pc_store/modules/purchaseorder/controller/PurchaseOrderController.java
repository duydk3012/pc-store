package com.datn.pc_store.modules.purchaseorder.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.purchaseorder.dto.*;
import com.datn.pc_store.modules.purchaseorder.enums.PurchaseOrderStatus;
import com.datn.pc_store.modules.purchaseorder.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Orders", description = "Quản lý đơn nhập hàng: Admin/Quản lý lập và xử lý đơn; nhân viên kho được tra cứu. Yêu cầu Bearer token")
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    @Operation(summary = "Xem danh sách và tìm kiếm đơn nhập hàng",
            description = "Lấy đơn chưa bị xóa, tìm theo mã/tên, lọc trạng thái, nhà cung cấp và thời điểm đặt hàng (bao gồm hai mốc). "
                    + "Mặc định 10 đơn/trang, sort=createdDate,desc. Có thể lặp lại sort; các trường cho phép: id, code, name, supplierId, orderDate, expectedDate, status, totalAmount, createdDate, createdBy, lastModifiedDate, lastModifiedBy. Sort hoặc khoảng thời gian sai trả về 400.")
    public ResponseEntity<PageResponse<PurchaseOrderResponse>> getAllOrders(
            @Parameter(description = "Từ khóa tìm kiếm theo mã hoặc tên đơn") @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc DRAFT, PENDING, APPROVED, RECEIVED hoặc CANCELLED") @RequestParam(required = false) PurchaseOrderStatus status,
            @Parameter(description = "Lọc theo ID nhà cung cấp") @RequestParam(required = false) Long supplierId,
            @Parameter(description = "Từ thời điểm đặt hàng, ISO 8601", example = "2026-10-01T00:00:00Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @Parameter(description = "Đến thời điểm đặt hàng, ISO 8601", example = "2026-10-31T23:59:59Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(purchaseOrderService.getAllOrders(keyword, status, supplierId, fromDate, toDate, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết đơn nhập theo ID", description = "Lấy thông tin đơn, các mặt hàng đặt mua, đơn giá, thành tiền và số lượng đã nhận. Trả về 404 nếu không tồn tại hoặc đã xóa.")
    public ResponseEntity<PurchaseOrderResponse> getOrderById(
            @Parameter(description = "ID đơn nhập") @PathVariable Long id
    ) {
        return ResponseEntity.ok(purchaseOrderService.getOrderById(id));
    }

    @GetMapping("/by-code/{code}")
    @Operation(summary = "Xem chi tiết đơn nhập theo mã", description = "Tra cứu đơn nhập chưa bị xóa theo mã duy nhất. Trả về 404 nếu không tìm thấy.")
    public ResponseEntity<PurchaseOrderResponse> getOrderByCode(
            @Parameter(description = "Mã đơn nhập") @PathVariable String code
    ) {
        return ResponseEntity.ok(purchaseOrderService.getOrderByCode(code));
    }

    @PostMapping
    @Operation(summary = "Lập đơn nhập hàng nháp", description = "Tạo DRAFT, có thể chưa có mặt hàng. Hệ thống tự tính subtotal và totalAmount, khởi tạo quantityReceived bằng 0; không nhận các giá trị này từ client. Nhà cung cấp phải ACTIVE, sản phẩm không INACTIVE. Trả về 201 khi thành công, 400 khi dữ liệu sai, 404 nếu đối tượng liên quan không tồn tại, 409 nếu mã đơn trùng.")
    public ResponseEntity<PurchaseOrderResponse> createOrder(@Valid @RequestBody PurchaseOrderCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderService.createOrder(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Chỉnh sửa đơn nhập nháp", description = "Chỉ sửa DRAFT chưa có phiếu nhập và chưa nhận hàng. Thay thế toàn bộ items, tự tính lại tổng tiền; items null/rỗng sẽ xóa danh sách cũ. orderDate giữ nguyên nếu không gửi; các trường tùy chọn còn lại null sẽ xóa giá trị cũ. Không sửa trạng thái hoặc số lượng đã nhận qua API này. Trả về 409 nếu đơn không còn được sửa.")
    public ResponseEntity<PurchaseOrderResponse> updateOrder(
            @Parameter(description = "ID đơn nhập cần sửa") @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderUpdateRequest req
    ) {
        return ResponseEntity.ok(purchaseOrderService.updateOrder(id, req));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Gửi duyệt, duyệt, hủy hoặc hoàn tất đơn nhập",
            description = "Luồng: DRAFT → PENDING → APPROVED → RECEIVED. Gửi/duyệt yêu cầu ít nhất một mặt hàng và dữ liệu liên quan còn hợp lệ. "
                    + "DRAFT/PENDING/APPROVED có thể chuyển CANCELLED nếu chưa nhận hàng và không có phiếu nhập PENDING/CONFIRMED. "
                    + "RECEIVED yêu cầu nhận đủ mọi mặt hàng, số lượng khớp các phiếu nhập CONFIRMED và không còn phiếu PENDING. Không cập nhật tồn kho, serial hoặc tạo phiếu nhập tại đây. "
                    + "Không quay về DRAFT; RECEIVED/CANCELLED là trạng thái kết thúc. Gửi lại đúng trạng thái hiện tại không làm thay đổi đơn. Trả về 409 nếu chuyển trạng thái không hợp lệ.")
    public ResponseEntity<PurchaseOrderResponse> updateStatus(
            @Parameter(description = "ID đơn nhập") @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderStatusUpdateRequest req
    ) {
        return ResponseEntity.ok(purchaseOrderService.updateStatus(id, req.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm đơn nhập nháp", description = "Chỉ xóa DRAFT chưa phát sinh phiếu nhập và chưa nhận hàng. Giữ chi tiết mặt hàng và mã đơn trong database để lưu lịch sử. Trả về 204 khi thành công, 404 nếu không tìm thấy, 409 nếu không được xóa.")
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID đơn nhập cần xóa") @PathVariable Long id
    ) {
        purchaseOrderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
