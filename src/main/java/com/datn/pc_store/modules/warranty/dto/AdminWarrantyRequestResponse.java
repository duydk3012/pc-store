package com.datn.pc_store.modules.warranty.dto;

import com.datn.pc_store.modules.warranty.enums.WarrantyPartStatus;
import com.datn.pc_store.modules.warranty.enums.WarrantyRequestStatus;

import java.time.Instant;
import java.util.List;

public record AdminWarrantyRequestResponse(
        Long id,
        String requestCode,
        Long warrantyId,
        String serialNumber,
        Long productId,
        String productName,
        String productCode,
        String orderCode,
        Long orderId,
        Long customerId,
        String customerName,
        String customerPhone,
        String customerEmail,
        Long technicianId,
        String technicianName,
        String technicianPhone,
        String issueDescription,
        String intakeAccessories,
        String deviceCondition,
        String inspectionNotes,
        String faultType,
        String solution,
        WarrantyRequestStatus status,
        Instant completedAt,
        Instant createdDate,
        Instant lastModifiedDate,
        List<Part> parts
) {
    public record Part(
            Long id,
            Long warrantyRequestId,
            Long productId,
            String productName,
            String productCode,
            int quantity,
            long unitCost,
            WarrantyPartStatus status,
            List<Long> productSerialIds,
            List<String> serialNumbers
    ) {}
}

