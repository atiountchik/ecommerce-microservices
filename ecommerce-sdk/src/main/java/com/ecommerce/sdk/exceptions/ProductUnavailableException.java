package com.ecommerce.sdk.exceptions;

public class ProductUnavailableException extends EcommerceException {
    public ProductUnavailableException() {
    }

    public ProductUnavailableException(String message) {
        super(message);
    }

    public ProductUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductUnavailableException(Throwable cause) {
        super(cause);
    }

    public ProductUnavailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
