package com.datn.pc_store.modules.delivery.handler;

import com.datn.pc_store.common.exception.BadRequestException;
import com.datn.pc_store.common.exception.ResourceNotFoundException;
import com.datn.pc_store.modules.delivery.enums.DeliveryResourceType;
import com.datn.pc_store.modules.delivery.enums.DeliveryStatus;
import com.datn.pc_store.modules.order.entity.Order;
import com.datn.pc_store.modules.order.enums.OrderStatus;
import com.datn.pc_store.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDeliveryHandler implements DeliveryResourceHandler {
    private final OrderRepository orderRepository;

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
        order.setStatus(status == DeliveryStatus.DELIVERED ? OrderStatus.COMPLETED : OrderStatus.CANCELLED);
    }

    private Order lockOrder(Long id) {
        return orderRepository.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", id));
    }
}
