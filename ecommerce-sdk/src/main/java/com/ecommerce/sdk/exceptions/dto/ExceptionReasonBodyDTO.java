package com.ecommerce.sdk.exceptions.dto;

import java.util.Objects;

public class ExceptionReasonBodyDTO {
    private String errorMessageId;

    public ExceptionReasonBodyDTO(String errorMessageId) {
        this.errorMessageId = errorMessageId;
    }

    public ExceptionReasonBodyDTO() {
    }

    public String getErrorMessageId() {
        return errorMessageId;
    }

    public void setErrorMessageId(String errorMessageId) {
        this.errorMessageId = errorMessageId;
    }

    @Override
    public String toString() {
        return "ExceptionDTO{" +
                "errorMessageId='" + errorMessageId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExceptionReasonBodyDTO that = (ExceptionReasonBodyDTO) o;
        return Objects.equals(errorMessageId, that.errorMessageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorMessageId);
    }
}
