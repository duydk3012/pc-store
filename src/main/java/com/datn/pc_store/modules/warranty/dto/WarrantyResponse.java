package com.datn.pc_store.modules.warranty.dto;

import java.time.Instant;

public record WarrantyResponse(Long id, Long orderItemId, Long productSerialId, String serialNumber,
                               Long warrantyPolicyId, Instant startDate, Instant endDate, boolean active) {}
