package com.ecommerce.sdk.exceptions;

public class ProductDoesNotExistException extends EcommerceException {
    public ProductDoesNotExistException() {
    }

    public ProductDoesNotExistException(String message) {
        super(message);
    }

    public ProductDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductDoesNotExistException(Throwable cause) {
        super(cause);
    }

    public ProductDoesNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
