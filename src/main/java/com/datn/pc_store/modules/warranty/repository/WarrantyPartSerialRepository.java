package com.datn.pc_store.modules.warranty.repository;

import com.datn.pc_store.modules.warranty.entity.WarrantyPartSerial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WarrantyPartSerialRepository extends JpaRepository<WarrantyPartSerial, Long> {
    List<WarrantyPartSerial> findByWarrantyPartId(Long warrantyPartId);
    List<WarrantyPartSerial> findByWarrantyPartIdIn(List<Long> warrantyPartIds);
}

