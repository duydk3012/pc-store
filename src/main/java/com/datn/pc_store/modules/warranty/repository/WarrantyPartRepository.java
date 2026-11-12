package com.datn.pc_store.modules.warranty.repository;

import com.datn.pc_store.modules.warranty.entity.WarrantyPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface WarrantyPartRepository extends JpaRepository<WarrantyPart, Long> {
    List<WarrantyPart> findByWarrantyRequestId(Long warrantyRequestId);
    List<WarrantyPart> findByWarrantyRequestIdIn(List<Long> warrantyRequestIds);

    @Query("select p from WarrantyPart p order by p.createdDate desc")
    List<WarrantyPart> findAllOrderByCreatedDateDesc();
}

