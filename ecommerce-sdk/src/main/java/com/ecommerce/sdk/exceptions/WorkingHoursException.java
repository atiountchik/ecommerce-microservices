package com.ecommerce.sdk.exceptions;

public class WorkingHoursException extends EcommerceException {
    public WorkingHoursException() {
    }

    public WorkingHoursException(String message) {
        super(message);
    }

    public WorkingHoursException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkingHoursException(Throwable cause) {
        super(cause);
    }

    public WorkingHoursException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
