package com.ecommerce.cart.repository.dbo;

import com.ecommerce.sdk.enums.ProductAvailabilityEnum;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "failed_item_availability_requests", schema = "public")
public class FailedItemAvailabilityRequestDBO {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status_date")
    private ZonedDateTime statusDate;

    @Column(name = "product_availability")
    private String productAvailability;

    @Column(name = "item_availability_request_id")
    private UUID itemAvailabilityRequestId;

    @Column(name = "buyer_id")
    private UUID buyerId;

    public FailedItemAvailabilityRequestDBO(Long id, ZonedDateTime statusDate, ProductAvailabilityEnum productAvailability, UUID itemAvailabilityRequestId, UUID buyerId) {
        this.id = id;
        this.statusDate = statusDate;
        this.productAvailability = productAvailability.name();
        this.itemAvailabilityRequestId = itemAvailabilityRequestId;
        this.buyerId = buyerId;
    }

    public FailedItemAvailabilityRequestDBO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(ZonedDateTime statusDate) {
        this.statusDate = statusDate;
    }

    public String getProductAvailability() {
        return productAvailability;
    }

    public void setProductAvailability(String productAvailability) {
        this.productAvailability = productAvailability;
    }

    public UUID getItemAvailabilityRequestId() {
        return itemAvailabilityRequestId;
    }

    public void setItemAvailabilityRequestId(UUID itemAvailabilityRequestId) {
        this.itemAvailabilityRequestId = itemAvailabilityRequestId;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FailedItemAvailabilityRequestDBO that = (FailedItemAvailabilityRequestDBO) o;
        return Objects.equals(id, that.id) && Objects.equals(statusDate, that.statusDate) && Objects.equals(productAvailability, that.productAvailability) && Objects.equals(itemAvailabilityRequestId, that.itemAvailabilityRequestId) && Objects.equals(buyerId, that.buyerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, statusDate, productAvailability, itemAvailabilityRequestId, buyerId);
    }
}
