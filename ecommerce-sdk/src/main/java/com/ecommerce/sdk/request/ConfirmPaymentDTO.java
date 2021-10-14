package com.ecommerce.sdk.request;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

public class ConfirmPaymentDTO {
    @NotBlank
    private String cardNumber;
    @Min(1)
    @Max(12)
    private Integer expMonth;
    @Min(2021)
    private Integer expYear;
    @Min(100)
    @Max(999)
    private Integer cvc;
    @DecimalMin("0.01")
    private Double amount;

    public ConfirmPaymentDTO(String cardNumber, Integer expMonth, Integer expYear, Integer cvc, Double amount) {
        this.cardNumber = cardNumber;
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.cvc = cvc;
        this.amount = amount;
    }

    public ConfirmPaymentDTO() {
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Integer getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(Integer expMonth) {
        this.expMonth = expMonth;
    }

    public Integer getExpYear() {
        return expYear;
    }

    public void setExpYear(Integer expYear) {
        this.expYear = expYear;
    }

    public Integer getCvc() {
        return cvc;
    }

    public void setCvc(Integer cvc) {
        this.cvc = cvc;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfirmPaymentDTO that = (ConfirmPaymentDTO) o;
        return Objects.equals(cardNumber, that.cardNumber) && Objects.equals(expMonth, that.expMonth) && Objects.equals(expYear, that.expYear) && Objects.equals(cvc, that.cvc) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNumber, expMonth, expYear, cvc, amount);
    }
}
