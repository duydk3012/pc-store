package com.datn.pc_store.modules.order.mapper;

import com.datn.pc_store.modules.order.dto.OrderItemResponse;
import com.datn.pc_store.modules.order.dto.OrderResponse;
import com.datn.pc_store.modules.order.entity.Order;
import com.datn.pc_store.modules.order.entity.OrderItem;
import com.datn.pc_store.modules.order.util.OrderAddressHelper;
import com.datn.pc_store.modules.product.entity.Product;
import com.datn.pc_store.modules.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return toResponse(order, Collections.emptyMap());
    }

    public OrderResponse toResponse(Order order, Map<Long, List<String>> itemSerialMap) {
        if (order == null) return null;

        User user = order.getUser();
        String customerUsername = user != null ? user.getUsername() : null;
        String customerName = user != null ? user.getFullName() : null;
        String customerEmail = user != null ? user.getEmail() : null;
        String customerPhone = user != null ? user.getPhoneNumber() : null;

        OrderAddressHelper.DecodedAddress decoded = OrderAddressHelper.decodeAddress(order.getAddress());
        String recipientName = decoded.recipientName() != null ? decoded.recipientName() : customerName;
        String recipientPhone = decoded.recipientPhone() != null ? decoded.recipientPhone() : customerPhone;
        String address = decoded.address();
        String note = decoded.note();

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                List<String> serials = itemSerialMap != null ? itemSerialMap.get(item.getId()) : null;
                itemResponses.add(toItemResponse(item, serials));
            }
        }

        return OrderResponse.builder()
                .id(order.getId())
                .code(order.getCode())
                .userId(order.getUserId())
                .customerUsername(customerUsername)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .customerPhone(customerPhone)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .address(address)
                .note(note)
                .type(order.getType())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .status(order.getStatus())
                .items(itemResponses)
                .createdDate(order.getCreatedDate())
                .createdBy(order.getCreatedBy())
                .lastModifiedDate(order.getLastModifiedDate())
                .lastModifiedBy(order.getLastModifiedBy())
                .build();
    }

    public OrderItemResponse toItemResponse(OrderItem item, List<String> serials) {
        if (item == null) return null;

        Product product = item.getProduct();
        String productCode = product != null ? product.getCode() : null;
        String productName = product != null ? product.getName() : null;
        String productImageUrl = product != null ? product.getImageUrl() : null;

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productCode(productCode)
                .productName(productName)
                .productImageUrl(productImageUrl)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .subtotal(item.getSubtotal())
                .serialNumbers(serials != null ? serials : Collections.emptyList())
                .createdDate(item.getCreatedDate())
                .lastModifiedDate(item.getLastModifiedDate())
                .build();
    }
}

