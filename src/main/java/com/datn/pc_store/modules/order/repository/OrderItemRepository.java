package com.datn.pc_store.modules.order.repository;

import com.datn.pc_store.modules.order.entity.OrderItem;
import com.datn.pc_store.modules.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findAllByOrderId(Long orderId);

    boolean existsByProductIdAndOrder_UserIdAndOrder_Status(Long productId, Long userId, OrderStatus status);
}

