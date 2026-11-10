package com.datn.pc_store.modules.delivery.handler;

import com.datn.pc_store.common.exception.BadRequestException;
import com.datn.pc_store.modules.delivery.enums.DeliveryResourceType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class DeliveryResourceHandlerFactory {
    private final Map<DeliveryResourceType, DeliveryResourceHandler> handlers;

    public DeliveryResourceHandlerFactory(List<DeliveryResourceHandler> handlers) {
        this.handlers = new EnumMap<>(DeliveryResourceType.class);
        for (DeliveryResourceHandler handler : handlers) {
            this.handlers.put(handler.getResourceType(), handler);
        }
    }

    public DeliveryResourceHandler get(DeliveryResourceType resourceType) {
        DeliveryResourceHandler handler = handlers.get(resourceType);
        if (handler == null) {
            throw new BadRequestException("Nguồn giao hàng không được hỗ trợ: " + resourceType);
        }
        return handler;
    }
}
