package com.ecommerce.sdk.exceptions;

public class CartItemDoesNotExistException extends EcommerceException {
    public CartItemDoesNotExistException() {
    }

    public CartItemDoesNotExistException(String message) {
        super(message);
    }

    public CartItemDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public CartItemDoesNotExistException(Throwable cause) {
        super(cause);
    }

    public CartItemDoesNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
