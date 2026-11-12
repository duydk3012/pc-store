package com.datn.pc_store.modules.warranty.repository;

import com.datn.pc_store.modules.warranty.entity.WarrantyRequest;
import com.datn.pc_store.modules.warranty.enums.WarrantyRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WarrantyRequestRepository extends JpaRepository<WarrantyRequest, Long> {
    @Query("""
        select count(r) > 0 from WarrantyRequest r join Warranty w on r.warrantyId = w.id
        where w.orderItemSerialId = :soldSerialId and r.status in :statuses
        """)
    boolean existsForSerial(@Param("soldSerialId") Long soldSerialId, @Param("statuses") Collection<WarrantyRequestStatus> statuses);

    List<WarrantyRequest> findByWarrantyId(Long warrantyId);

    List<WarrantyRequest> findByWarrantyIdIn(Collection<Long> warrantyIds);

    long countByWarrantyId(Long warrantyId);

    Optional<WarrantyRequest> findByRequestCode(String requestCode);

    @Query("""
        select r from WarrantyRequest r join Warranty w on w.id = r.warrantyId
        join OrderItemSerial s on s.id = w.orderItemSerialId
        join s.productSerial ps
        where lower(ps.serialNumber) = lower(:serialNumber)
        order by r.createdDate desc
        """)
    List<WarrantyRequest> findBySerialNumber(@Param("serialNumber") String serialNumber);
}
