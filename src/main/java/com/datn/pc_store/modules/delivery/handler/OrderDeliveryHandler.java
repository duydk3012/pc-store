package com.datn.pc_store.modules.delivery.handler;

import com.datn.pc_store.common.exception.BadRequestException;
import com.datn.pc_store.common.exception.ResourceNotFoundException;
import com.datn.pc_store.modules.delivery.enums.DeliveryResourceType;
import com.datn.pc_store.modules.delivery.enums.DeliveryStatus;
import com.datn.pc_store.modules.order.entity.Order;
import com.datn.pc_store.modules.order.enums.OrderStatus;
import com.datn.pc_store.modules.order.repository.OrderRepository;
import com.datn.pc_store.modules.order.service.OrderSerialService;
import com.datn.pc_store.modules.warranty.service.WarrantyActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDeliveryHandler implements DeliveryResourceHandler {
    private final OrderRepository orderRepository;
    private final OrderSerialService orderSerialService;
    private final WarrantyActivationService warrantyActivationService;

    @Override
    public DeliveryResourceType getResourceType() {
        return DeliveryResourceType.ORDER;
    }

    @Override
    public void startDelivery(Long resourceId) {
        Order order = lockOrder(resourceId);
        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PROCESSING) {
            throw new BadRequestException("Chỉ có thể lập vận đơn cho đơn hàng CONFIRMED hoặc PROCESSING.");
        }
        order.setStatus(OrderStatus.PROCESSING);
        orderSerialService.markDelivering(resourceId);
    }

    @Override
    public String getResourceCode(Long resourceId) {
        return orderRepository.findById(resourceId).map(Order::getCode).orElse(null);
    }

    @Override
    public void onTerminalStatus(Long resourceId, DeliveryStatus status) {
        Order order = lockOrder(resourceId);
        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new BadRequestException("Đơn hàng phải ở trạng thái PROCESSING để kết thúc giao hàng.");
        }
        if (status == DeliveryStatus.DELIVERED) {
            order.setStatus(OrderStatus.COMPLETED);
            orderSerialService.markSold(resourceId);
            warrantyActivationService.activateForCompletedOrder(order);
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            orderSerialService.release(resourceId);
        }
    }

    private Order lockOrder(Long id) {
        return orderRepository.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", id));
    }
}
