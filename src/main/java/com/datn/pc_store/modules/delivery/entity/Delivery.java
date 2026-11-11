package com.datn.pc_store.modules.delivery.entity;

import com.datn.pc_store.common.entity.BaseEntity;
import com.datn.pc_store.modules.delivery.enums.DeliveryResourceType;
import com.datn.pc_store.modules.delivery.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_type", nullable = false)
    private DeliveryResourceType resourceType;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @Column(name = "carrier_name", length = 255)
    private String carrierName;

    @Column(name = "shipping_fee", nullable = false)
    @Builder.Default
    private long shippingFee = 0L;

    @Column(nullable = false)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PICKED_UP;
}
