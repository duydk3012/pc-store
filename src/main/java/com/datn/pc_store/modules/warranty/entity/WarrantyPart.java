package com.datn.pc_store.modules.warranty.entity;

import com.datn.pc_store.common.entity.BaseEntity;
import com.datn.pc_store.modules.warranty.enums.WarrantyPartStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "warranty_parts")
@Getter
@Setter
public class WarrantyPart extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long warrantyRequestId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private long unitCost;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private WarrantyPartStatus status = WarrantyPartStatus.PENDING;
}
