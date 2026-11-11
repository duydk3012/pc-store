package com.datn.pc_store.modules.delivery.repository;

import com.datn.pc_store.modules.delivery.entity.Delivery;
import com.datn.pc_store.modules.delivery.enums.DeliveryResourceType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long>, JpaSpecificationExecutor<Delivery> {
    boolean existsByResourceTypeAndResourceId(DeliveryResourceType resourceType, Long resourceId);

    Optional<Delivery> findFirstByResourceTypeAndResourceIdOrderByIdDesc(DeliveryResourceType resourceType, Long resourceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Delivery d WHERE d.id = :id")
    Optional<Delivery> findForUpdate(@Param("id") Long id);
}
