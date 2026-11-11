package com.datn.pc_store.modules.warranty.repository;

import com.datn.pc_store.modules.warranty.entity.WarrantyRequest;
import com.datn.pc_store.modules.warranty.enums.WarrantyRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Collection;

public interface WarrantyRequestRepository extends JpaRepository<WarrantyRequest, Long> {
    @Query("""
        select count(r) > 0 from WarrantyRequest r join Warranty w on r.warrantyId = w.id
        where w.orderItemSerialId = :soldSerialId and r.status in :statuses
        """)
    boolean existsForSerial(Long soldSerialId, Collection<WarrantyRequestStatus> statuses);
}
