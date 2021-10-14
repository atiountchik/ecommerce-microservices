package com.ecommerce.cart.repository.dbo;

import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.enums.CountryEnum;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name="carts", schema="public")
public class CartDBO {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creation_date")
    private ZonedDateTime creationDate;

    @Column(name = "last_update_date")
    private ZonedDateTime lastUpdateDate;

    @Column(name = "SKU")
    private UUID sku;

    @Column(name = "price")
    private Double price;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "buyer_id")
    private UUID buyerId;

    public CartDBO(Long id, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, UUID sku, Double price, Double weight, Integer quantity, UUID buyerId) {
        this.id = id;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.sku = sku;
        this.price = price;
        this.weight = weight;
        this.quantity = quantity;
        this.buyerId = buyerId;
    }

    public CartDBO() {
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

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
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
        CartDBO cartDBO = (CartDBO) o;
        return Objects.equals(id, cartDBO.id) && Objects.equals(creationDate, cartDBO.creationDate) && Objects.equals(lastUpdateDate, cartDBO.lastUpdateDate) && Objects.equals(sku, cartDBO.sku) && Objects.equals(price, cartDBO.price) && Objects.equals(weight, cartDBO.weight) && Objects.equals(quantity, cartDBO.quantity) && Objects.equals(buyerId, cartDBO.buyerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, lastUpdateDate, sku, price, weight, quantity, buyerId);
    }

    public CartItemDTO toCartItemDTO() {
        return new CartItemDTO(this.sku, this.price, this.quantity, this.weight);
    }
}
