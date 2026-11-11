package com.datn.pc_store.modules.warranty.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.warranty.dto.AdminSparePartResponse;
import com.datn.pc_store.modules.warranty.dto.WarrantyPartStatusUpdateRequest;
import com.datn.pc_store.modules.warranty.enums.WarrantyPartStatus;
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
@RequestMapping("/api/admin/warranty-parts")
@RequiredArgsConstructor
@Tag(name = "Warranty Spare Parts (Admin)", description = "Quản lý linh kiện thay thế phục vụ bảo hành")
public class AdminWarrantyPartsController {

    private final AdminWarrantyService service;

    @GetMapping
    @Operation(summary = "Danh sách linh kiện bảo hành")
    public ResponseEntity<PageResponse<AdminSparePartResponse>> getAllSpareParts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) WarrantyPartStatus status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAllSpareParts(keyword, status, pageable));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái linh kiện thay thế")
    public ResponseEntity<AdminSparePartResponse> updatePartStatus(
            @PathVariable Long id,
            @Valid @RequestBody WarrantyPartStatusUpdateRequest req) {
        return ResponseEntity.ok(service.updateSparePartStatus(id, req.status()));
    }
}

