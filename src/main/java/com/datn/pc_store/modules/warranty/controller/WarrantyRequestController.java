package com.datn.pc_store.modules.warranty.controller;

import com.datn.pc_store.modules.warranty.dto.*;
import com.datn.pc_store.modules.warranty.repository.WarrantyRepository;
import com.datn.pc_store.modules.warranty.service.WarrantyRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WarrantyRequestController {
    private final WarrantyRequestService service;
    private final WarrantyRepository warranties;

    @GetMapping("/warranties")
    @Tag(name = "Warranties", description = "Tra cứu bảo hành đã kích hoạt cho serial trong đơn hàng")
    @Operation(summary = "Tra cứu bảo hành đã cấp theo đơn hàng để tiếp nhận máy lỗi")
    public List<WarrantyResponse> findByOrder(@RequestParam Long orderId) {
        return warranties.findForOrder(orderId);
    }

    @PostMapping("/warranty-requests")
    @Tag(name = "Warranty Requests", description = "Tiếp nhận máy lỗi và ghi nhận linh kiện bảo hành")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tiếp nhận máy lỗi và ghi nhận linh kiện bảo hành")
    public WarrantyRequestResponse create(@Valid @RequestBody WarrantyRequestCreateRequest input) {
        return service.create(input);
    }
}
