package com.ecommerce.sdk.exceptions;

public class OrderStatusNotFoundException extends EcommerceException {
    public OrderStatusNotFoundException() {
    }

    public OrderStatusNotFoundException(String message) {
        super(message);
    }

    public OrderStatusNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderStatusNotFoundException(Throwable cause) {
        super(cause);
    }

    public OrderStatusNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
