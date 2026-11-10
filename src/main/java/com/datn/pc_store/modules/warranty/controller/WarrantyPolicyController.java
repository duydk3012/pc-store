package com.datn.pc_store.modules.warranty.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.warranty.dto.*;
import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyStatus;
import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyTargetType;
import com.datn.pc_store.modules.warranty.service.WarrantyPolicyService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/admin/warranty-policies")
@RequiredArgsConstructor
@Tag(name = "Warranty Policies (Admin)", description = "Quản lý chính sách bảo hành")
public class WarrantyPolicyController {
    private final WarrantyPolicyService service;

    @GetMapping
    @Operation(summary = "Danh sách chính sách bảo hành, tìm kiếm và phân trang")
    public ResponseEntity<PageResponse<WarrantyPolicyResponse>> getAllPolicies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) WarrantyPolicyStatus status,
            @RequestParam(required = false) WarrantyPolicyTargetType targetType,
            @RequestParam(required = false) Long targetId,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAllPolicies(keyword, status, targetType, targetId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết chính sách bảo hành")
    public ResponseEntity<WarrantyPolicyResponse> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPolicyById(id));
    }

    @PostMapping
    @Operation(summary = "Tạo chính sách bảo hành")
    public ResponseEntity<WarrantyPolicyResponse> createPolicy(@Valid @RequestBody WarrantyPolicyCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPolicy(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật chính sách bảo hành")
    public ResponseEntity<WarrantyPolicyResponse> updatePolicy(@PathVariable Long id,
            @Valid @RequestBody WarrantyPolicyUpdateRequest req) {
        return ResponseEntity.ok(service.updatePolicy(id, req));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái chính sách bảo hành")
    public ResponseEntity<WarrantyPolicyResponse> updateStatus(@PathVariable Long id,
            @Valid @RequestBody WarrantyPolicyStatusUpdateRequest req) {
        return ResponseEntity.ok(service.updateStatus(id, req.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mềm chính sách bảo hành")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        service.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}
