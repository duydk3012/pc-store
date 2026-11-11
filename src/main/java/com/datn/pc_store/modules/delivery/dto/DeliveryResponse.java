package com.datn.pc_store.modules.delivery.dto;

import com.datn.pc_store.modules.delivery.enums.DeliveryResourceType;
import com.datn.pc_store.modules.delivery.enums.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@Schema(description = "Thông tin giao hàng khớp với bảng delivery trong migration init_schema")
public class DeliveryResponse {
    private Long id;
    private DeliveryResourceType resourceType;
    private Long resourceId;
    private String resourceCode;
    private String trackingCode;
    private String carrierName;
    private long shippingFee;
    private DeliveryStatus status;
    private Long createdBy;
    private Instant createdDate;
    private Long lastModifiedBy;
    private Instant lastModifiedDate;
}
