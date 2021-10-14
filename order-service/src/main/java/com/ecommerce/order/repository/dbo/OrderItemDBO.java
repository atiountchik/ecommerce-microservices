package com.ecommerce.order.repository.dbo;

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

    @ManyToOne(cascade = CascadeType.ALL, fetch= FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    @JsonBackReference
    private OrderDBO orderDBO;

    public OrderItemDBO(Long id, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, UUID sku, Double price, Integer quantity, Double weight, OrderDBO orderDBO) {
        this.id = id;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.sku = sku;
        this.price = price;
        this.quantity = quantity;
        this.weight = weight;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemDBO that = (OrderItemDBO) o;
        return Objects.equals(id, that.id) && Objects.equals(creationDate, that.creationDate) && Objects.equals(lastUpdateDate, that.lastUpdateDate) && Objects.equals(sku, that.sku) && Objects.equals(price, that.price) && Objects.equals(quantity, that.quantity) && Objects.equals(weight, that.weight) && Objects.equals(orderDBO, that.orderDBO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, lastUpdateDate, sku, price, quantity, weight, orderDBO);
    }
}
