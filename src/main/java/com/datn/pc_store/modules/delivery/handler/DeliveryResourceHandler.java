package com.datn.pc_store.modules.delivery.handler;

import com.datn.pc_store.modules.delivery.enums.DeliveryResourceType;
import com.datn.pc_store.modules.delivery.enums.DeliveryStatus;

public interface DeliveryResourceHandler {
    DeliveryResourceType getResourceType();

    /** Locks the source document and puts it in its shipping state for this transaction. */
    void startDelivery(Long resourceId);

    String getResourceCode(Long resourceId);

    void onTerminalStatus(Long resourceId, DeliveryStatus status);
}
