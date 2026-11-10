package com.datn.pc_store.modules.delivery.handler;

import com.datn.pc_store.common.exception.BadRequestException;
import com.datn.pc_store.common.exception.ResourceNotFoundException;
import com.datn.pc_store.modules.delivery.enums.DeliveryResourceType;
import com.datn.pc_store.modules.delivery.enums.DeliveryStatus;
import com.datn.pc_store.modules.goodsreceipt.enums.ProductSerialStatus;
import com.datn.pc_store.modules.purchasereturn.entity.PurchaseReturn;
import com.datn.pc_store.modules.purchasereturn.entity.PurchaseReturnItem;
import com.datn.pc_store.modules.purchasereturn.enums.PurchaseReturnStatus;
import com.datn.pc_store.modules.purchasereturn.repository.PurchaseReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseReturnDeliveryHandler implements DeliveryResourceHandler {
    private final PurchaseReturnRepository purchaseReturnRepository;

    @Override
    public DeliveryResourceType getResourceType() {
        return DeliveryResourceType.PURCHASE_RETURN;
    }

    @Override
    public void startDelivery(Long resourceId) {
        PurchaseReturn purchaseReturn = lockReturn(resourceId);
        if (purchaseReturn.getStatus() == PurchaseReturnStatus.PENDING) {
            purchaseReturn.startDelivery();
        } else if (purchaseReturn.getStatus() != PurchaseReturnStatus.DELIVERING) {
            throw new BadRequestException("Chỉ có thể lập vận đơn cho phiếu hoàn PENDING hoặc DELIVERING.");
        }
    }

    @Override
    public String getResourceCode(Long resourceId) {
        return purchaseReturnRepository.findById(resourceId).map(PurchaseReturn::getCode).orElse(null);
    }

    @Override
    public void onTerminalStatus(Long resourceId, DeliveryStatus status) {
        PurchaseReturn purchaseReturn = lockReturn(resourceId);
        if (purchaseReturn.getStatus() != PurchaseReturnStatus.DELIVERING) {
            throw new BadRequestException("Phiếu hoàn trả phải ở trạng thái DELIVERING để kết thúc giao hàng.");
        }

        boolean delivered = status == DeliveryStatus.DELIVERED;
        purchaseReturn.setStatus(delivered ? PurchaseReturnStatus.RETURNED : PurchaseReturnStatus.CANCELLED);
        for (PurchaseReturnItem item : purchaseReturn.getItems()) {
            if (item.getProductSerial() != null) {
                item.getProductSerial().setStatus(delivered
                        ? ProductSerialStatus.RETURNED
                        : ProductSerialStatus.DEFECTIVE);
                item.getProductSerial().setTerminated(delivered);
            }
        }
    }

    private PurchaseReturn lockReturn(Long id) {
        return purchaseReturnRepository.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu hoàn trả", "id", id));
    }
}
