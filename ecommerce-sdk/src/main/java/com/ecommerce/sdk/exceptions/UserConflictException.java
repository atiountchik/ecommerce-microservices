package com.ecommerce.sdk.exceptions;

public class UserConflictException extends EcommerceException {
    public UserConflictException() {
    }

    public UserConflictException(String message) {
        super(message);
    }

    public UserConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserConflictException(Throwable cause) {
        super(cause);
    }

    public UserConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
