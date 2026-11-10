package com.datn.pc_store.modules.warranty.dto;

import com.datn.pc_store.modules.warranty.enums.WarrantyRequestStatus;
import com.datn.pc_store.modules.warranty.enums.WarrantyPartStatus;
import java.util.List;

public record WarrantyRequestResponse(Long id, String requestCode, Long warrantyId, Long customerId,
                                     String issueDescription, WarrantyRequestStatus status, List<Part> parts) {
    public record Part(Long id, Long productId, int quantity, long unitCost, WarrantyPartStatus status,
                       List<Long> productSerialIds) {}
}
