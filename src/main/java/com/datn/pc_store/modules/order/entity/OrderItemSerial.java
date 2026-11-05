package com.datn.pc_store.modules.order.entity;

import com.datn.pc_store.modules.goodsreceipt.entity.ProductSerial;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "order_item_serial")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemSerial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "product_serial_id", nullable = false, unique = true)
    private Long productSerialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_serial_id", insertable = false, updatable = false)
    private ProductSerial productSerial;

    @CreatedDate
    @Column(name = "created_date", updatable = false, nullable = false)
    private Instant createdDate;
}

