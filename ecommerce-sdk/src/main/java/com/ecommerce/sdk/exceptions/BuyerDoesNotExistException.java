package com.ecommerce.sdk.exceptions;

public class BuyerDoesNotExistException extends EcommerceException {
    public BuyerDoesNotExistException() {
    }

    public BuyerDoesNotExistException(String message) {
        super(message);
    }

    public BuyerDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuyerDoesNotExistException(Throwable cause) {
        super(cause);
    }

    public BuyerDoesNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
