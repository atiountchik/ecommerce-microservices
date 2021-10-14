package com.ecommerce.sdk.exceptions;

public class CartIsEmptyForBuyerException extends EcommerceException {
    public CartIsEmptyForBuyerException() {
    }

    public CartIsEmptyForBuyerException(String message) {
        super(message);
    }

    public CartIsEmptyForBuyerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CartIsEmptyForBuyerException(Throwable cause) {
        super(cause);
    }

    public CartIsEmptyForBuyerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
