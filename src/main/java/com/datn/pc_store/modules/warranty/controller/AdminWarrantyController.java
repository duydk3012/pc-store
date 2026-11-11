package com.datn.pc_store.modules.warranty.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.warranty.dto.AdminWarrantyResponse;
import com.datn.pc_store.modules.warranty.dto.WarrantyExtendRequest;
import com.datn.pc_store.modules.warranty.dto.WarrantyHistoryResponse;
import com.datn.pc_store.modules.warranty.service.AdminWarrantyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/warranties")
@RequiredArgsConstructor
@Tag(name = "Product Warranties (Admin)", description = "Quản lý bảo hành sản phẩm đã kích hoạt")
public class AdminWarrantyController {

    private final AdminWarrantyService service;

    @GetMapping
    @Operation(summary = "Danh sách bảo hành sản phẩm, tìm kiếm và phân trang")
    public ResponseEntity<PageResponse<AdminWarrantyResponse>> getAllWarranties(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAllWarranties(keyword, serialNumber, orderCode, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết gói bảo hành sản phẩm")
    public ResponseEntity<AdminWarrantyResponse> getWarrantyById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getWarrantyById(id));
    }

    @GetMapping("/by-serial/{serialNumber}")
    @Operation(summary = "Tra cứu bảo hành theo số serial")
    public ResponseEntity<AdminWarrantyResponse> getWarrantyBySerial(@PathVariable String serialNumber) {
        return ResponseEntity.ok(service.getWarrantyBySerial(serialNumber));
    }

    @PostMapping("/{id}/extend")
    @Operation(summary = "Gia hạn thời gian bảo hành cho sản phẩm")
    public ResponseEntity<AdminWarrantyResponse> extendWarranty(
            @PathVariable Long id,
            @Valid @RequestBody WarrantyExtendRequest req) {
        return ResponseEntity.ok(service.extendWarranty(id, req));
    }

    @PatchMapping("/{id}/void")
    @Operation(summary = "Hủy hiệu lực (vô hiệu hóa) gói bảo hành")
    public ResponseEntity<AdminWarrantyResponse> voidWarranty(@PathVariable Long id) {
        return ResponseEntity.ok(service.voidWarranty(id));
    }

    @GetMapping("/history/{serialNumber}")
    @Operation(summary = "Tra cứu toàn bộ lịch sử vòng đời bảo hành theo serial")
    public ResponseEntity<WarrantyHistoryResponse> getWarrantyHistoryBySerial(@PathVariable String serialNumber) {
        return ResponseEntity.ok(service.getWarrantyHistoryBySerial(serialNumber));
    }
}

