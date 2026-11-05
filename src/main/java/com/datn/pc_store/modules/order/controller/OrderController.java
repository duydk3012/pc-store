package com.datn.pc_store.modules.order.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.auth.security.CustomUserDetails;
import com.datn.pc_store.modules.order.dto.OrderAssignSerialRequest;
import com.datn.pc_store.modules.order.dto.OrderCreateRequest;
import com.datn.pc_store.modules.order.dto.OrderResponse;
import com.datn.pc_store.modules.order.dto.OrderStatusUpdateRequest;
import com.datn.pc_store.modules.order.enums.OrderStatus;
import com.datn.pc_store.modules.order.enums.OrderType;
import com.datn.pc_store.modules.order.service.OrderService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Quản lý quy trình đặt hàng và bán hàng 4 giai đoạn. Yêu cầu Bearer token")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem danh sách và tìm kiếm đơn hàng",
            description = "Tra cứu danh sách đơn hàng. Nhân viên/Admin xem được tất cả đơn; Khách hàng thông thường chỉ xem danh sách đơn của chính mình.")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @Parameter(description = "Từ khóa tìm kiếm theo mã đơn, khách hàng, số điện thoại, địa chỉ")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo trạng thái đơn hàng")
            @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Lọc theo hình thức đặt hàng (IN_STORE, ONLINE)")
            @RequestParam(required = false) OrderType type,
            @Parameter(description = "Lọc theo ID khách hàng")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Từ thời điểm tạo đơn, ISO 8601", example = "2026-10-01T00:00:00Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @Parameter(description = "Đến thời điểm tạo đơn, ISO 8601", example = "2026-10-31T23:59:59Z")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long currentUserId = getCurrentUserId();
        boolean staffOrAdmin = isStaffOrAdmin();

        Long effectiveUserId = staffOrAdmin ? userId : currentUserId;

        return ResponseEntity.ok(orderService.getAllOrders(
                keyword, status, type, effectiveUserId, fromDate, toDate, pageable
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem chi tiết đơn hàng theo ID",
            description = "Lấy đầy đủ thông tin đơn hàng, khách hàng, người nhận và danh sách mặt hàng đã đặt mua kèm mã serial.")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "ID đơn hàng") @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.getOrderById(id, getCurrentUserId(), isStaffOrAdmin()));
    }

    @GetMapping("/by-code/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem chi tiết đơn hàng theo mã đơn",
            description = "Tra cứu đơn hàng qua mã duy nhất.")
    public ResponseEntity<OrderResponse> getOrderByCode(
            @Parameter(description = "Mã đơn hàng") @PathVariable String code
    ) {
        return ResponseEntity.ok(orderService.getOrderByCode(code, getCurrentUserId(), isStaffOrAdmin()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "GĐ 1: Khởi tạo đơn hàng mới",
            description = "Khách hàng đặt hàng trực tuyến hoặc Admin/Nhân viên bán hàng tạo đơn bán lẻ tại quầy.")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(req, getCurrentUserId(), isStaffOrAdmin()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhật trạng thái đơn hàng",
            description = "Nhân viên/Admin cập nhật trạng thái đơn hàng (CONFIRMED, PROCESSING, COMPLETED, CANCELLED). Khách hàng có thể hủy đơn khi PENDING hoặc xác nhận nhận hàng khi PROCESSING.")
    public ResponseEntity<OrderResponse> updateStatus(
            @Parameter(description = "ID đơn hàng") @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest req
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, req.getStatus(), getCurrentUserId(), isStaffOrAdmin()));
    }

    @PostMapping("/{id}/assign-serials")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF_WAREHOUSE', 'STAFF_SALES')")
    @Operation(summary = "GĐ 3: Quét mã Serial định danh & đóng gói sản phẩm",
            description = "Nhân viên kho quét gán mã serial cho các sản phẩm trong đơn hàng. Các serial sẽ tự động chuyển sang trạng thái PACKING (Đang đóng gói).")
    public ResponseEntity<OrderResponse> assignSerials(
            @Parameter(description = "ID đơn hàng") @PathVariable Long id,
            @Valid @RequestBody OrderAssignSerialRequest req
    ) {
        return ResponseEntity.ok(orderService.assignSerialsToOrder(id, req.getItemSerials()));
    }

    @PostMapping("/{id}/handover")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF_WAREHOUSE')")
    @Operation(summary = "GĐ 3 Bước 5: Bàn giao đơn hàng cho ĐVVC ngoài",
            description = "Bàn giao kiện hàng cho đối tác vận chuyển, chuyển trạng thái serial sang DELIVERING (Đang giao).")
    public ResponseEntity<OrderResponse> handoverToCarrier(
            @Parameter(description = "ID đơn hàng") @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.handoverToCarrier(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "GĐ 4 Bước 3: Khách nhận hàng & Hoàn tất đơn hàng",
            description = "Xác nhận nhận hàng thành công. Trạng thái đơn hàng chuyển thành COMPLETED và TẤT CẢ CÁC SERIAL CHUYỂN SANG TRẠNG THÁI SOLD (ĐÃ BÁN).")
    public ResponseEntity<OrderResponse> completeOrder(
            @Parameter(description = "ID đơn hàng") @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, OrderStatus.COMPLETED, getCurrentUserId(), isStaffOrAdmin()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Xóa đơn hàng",
            description = "Chỉ cho phép xóa đơn khi chưa hoàn tất, chưa xuất hóa đơn và chưa có bản ghi thanh toán.")
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID đơn hàng cần xóa") @PathVariable Long id
    ) {
        orderService.deleteOrder(id, isStaffOrAdmin());
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails principal) {
            return principal.getId();
        }
        return null;
    }

    private boolean isStaffOrAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_MANAGER")
                        || a.getAuthority().equals("ROLE_STAFF_SALES")
                        || a.getAuthority().equals("ROLE_STAFF_WAREHOUSE")
                        || a.getAuthority().equals("ROLE_STAFF_TECH"));
    }
}
