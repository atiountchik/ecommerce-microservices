package com.ecommerce.order.repository.dbo;

import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.domain.CartItemDetailsDTO;
import com.ecommerce.sdk.enums.CountryEnum;
import com.ecommerce.sdk.exceptions.CountryNotFoundException;
import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "order_items", schema = "public")
public class OrderItemDBO {
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

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "country")
    private String country;

    @ManyToOne(cascade = CascadeType.ALL, fetch= FetchType.EAGER)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    @JsonBackReference
    private OrderDBO orderDBO;

    public OrderItemDBO(Long id, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, UUID sku, Double price, Integer quantity, Double weight, CountryEnum country, OrderDBO orderDBO) {
        this.id = id;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.sku = sku;
        this.price = price;
        this.quantity = quantity;
        this.weight = weight;
        this.country = country.name();
        this.orderDBO = orderDBO;
    }

    public OrderItemDBO() {
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

    public OrderDBO getOrderDBO() {
        return orderDBO;
    }

    public void setOrderDBO(OrderDBO orderDBO) {
        this.orderDBO = orderDBO;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public CartItemDetailsDTO toCartItemDetailsDTO() throws CountryNotFoundException {
        CartItemDetailsDTO cartItemDetailsDTO = new CartItemDetailsDTO(new CartItemDTO(this.sku, this.quantity), this.price, this.weight, CountryEnum.getCountryEnum(this.country));
        return cartItemDetailsDTO;
    }

}
