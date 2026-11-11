package com.datn.pc_store.modules.warranty.dto;

import java.time.Instant;

public record AdminWarrantyResponse(
        Long id,
        Long orderItemId,
        Long orderId,
        String orderCode,
        Long productSerialId,
        String serialNumber,
        Long productId,
        String productName,
        String productCode,
        String brandName,
        String categoryName,
        Long customerId,
        String customerName,
        String customerPhone,
        String customerEmail,
        Long warrantyPolicyId,
        String policyName,
        int durationDays,
        Instant startDate,
        Instant endDate,
        boolean active,
        String status,
        long remainingDays,
        String note,
        long requestsCount
) {}

