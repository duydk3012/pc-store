package com.datn.pc_store.modules.warranty.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "warranty_part_serial")
@Getter
@Setter
public class WarrantyPartSerial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long warrantyPartId;

    @Column(nullable = false)
    private Long productSerialId;
}
