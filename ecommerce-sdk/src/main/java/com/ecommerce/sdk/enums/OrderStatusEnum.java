package com.ecommerce.sdk.enums;

import com.ecommerce.sdk.exceptions.CountryNotFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum OrderStatusEnum {
    PENDING_INFO_CONFIRMATION,
    PENDING_PAYMENT,
    PROCESSED,
    DRY_RUN,
    CANCELED,
    REFUNDED,
    ERROR_EMPTY_CART,
    ERROR_INVALID_PRODUCT,
    ERROR_CORRUPTED_PRODUCT,
    ERROR_UNKOWN_BUYER;

    private static Map<String, OrderStatusEnum> mapOrderStatusStringTooEnum;

    static {
        mapOrderStatusStringTooEnum = new HashMap<>();
        Arrays.stream(values()).forEach(orderStatusEnum -> mapOrderStatusStringTooEnum.put(orderStatusEnum.name(), orderStatusEnum));
    }

    public static OrderStatusEnum getOrderStatusEnum(String orderStatus) throws CountryNotFoundException {
        OrderStatusEnum orderStatusEnum = mapOrderStatusStringTooEnum.get(orderStatus);
        if (orderStatusEnum == null) {
            throw new CountryNotFoundException();
        }
        return orderStatusEnum;
    }
}
