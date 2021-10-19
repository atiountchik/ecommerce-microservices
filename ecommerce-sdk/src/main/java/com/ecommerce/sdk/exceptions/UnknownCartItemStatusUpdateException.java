package com.ecommerce.sdk.exceptions;

public class UnknownCartItemStatusUpdateException extends EcommerceException {
    public UnknownCartItemStatusUpdateException() {
    }

    public UnknownCartItemStatusUpdateException(String message) {
        super(message);
    }

    public UnknownCartItemStatusUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownCartItemStatusUpdateException(Throwable cause) {
        super(cause);
    }

    public UnknownCartItemStatusUpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
