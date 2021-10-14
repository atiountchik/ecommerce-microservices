package com.ecommerce.sdk.domain;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

public class CartItemDTO {

    @NotNull
    private UUID sku;
    @DecimalMin("0.01")
    private Double price;
    @Min(1)
    private Integer quantity;
    @DecimalMin("0.001")
    private Double weight;

    public CartItemDTO(UUID sku, Double price, Integer quantity, Double weight) {
        this.sku = sku;
        this.price = price;
        this.quantity = quantity;
        this.weight = weight;
    }

    public CartItemDTO() {
    }

    public UUID getSku() {
        return sku;
    }

    public void setSku(UUID sku) {
        this.sku = sku;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemDTO that = (CartItemDTO) o;
        return Objects.equals(sku, that.sku) && Objects.equals(price, that.price) && Objects.equals(quantity, that.quantity) && Objects.equals(weight, that.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, price, quantity, weight);
    }
}
