package com.datn.pc_store.modules.order.entity;

import com.datn.pc_store.common.entity.BaseEntity;
import com.datn.pc_store.modules.order.enums.OrderStatus;
import com.datn.pc_store.modules.order.enums.OrderType;
import com.datn.pc_store.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private OrderType type = OrderType.ONLINE;

    @Column(name = "total_amount", nullable = false)
    @Builder.Default
    private long totalAmount = 0L;

    @Column(name = "discount_amount", nullable = false)
    @Builder.Default
    private long discountAmount = 0L;

    @Column(name = "final_amount", nullable = false)
    @Builder.Default
    private long finalAmount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(length = 500)
    private String address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}

