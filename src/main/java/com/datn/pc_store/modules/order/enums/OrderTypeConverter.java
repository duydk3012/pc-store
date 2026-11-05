package com.datn.pc_store.modules.order.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderTypeConverter implements AttributeConverter<OrderType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(OrderType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public OrderType convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : OrderType.fromValue(dbData);
    }
}

