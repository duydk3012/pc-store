package com.datn.pc_store.modules.warranty.service;

import com.datn.pc_store.common.exception.BadRequestException;
import com.datn.pc_store.common.exception.ResourceNotFoundException;
import com.datn.pc_store.modules.goodsreceipt.entity.ProductSerial;
import com.datn.pc_store.modules.goodsreceipt.enums.ProductSerialStatus;
import com.datn.pc_store.modules.goodsreceipt.repository.ProductSerialRepository;
import com.datn.pc_store.modules.order.repository.OrderItemSerialRepository;
import com.datn.pc_store.modules.product.repository.ProductRepository;
import com.datn.pc_store.modules.user.repository.UserRepository;
import com.datn.pc_store.modules.warranty.dto.*;
import com.datn.pc_store.modules.warranty.entity.*;
import com.datn.pc_store.modules.warranty.enums.*;
import com.datn.pc_store.modules.warranty.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarrantyRequestService {
    private final WarrantyRepository warranties;
    private final WarrantyRequestRepository requests;
    private final OrderItemSerialRepository soldSerials;
    private final ProductSerialRepository inventory;
    private final ProductRepository products;
    private final UserRepository users;
    private final EntityManager entityManager;

    @Transactional
    public WarrantyRequestResponse create(WarrantyRequestCreateRequest input) {
        // All policies covering this physical unit share this lock during intake.
        var soldSerial = soldSerials.lockForWarranty(input.warrantyId())
                .orElseThrow(() -> new ResourceNotFoundException("Bảo hành của serial", "id", input.warrantyId()));
        var warranty = warranties.findById(input.warrantyId()).orElseThrow();
        Instant now = Instant.now();
        if (!warranty.isStatus() || now.isBefore(warranty.getStartDate()) || now.isAfter(warranty.getEndDate())) {
            throw new BadRequestException("Bảo hành không còn hiệu lực.");
        }
        if (requests.existsForSerial(soldSerial.getId(), List.of(WarrantyRequestStatus.PENDING, WarrantyRequestStatus.IN_PROGRESS))) {
            throw new BadRequestException("Serial đã có phiếu bảo hành đang xử lý.");
        }
        if (!users.existsById(input.customerId())) {
            throw new ResourceNotFoundException("Khách hàng", "id", input.customerId());
        }
        WarrantyRequest request = new WarrantyRequest();
        request.setRequestCode("WR-" + UUID.randomUUID());
        request.setWarrantyId(warranty.getId());
        request.setCustomerId(input.customerId());
        request.setIssueDescription(input.issueDescription().trim());
        requests.save(request);

        List<WarrantyRequestCreateRequest.Part> parts = input.parts() == null ? List.of() : input.parts();
        var selectedIds = new HashSet<Long>();
        for (var part : parts) {
            if (part.productSerialIds() == null || part.productSerialIds().isEmpty()) continue;
            if (part.productSerialIds().size() != part.quantity()) {
                throw new BadRequestException("Số serial thay thế phải bằng số lượng linh kiện.");
            }
            for (Long id : part.productSerialIds()) {
                if (id.equals(soldSerial.getProductSerialId()) || !selectedIds.add(id)) {
                    throw new BadRequestException("Serial thay thế trùng với máy lỗi hoặc linh kiện khác.");
                }
            }
        }
        Map<Long, ProductSerial> locked = selectedIds.isEmpty() ? Map.of() : inventory.lockByIds(selectedIds)
                .stream().collect(Collectors.toMap(ProductSerial::getId, Function.identity()));
        var result = new ArrayList<WarrantyRequestResponse.Part>();
        for (var part : parts) {
            result.add(savePart(request.getId(), part, locked));
        }
        return new WarrantyRequestResponse(request.getId(), request.getRequestCode(), request.getWarrantyId(),
                request.getCustomerId(), request.getIssueDescription(), request.getStatus(), result);
    }

    private WarrantyRequestResponse.Part savePart(Long requestId, WarrantyRequestCreateRequest.Part input,
                                                  Map<Long, ProductSerial> locked) {
        if (!products.existsById(input.productId())) {
            throw new ResourceNotFoundException("Sản phẩm thay thế", "id", input.productId());
        }
        var ids = input.productSerialIds() == null ? List.<Long>of() : input.productSerialIds();
        var part = new WarrantyPart();
        part.setWarrantyRequestId(requestId);
        part.setProductId(input.productId());
        part.setQuantity(input.quantity());
        part.setUnitCost(input.unitCost() == null ? 0L : input.unitCost());
        part.setStatus(ids.isEmpty() ? WarrantyPartStatus.PENDING : WarrantyPartStatus.RESERVED);
        entityManager.persist(part);
        for (Long id : ids) {
            ProductSerial serial = locked.get(id);
            if (serial == null || serial.isTerminated() || serial.getStatus() != ProductSerialStatus.IN_STOCK
                    || !serial.getProductId().equals(part.getProductId())) {
                throw new BadRequestException("Serial thay thế không thuộc sản phẩm hoặc không còn trong kho: " + id);
            }
            serial.setStatus(ProductSerialStatus.USING_BY_TECHNICAL);
            var link = new WarrantyPartSerial();
            link.setWarrantyPartId(part.getId());
            link.setProductSerialId(id);
            entityManager.persist(link);
        }
        return new WarrantyRequestResponse.Part(part.getId(), part.getProductId(), part.getQuantity(),
                part.getUnitCost(), part.getStatus(), ids);
    }
}
