package com.ecommerce.sdk.exceptions;

public class OrderAlreadyBeingPrcessedException extends EcommerceException {
    public OrderAlreadyBeingPrcessedException() {
    }

    public OrderAlreadyBeingPrcessedException(String message) {
        super(message);
    }

    public OrderAlreadyBeingPrcessedException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderAlreadyBeingPrcessedException(Throwable cause) {
        super(cause);
    }

    public OrderAlreadyBeingPrcessedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
