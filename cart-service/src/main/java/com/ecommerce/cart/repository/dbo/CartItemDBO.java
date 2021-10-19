package com.ecommerce.cart.repository.dbo;

import com.ecommerce.sdk.domain.CartItemDTO;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cart_items", schema = "public")
public class CartItemDBO {
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

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "buyer_id")
    private UUID buyerId;

    @OneToMany(mappedBy = "cartItemDBO", cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<ItemAvailabilityRequestDBO> itemAvailabilityRequestDBOList;

    public CartItemDBO(Long id, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, UUID sku, Integer quantity, UUID buyerId, List<ItemAvailabilityRequestDBO> itemAvailabilityRequestDBOList) {
        this.id = id;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.sku = sku;
        this.quantity = quantity;
        this.buyerId = buyerId;
        this.itemAvailabilityRequestDBOList = itemAvailabilityRequestDBOList;
    }

    public CartItemDBO(Long id, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, UUID sku, Integer quantity, UUID buyerId) {
        this(id, creationDate, lastUpdateDate, sku, quantity, buyerId, null);
    }

    public CartItemDBO() {
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

    public List<ItemAvailabilityRequestDBO> getItemAvailabilityRequestDBOList() {
        return itemAvailabilityRequestDBOList;
    }

    public void setItemAvailabilityRequestDBOList(List<ItemAvailabilityRequestDBO> itemAvailabilityRequestDBOList) {
        this.itemAvailabilityRequestDBOList = itemAvailabilityRequestDBOList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemDBO that = (CartItemDBO) o;
        return Objects.equals(id, that.id) && Objects.equals(creationDate, that.creationDate) && Objects.equals(lastUpdateDate, that.lastUpdateDate) && Objects.equals(sku, that.sku) && Objects.equals(quantity, that.quantity) && Objects.equals(buyerId, that.buyerId) && Objects.equals(itemAvailabilityRequestDBOList, that.itemAvailabilityRequestDBOList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, lastUpdateDate, sku, quantity, buyerId, itemAvailabilityRequestDBOList);
    }

    public CartItemDTO toCartItemDTO() {
        return new CartItemDTO(this.sku, this.quantity);
    }
}
