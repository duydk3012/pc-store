package com.datn.pc_store.modules.warranty.service;

import com.datn.pc_store.modules.order.entity.Order;
import com.datn.pc_store.modules.order.enums.OrderStatus;
import com.datn.pc_store.modules.warranty.entity.Warranty;
import com.datn.pc_store.modules.warranty.repository.WarrantyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class WarrantyActivationService {
    private final WarrantyRepository warranties;

    /** Called while delivery completion holds the order's write lock. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void activateForCompletedOrder(Order order) {
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("Chỉ kích hoạt bảo hành khi đơn đã hoàn tất.");
        }
        Instant start = Instant.now();
        for (var match : warranties.findUnissuedPolicies(order.getId())) {
            var warranty = new Warranty();
            warranty.setOrderItemSerialId(match.getOrderItemSerialId());
            warranty.setWarrantyPolicyId(match.getPolicyId());
            warranty.setStartDate(start);
            warranty.setEndDate(start.plus(match.getDurationDays(), ChronoUnit.DAYS));
            warranties.save(warranty);
        }
    }
}
