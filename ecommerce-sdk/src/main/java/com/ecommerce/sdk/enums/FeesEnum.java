package com.ecommerce.sdk.enums;

public enum FeesEnum {
    NATIONAL_STANDARD_FEE(2),
    NATIONAL_PER_KILO_FEE(1),
    INTERNATIONAL_STANDARD_FEE(5),
    INTERNATIONAL_PER_KILO_FEE(2);

    private final Integer fee;

    FeesEnum(Integer fee) {
        this.fee = fee;
    }

    public Integer getFee() {
        return fee;
    }
}
