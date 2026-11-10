package com.datn.pc_store.modules.delivery.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.delivery.dto.DeliveryCreateRequest;
import com.datn.pc_store.modules.delivery.dto.DeliveryMetadataUpdateRequest;
import com.datn.pc_store.modules.delivery.dto.DeliveryResponse;
import com.datn.pc_store.modules.delivery.dto.DeliveryStatusUpdateRequest;
import com.datn.pc_store.modules.delivery.enums.DeliveryResourceType;
import com.datn.pc_store.modules.delivery.enums.DeliveryStatus;
import com.datn.pc_store.modules.delivery.service.DeliveryService;
import com.datn.pc_store.modules.auth.security.CustomUserDetails;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.net.URI;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@Tag(name = "Deliveries", description = "Quản lý giao hàng cho khách và hoàn trả tới nhà cung cấp")
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PostMapping
    @Operation(summary = "Tạo vận đơn khi nhân viên bàn giao hàng cho đơn vị vận chuyển")
    public ResponseEntity<DeliveryResponse> create(@Valid @RequestBody DeliveryCreateRequest request) {
        DeliveryResponse delivery = deliveryService.create(request, getCurrentUserId());
        return ResponseEntity.created(URI.create("/api/deliveries/" + delivery.getId())).body(delivery);
    }

    @GetMapping
    @Operation(summary = "Tìm kiếm và phân trang vận đơn")
    public ResponseEntity<PageResponse<DeliveryResponse>> getAll(
            @RequestParam(required = false) DeliveryResourceType resourceType,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) String carrierName,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @ParameterObject @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(deliveryService.getAll(resourceType, status, carrierName, keyword, fromDate, toDate, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem vận đơn theo ID")
    public ResponseEntity<DeliveryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getById(id));
    }

    @GetMapping("/by-resource/{resourceType}/{resourceId}")
    @Operation(summary = "Tra cứu vận đơn theo đơn hàng hoặc phiếu hoàn")
    public ResponseEntity<DeliveryResponse> getByResource(
            @Parameter(example = "ORDER") @PathVariable DeliveryResourceType resourceType,
            @PathVariable Long resourceId
    ) {
        return ResponseEntity.ok(deliveryService.getByResource(resourceType, resourceId));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Cập nhật mã vận đơn, đơn vị vận chuyển hoặc cước phí")
    public ResponseEntity<DeliveryResponse> updateMetadata(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryMetadataUpdateRequest request
    ) {
        return ResponseEntity.ok(deliveryService.updateMetadata(id, request, getCurrentUserId()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái giao hàng và đồng bộ đơn nguồn")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, request.getStatus(), getCurrentUserId()));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }
}
