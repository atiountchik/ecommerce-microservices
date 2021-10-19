package com.ecommerce.sdk.domain;

import com.ecommerce.sdk.enums.CountryEnum;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class CartItemDetailsDTO {

    @NotNull
    private CartItemDTO item;
    @DecimalMin("0.01")
    private Double price;
    @DecimalMin("0.001")
    private Double weight;
    @NotNull
    private CountryEnum country;

    public CartItemDetailsDTO(CartItemDTO item, Double price, Double weight, CountryEnum country) {
        this.item = item;
        this.price = price;
        this.weight = weight;
        this.country = country;
    }

    public CartItemDetailsDTO() {
    }

    public CartItemDTO getItem() {
        return item;
    }

    public void setItem(CartItemDTO item) {
        this.item = item;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public CountryEnum getCountry() {
        return country;
    }

    public void setCountry(CountryEnum country) {
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemDetailsDTO that = (CartItemDetailsDTO) o;
        return Objects.equals(item, that.item) && Objects.equals(price, that.price) && Objects.equals(weight, that.weight) && country == that.country;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, price, weight, country);
    }
}
