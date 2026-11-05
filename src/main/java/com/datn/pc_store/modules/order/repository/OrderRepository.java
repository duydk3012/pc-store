package com.datn.pc_store.modules.order.repository;

import com.datn.pc_store.modules.order.entity.Order;
import com.datn.pc_store.modules.order.enums.OrderStatus;
import com.datn.pc_store.modules.order.enums.OrderType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByCode(String code);

    boolean existsByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findForUpdate(@Param("id") Long id);

    @Query("""
        SELECT o FROM Order o
        LEFT JOIN o.user u
        WHERE (:keyword IS NULL OR :keyword = ''
            OR LOWER(o.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(o.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR (u.phoneNumber IS NOT NULL AND u.phoneNumber LIKE CONCAT('%', :keyword, '%')))
          AND (:status IS NULL OR o.status = :status)
          AND (:type IS NULL OR o.type = :type)
          AND (:userId IS NULL OR o.userId = :userId)
          AND (:fromDate IS NULL OR o.createdDate >= :fromDate)
          AND (:toDate IS NULL OR o.createdDate <= :toDate)
        """)
    Page<Order> searchOrders(@Param("keyword") String keyword,
                            @Param("status") OrderStatus status,
                            @Param("type") OrderType type,
                            @Param("userId") Long userId,
                            @Param("fromDate") Instant fromDate,
                            @Param("toDate") Instant toDate,
                            Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM invoices WHERE order_id = :id", nativeQuery = true)
    long countInvoices(@Param("id") Long id);

    @Query(value = "SELECT COUNT(*) FROM payment WHERE order_id = :id", nativeQuery = true)
    long countPayments(@Param("id") Long id);
}

