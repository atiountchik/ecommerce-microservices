package com.ecommerce.sdk.request;

import com.ecommerce.sdk.enums.CountryEnum;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class RegisterNewProductRequestDTO {

    @NotBlank
    private String name;
    @NotNull
    @DecimalMin("0.01")
    private Double price;
    @DecimalMin("0.001")
    private Double weight;
    @Min(1)
    private Integer stockAmount;
    private CountryEnum country;

    public RegisterNewProductRequestDTO(String name, Double price, Double weight, Integer stockAmount, CountryEnum country) {
        this.name = name;
        this.price = price;
        this.weight = weight;
        this.stockAmount = stockAmount;
        this.country = country;
    }

    public RegisterNewProductRequestDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStockAmount() {
        return stockAmount;
    }

    public void setStockAmount(Integer stockAmount) {
        this.stockAmount = stockAmount;
    }

    public CountryEnum getCountry() {
        return country;
    }

    public void setCountry(CountryEnum country) {
        this.country = country;
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
        RegisterNewProductRequestDTO that = (RegisterNewProductRequestDTO) o;
        return Objects.equals(name, that.name) && Objects.equals(price, that.price) && Objects.equals(weight, that.weight) && Objects.equals(stockAmount, that.stockAmount) && country == that.country;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, weight, stockAmount, country);
    }
}
