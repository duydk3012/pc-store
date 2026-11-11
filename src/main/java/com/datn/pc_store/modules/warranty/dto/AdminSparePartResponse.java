package com.datn.pc_store.modules.warranty.dto;

import com.datn.pc_store.modules.warranty.enums.WarrantyPartStatus;

import java.time.Instant;
import java.util.List;

public record AdminSparePartResponse(
        Long id,
        Long warrantyRequestId,
        String requestCode,
        Long productId,
        String productName,
        String productCode,
        String productCategory,
        int quantity,
        long unitCost,
        long totalCost,
        WarrantyPartStatus status,
        List<String> serialNumbers,
        String assignedTechnician,
        Instant createdDate,
        Instant lastModifiedDate
) {}

