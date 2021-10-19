package com.ecommerce.cart.repository.dbo;

import com.ecommerce.sdk.enums.ProductAvailabilityEnum;
import com.ecommerce.sdk.request.ProductAvailabilityRequestDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "item_availability_requests", schema = "public")
public class ItemAvailabilityRequestDBO {

    @Id
    @GeneratedValue(generator = "uuid_generator")
    @GenericGenerator(name = "uuid_generator", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id")
    private UUID id;

    @Column(name = "creation_date")
    private ZonedDateTime creationDate;

    @Column(name = "last_update_date")
    private ZonedDateTime lastUpdateDate;

    @Column(name = "new_quantity")
    private Integer newQuantity;

    @Column(name = "product_availability")
    private String productAvailability;

    @ManyToOne(cascade = CascadeType.ALL, fetch= FetchType.EAGER)
    @JoinColumn(name = "cart_item_id", referencedColumnName = "id")
    @JsonBackReference
    private CartItemDBO cartItemDBO;

    public ItemAvailabilityRequestDBO(UUID id, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, Integer newQuantity, ProductAvailabilityEnum productAvailability, CartItemDBO cartItemDBO) {
        this.id = id;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.newQuantity = newQuantity;
        this.productAvailability = productAvailability == null ? null : productAvailability.name();
        this.cartItemDBO = cartItemDBO;
    }

    public ItemAvailabilityRequestDBO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public String getProductAvailability() {
        return productAvailability;
    }

    public void setProductAvailability(String productAvailability) {
        this.productAvailability = productAvailability;
    }

    public CartItemDBO getCartItemDBO() {
        return cartItemDBO;
    }

    public void setCartItemDBO(CartItemDBO cartItemDBO) {
        this.cartItemDBO = cartItemDBO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemAvailabilityRequestDBO that = (ItemAvailabilityRequestDBO) o;
        return Objects.equals(id, that.id) && Objects.equals(creationDate, that.creationDate) && Objects.equals(lastUpdateDate, that.lastUpdateDate) && Objects.equals(newQuantity, that.newQuantity) && Objects.equals(productAvailability, that.productAvailability) && Objects.equals(cartItemDBO, that.cartItemDBO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, lastUpdateDate, newQuantity, productAvailability, cartItemDBO);
    }
}
