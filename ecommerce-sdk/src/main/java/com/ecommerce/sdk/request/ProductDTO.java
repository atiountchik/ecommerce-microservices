package com.ecommerce.sdk.request;

import com.ecommerce.sdk.enums.CountryEnum;

import javax.validation.constraints.*;
import java.util.Objects;

public class ProductDTO {

    @Min(1)
    private Long id;

    @Size(max = 200)
    private String name;

    @DecimalMin("0.01")
    private Double price;

    @DecimalMin("0.001")
    private Double weight;

    @Min(0)
    private Integer stockAmount;

    @NotNull
    private CountryEnum country;

    public ProductDTO(Long id, String name, Double price, Double weight, Integer stockAmount, CountryEnum country) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.weight = weight;
        this.stockAmount = stockAmount;
        this.country = country;
    }

    public ProductDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDTO that = (ProductDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(price, that.price) && Objects.equals(weight, that.weight) && Objects.equals(stockAmount, that.stockAmount) && country == that.country;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, weight, stockAmount, country);
    }
}
