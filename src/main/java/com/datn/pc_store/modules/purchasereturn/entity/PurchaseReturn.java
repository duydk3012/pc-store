package com.datn.pc_store.modules.purchasereturn.entity;

import com.datn.pc_store.common.entity.BaseEntity;
import com.datn.pc_store.modules.goodsreceipt.enums.ProductSerialStatus;
import com.datn.pc_store.modules.purchasereturn.enums.PurchaseReturnStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturn extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 255)
    private String name;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(length = 255)
    private String reason;

    @Column(length = 500)
    private String description;

    @Column(name = "return_date")
    private Instant returnDate;

    @Column(name = "total_amount", nullable = false)
    @Builder.Default
    private long totalAmount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private PurchaseReturnStatus status = PurchaseReturnStatus.PENDING;

    @OneToMany(mappedBy = "purchaseReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<PurchaseReturnItem> items = new ArrayList<>();

    public void startDelivery() {
        status = PurchaseReturnStatus.DELIVERING;
        if (returnDate == null) returnDate = Instant.now();
        for (PurchaseReturnItem item : items) {
            if (item.getProductSerial() != null) {
                item.getProductSerial().setStatus(ProductSerialStatus.RETURNING);
            }
        }
    }
}
