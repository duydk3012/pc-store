package com.datn.pc_store.modules.warranty.dto;

import com.datn.pc_store.modules.warranty.enums.WarrantyRequestStatus;

public record TechnicalHandlingUpdateRequest(
        Long technicianId,
        String technicianName,
        String inspectionNotes,
        String faultType,
        String solution,
        WarrantyRequestStatus status
) {}

