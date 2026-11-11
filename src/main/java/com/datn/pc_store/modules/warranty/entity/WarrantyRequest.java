package com.datn.pc_store.modules.warranty.entity;

import com.datn.pc_store.common.entity.BaseEntity;
import com.datn.pc_store.modules.warranty.enums.WarrantyRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "warranty_request")
@Getter
@Setter
public class WarrantyRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String requestCode;

    @Column(nullable = false)
    private Long warrantyId;

    @Column(nullable = false)
    private Long customerId;

    @Column(length = 500)
    private String issueDescription;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private WarrantyRequestStatus status = WarrantyRequestStatus.PENDING;
}
