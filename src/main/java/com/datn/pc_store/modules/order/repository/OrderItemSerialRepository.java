package com.datn.pc_store.modules.order.repository;

import com.datn.pc_store.modules.order.entity.OrderItemSerial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemSerialRepository extends JpaRepository<OrderItemSerial, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from OrderItemSerial s where s.id = (select w.orderItemSerialId from Warranty w where w.id = :warrantyId)")
    java.util.Optional<OrderItemSerial> lockForWarranty(Long warrantyId);

    List<OrderItemSerial> findAllByOrderItemId(Long orderItemId);

    @Query("SELECT ois FROM OrderItemSerial ois JOIN OrderItem oi ON oi.id = ois.orderItemId WHERE oi.order.id = :orderId")
    List<OrderItemSerial> findAllByOrderId(@Param("orderId") Long orderId);
}

