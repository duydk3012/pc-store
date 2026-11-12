package com.datn.pc_store.modules.warranty.entity;

import com.datn.pc_store.common.entity.BaseEntity;
import com.datn.pc_store.modules.warranty.enums.WarrantyRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

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

    @Column(name = "technician_id")
    private Long technicianId;

    @Column(length = 500)
    private String issueDescription;

    @Column(length = 500)
    private String inspectionNotes;

    @Column(length = 500)
    private String solution;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private WarrantyRequestStatus status = WarrantyRequestStatus.PENDING;

    @Column(name = "completed_at")
    private Instant completedAt;
}
