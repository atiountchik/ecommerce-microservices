package com.ecommerce.sdk.request;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

public class ClearCartRequestDTO {
    @NotNull
    private UUID buyerId;

    public ClearCartRequestDTO(UUID buyerId) {
        this.buyerId = buyerId;
    }

    public ClearCartRequestDTO() {
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
        ClearCartRequestDTO that = (ClearCartRequestDTO) o;
        return Objects.equals(buyerId, that.buyerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buyerId);
    }
}
