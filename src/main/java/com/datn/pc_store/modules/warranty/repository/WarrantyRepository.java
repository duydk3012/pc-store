package com.datn.pc_store.modules.warranty.repository;

import com.datn.pc_store.modules.warranty.dto.WarrantyResponse;
import com.datn.pc_store.modules.warranty.entity.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface WarrantyRepository extends JpaRepository<Warranty, Long> {
    interface PolicyMatch {
        Long getOrderItemSerialId();
        Long getPolicyId();
        int getDurationDays();
    }

    @Query(value = """
        select s.id as orderItemSerialId, wp.id as policyId, wp.duration_days as durationDays
        from order_items oi
        join order_item_serial s on s.order_item_id = oi.id
        join products p on p.id = oi.product_id
        join warranty_policy wp on wp.is_deleted = 0 and wp.status = 1 and (
            wp.target_type is null
            or (wp.target_type = 0 and exists (
                select 1 from category_products cp where cp.product_id = p.id and cp.category_id = wp.target_id))
            or (wp.target_type = 1 and wp.target_id = p.id)
            or (wp.target_type = 2 and wp.target_id = p.brand_id))
        where oi.order_id = :orderId and not exists (
            select 1 from warranty w where w.order_item_serial_id = s.id and w.warranty_policy_id = wp.id)
        """, nativeQuery = true)
    List<PolicyMatch> findUnissuedPolicies(Long orderId);

    @Query("""
        select new com.datn.pc_store.modules.warranty.dto.WarrantyResponse(
            w.id, oi.id, s.productSerialId, ps.serialNumber, w.warrantyPolicyId, w.startDate, w.endDate, w.status)
        from Warranty w join OrderItemSerial s on s.id = w.orderItemSerialId
        join s.productSerial ps join OrderItem oi on oi.id = s.orderItemId
        where oi.order.id = :orderId order by oi.id, s.id, w.warrantyPolicyId
        """)
    List<WarrantyResponse> findForOrder(Long orderId);
}
