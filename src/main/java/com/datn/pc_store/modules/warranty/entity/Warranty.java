package com.datn.pc_store.modules.warranty.entity;

import com.datn.pc_store.common.entity.BaseEntity;
import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "warranty")
@Getter
@Setter
public class Warranty extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_item_serial_id")
    private Long orderItemSerialId;

    @Column(nullable = false)
    private Long warrantyPolicyId;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    @Column(nullable = false)
    private boolean status = true;

    @Column(length = 500)
    private String note;
}
