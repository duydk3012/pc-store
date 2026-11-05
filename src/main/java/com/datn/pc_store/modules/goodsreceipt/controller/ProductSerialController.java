package com.datn.pc_store.modules.goodsreceipt.controller;

import com.datn.pc_store.common.dto.PageResponse;
import com.datn.pc_store.modules.goodsreceipt.dto.ProductSerialResponse;
import com.datn.pc_store.modules.goodsreceipt.enums.ProductSerialStatus;
import com.datn.pc_store.modules.goodsreceipt.service.ProductSerialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-serials")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF_WAREHOUSE', 'STAFF_TECH', 'STAFF_SALES')")
@Tag(name = "Product Serials", description = "Tra cứu serial sản phẩm. Yêu cầu Bearer token")
public class ProductSerialController {

    private final ProductSerialService productSerialService;

    @GetMapping
    @Operation(summary = "Xem danh sách và tìm kiếm serial sản phẩm",
            description = "Tra cứu danh sách serial sản phẩm phân trang, lọc theo nhà cung cấp (supplierId), sản phẩm (productId), trạng thái (status), từ khóa serial number (keyword).")
    public ResponseEntity<PageResponse<ProductSerialResponse>> searchSerials(
            @Parameter(description = "ID nhà cung cấp") @RequestParam(required = false) Long supplierId,
            @Parameter(description = "ID sản phẩm") @RequestParam(required = false) Long productId,
            @Parameter(description = "Trạng thái serial sản phẩm") @RequestParam(required = false) ProductSerialStatus status,
            @Parameter(description = "Từ khóa tìm theo số serial") @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(productSerialService.searchSerials(supplierId, productId, status, keyword, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem thông tin serial theo ID", description = "Tra cứu thông tin serial sản phẩm theo ID")
    public ResponseEntity<ProductSerialResponse> getById(
            @Parameter(description = "ID serial") @PathVariable Long id
    ) {
        return ResponseEntity.ok(productSerialService.getById(id));
    }

    @GetMapping("/by-serial/{serialNumber}")
    @Operation(summary = "Xem thông tin serial theo số serial", description = "Tra cứu thông tin chi tiết một serial theo số serial")
    public ResponseEntity<ProductSerialResponse> getBySerialNumber(
            @Parameter(description = "Số serial") @PathVariable String serialNumber
    ) {
        return ResponseEntity.ok(productSerialService.getBySerialNumber(serialNumber));
    }

    @GetMapping("/by-product/{productId}")
    @Operation(summary = "Lấy danh sách serial còn hiệu lực theo sản phẩm",
            description = "Lọc các serial chưa bị kết thúc (is_terminated = false) của một sản phẩm, hỗ trợ lọc theo trạng thái serial và phân trang")
    public ResponseEntity<PageResponse<ProductSerialResponse>> getActiveSerialsByProduct(
            @Parameter(description = "ID sản phẩm") @PathVariable Long productId,
            @Parameter(description = "Lọc theo trạng thái serial sản phẩm") @RequestParam(required = false) ProductSerialStatus status,
            @ParameterObject @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(productSerialService.getActiveSerialsByProduct(productId, status, pageable));
    }
}
