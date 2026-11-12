package com.datn.pc_store.modules.warranty.dto;

import java.time.Instant;
import java.util.List;

public record WarrantyHistoryResponse(
        String serialNumber,
        String productName,
        String productCode,
        String customerName,
        String customerPhone,
        String orderCode,
        Instant purchaseDate,
        Instant warrantyStartDate,
        Instant warrantyEndDate,
        String policyName,
        String status,
        int totalRequests,
        List<TimelineEvent> timeline
) {
    public record TimelineEvent(
            String id,
            Instant timestamp,
            String title,
            String description,
            String actor,
            String type,
            String badgeColor
    ) {}
}

