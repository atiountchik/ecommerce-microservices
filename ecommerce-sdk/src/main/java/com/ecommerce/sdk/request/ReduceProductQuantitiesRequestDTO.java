package com.ecommerce.sdk.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

public class ReduceProductQuantitiesRequestDTO {
    @NotNull
    private UUID sku;
    @Min(1)
    private Integer quantity;

    public ReduceProductQuantitiesRequestDTO(UUID sku, Integer quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

    public ReduceProductQuantitiesRequestDTO() {
    }

    public UUID getSku() {
        return sku;
    }

    public void setSku(UUID sku) {
        this.sku = sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReduceProductQuantitiesRequestDTO that = (ReduceProductQuantitiesRequestDTO) o;
        return Objects.equals(sku, that.sku) && Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, quantity);
    }
}
