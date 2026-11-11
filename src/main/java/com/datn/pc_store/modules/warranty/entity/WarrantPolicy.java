package com.datn.pc_store.modules.warranty.entity;

import com.datn.pc_store.common.entity.BaseEntity;
import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyStatus;
import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyTargetType;
import com.datn.pc_store.modules.warranty.enums.WarrantyPolicyTargetTypeConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "warranty_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE warranty_policy SET is_deleted = 1 WHERE id = ?")
@SQLRestriction("is_deleted = 0")
public class WarrantPolicy extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private int durationDays;

    @Column(length = 500)
    private String description;

    @Convert(converter = WarrantyPolicyTargetTypeConverter.class)
    private WarrantyPolicyTargetType targetType;

    private Long targetId;

    @Column(nullable = false)
    @Builder.Default
    private WarrantyPolicyStatus status = WarrantyPolicyStatus.ACTIVE;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
}
