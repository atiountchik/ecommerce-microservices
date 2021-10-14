package com.ecommerce.sdk.exceptions;

public class CorruptedProductException extends EcommerceException {
    public CorruptedProductException() {
    }

    public CorruptedProductException(String message) {
        super(message);
    }

    public CorruptedProductException(String message, Throwable cause) {
        super(message, cause);
    }

    public CorruptedProductException(Throwable cause) {
        super(cause);
    }

    public CorruptedProductException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
