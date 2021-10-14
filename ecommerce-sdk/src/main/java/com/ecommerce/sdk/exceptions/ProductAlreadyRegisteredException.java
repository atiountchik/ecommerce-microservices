package com.ecommerce.sdk.exceptions;

public class ProductAlreadyRegisteredException extends EcommerceException {
    public ProductAlreadyRegisteredException() {
    }

    public ProductAlreadyRegisteredException(String message) {
        super(message);
    }

    public ProductAlreadyRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductAlreadyRegisteredException(Throwable cause) {
        super(cause);
    }

    public ProductAlreadyRegisteredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
