package com.datn.pc_store.modules.order.service;

import com.datn.pc_store.common.exception.BadRequestException;
import com.datn.pc_store.modules.goodsreceipt.entity.ProductSerial;
import com.datn.pc_store.modules.goodsreceipt.enums.ProductSerialStatus;
import com.datn.pc_store.modules.goodsreceipt.repository.ProductSerialRepository;
import com.datn.pc_store.modules.order.dto.OrderItemRequest;
import com.datn.pc_store.modules.order.entity.Order;
import com.datn.pc_store.modules.order.entity.OrderItemSerial;
import com.datn.pc_store.modules.order.repository.OrderItemSerialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class OrderSerialService {
    private final ProductSerialRepository inventory;
    private final OrderItemSerialRepository assignments;
    private final com.datn.pc_store.modules.order.repository.OrderItemRepository orderItems;

    public void reserve(Order order, List<OrderItemRequest> items) {
        Set<String> numbers = new HashSet<>();
        for (var item : items) {
            if (item.getSerialNumbers() == null || item.getSerialNumbers().size() != item.getQuantity()) {
                throw new BadRequestException("Phải nhập đủ một serial cho mỗi sản phẩm đặt mua.");
            }
            for (String number : item.getSerialNumbers()) {
                if (!numbers.add(number.trim())) {
                    throw new BadRequestException("Serial xuất hiện nhiều lần trong đơn: " + number);
                }
            }
        }
        Map<String, ProductSerial> byNumber = new HashMap<>();
        for (var serial : inventory.lockActiveBySerialNumbers(numbers)) {
            if (byNumber.put(serial.getSerialNumber(), serial) != null) {
                throw new BadRequestException("Có nhiều serial đang hoạt động cùng mã: " + serial.getSerialNumber());
            }
        }
        for (int i = 0; i < items.size(); i++) {
            var line = order.getItems().get(i);
            for (String number : items.get(i).getSerialNumbers()) {
                ProductSerial serial = byNumber.get(number.trim());
                if (serial == null || !serial.getProductId().equals(line.getProductId())
                        || serial.getStatus() != ProductSerialStatus.IN_STOCK) {
                    throw new BadRequestException("Serial không đúng sản phẩm hoặc không còn trong kho: " + number);
                }
                serial.setStatus(ProductSerialStatus.PACKING);
                assignments.save(OrderItemSerial.builder().orderItemId(line.getId())
                        .productSerialId(serial.getId()).productSerial(serial).build());
            }
        }
    }

    public void markDelivering(Long orderId) {
        for (var link : assignments.findAllByOrderId(orderId)) {
            link.getProductSerial().setStatus(ProductSerialStatus.DELIVERING);
        }
    }

    public void markSold(Long orderId) {
        var links = assignments.findAllByOrderId(orderId);
        for (var item : orderItems.findAllByOrderId(orderId)) {
            if (links.stream().filter(link -> link.getOrderItemId().equals(item.getId())).count() != item.getQuantity()) {
                throw new BadRequestException("Dòng hàng " + item.getId() + " chưa có đủ serial để hoàn tất và kích hoạt bảo hành.");
            }
        }
        for (var link : links) {
            link.getProductSerial().setStatus(ProductSerialStatus.SOLD);
        }
    }

    public void release(Long orderId) {
        for (var link : assignments.findAllByOrderId(orderId)) {
            var serial = link.getProductSerial();
            if (serial.getStatus() == ProductSerialStatus.PACKING || serial.getStatus() == ProductSerialStatus.DELIVERING) {
                serial.setStatus(ProductSerialStatus.IN_STOCK);
                assignments.delete(link);
            }
        }
    }
}
