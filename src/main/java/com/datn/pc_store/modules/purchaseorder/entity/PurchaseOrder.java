package com.datn.pc_store.modules.purchaseorder.entity;

import com.datn.pc_store.common.entity.BaseEntity;
import com.datn.pc_store.modules.purchaseorder.enums.PurchaseOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE purchase_orders SET is_deleted = 1 WHERE id = ?")
@SQLRestriction("is_deleted = 0")
public class PurchaseOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 100)
    private String name;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "order_date")
    private Instant orderDate;

    @Column(name = "expected_date")
    private Instant expectedDate;

    @Column(nullable = false)
    @Builder.Default
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
}
