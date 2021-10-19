package com.ecommerce.sdk.request;

import com.ecommerce.sdk.enums.CountryEnum;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

public class PaymentRequestDTO {

    @DecimalMin("0.01")
    private Double amount;
    @NotNull
    private CountryEnum country;
    @NotNull
    private UUID statusUuid;
    @NotNull
    private UUID buyerId;

    public PaymentRequestDTO(Double amount, CountryEnum country, UUID statusUuid, UUID buyerId) {
        this.amount = amount;
        this.country = country;
        this.statusUuid = statusUuid;
        this.buyerId = buyerId;
    }

    public PaymentRequestDTO() {
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public CountryEnum getCountry() {
        return country;
    }

    public void setCountry(CountryEnum country) {
        this.country = country;
    }

    public UUID getStatusUuid() {
        return statusUuid;
    }

    public void setStatusUuid(UUID statusUuid) {
        this.statusUuid = statusUuid;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentRequestDTO that = (PaymentRequestDTO) o;
        return Objects.equals(amount, that.amount) && Objects.equals(country, that.country) && Objects.equals(statusUuid, that.statusUuid) && Objects.equals(buyerId, that.buyerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, country, statusUuid, buyerId);
    }
}
