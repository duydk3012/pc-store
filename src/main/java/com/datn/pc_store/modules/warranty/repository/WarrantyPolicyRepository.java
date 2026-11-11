package com.datn.pc_store.modules.warranty.repository;
import com.datn.pc_store.modules.warranty.entity.WarrantPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface WarrantyPolicyRepository extends JpaRepository<WarrantPolicy, Long>, JpaSpecificationExecutor<WarrantPolicy> {}
