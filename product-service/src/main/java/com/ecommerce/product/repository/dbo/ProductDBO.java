package com.ecommerce.product.repository.dbo;

import com.ecommerce.sdk.enums.CountryEnum;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name="products", schema="public")
public class ProductDBO {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Min(0)
    private Long id;

    @Column(name = "creation_date")
    private ZonedDateTime creationDate;

    @Column(name = "last_update_date")
    private ZonedDateTime lastUpdateDate;

    @Column(name = "name")
    @NotBlank
    private String name;

    @Column(name = "price")
    @NotNull
    @DecimalMin("0.01")
    private Double price;

    @Column(name = "weight")
    @NotNull
    @DecimalMin("0.001")
    private Double weight;

    @Column(name = "stock_amount")
    @Min(1)
    private Integer stockAmount;

    @Column(name = "country")
    private String country;

    @Column(name = "SKU")
    private UUID sku;

    @Column(name = "seller_id")
    @NotNull
    private UUID sellerId;

    public ProductDBO(Long id, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, String name, Double price, Double weight, Integer stockAmount, CountryEnum country, UUID sku, UUID sellerId) {
        this.id = id;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.name = name;
        this.price = price;
        this.weight = weight;
        this.stockAmount = stockAmount;
        this.country = country.name();
        this.sku = sku;
        this.sellerId = sellerId;
    }

    public ProductDBO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public ZonedDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(ZonedDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public void setSellerId(UUID sellerId) {
        this.sellerId = sellerId;
    }

    public UUID getSku() {
        return sku;
    }

    public void setSku(UUID sku) {
        this.sku = sku;
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
        ProductDBO that = (ProductDBO) o;
        return Objects.equals(id, that.id) && Objects.equals(creationDate, that.creationDate) && Objects.equals(lastUpdateDate, that.lastUpdateDate) && Objects.equals(name, that.name) && Objects.equals(price, that.price) && Objects.equals(weight, that.weight) && Objects.equals(stockAmount, that.stockAmount) && Objects.equals(country, that.country) && Objects.equals(sku, that.sku) && Objects.equals(sellerId, that.sellerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, lastUpdateDate, name, price, weight, stockAmount, country, sku, sellerId);
    }
}
