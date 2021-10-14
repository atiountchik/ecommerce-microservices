package com.ecommerce.sdk.request;

import com.ecommerce.sdk.domain.CartItemDTO;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlaceOrderBuyerRequestDTO {
    @NotNull
    private Map<String, List<CartItemDTO>> mapItemsGroupedByCountry;
    @NotNull
    private UUID buyerId;
    @NotNull
    private UUID statusUuid;

    public PlaceOrderBuyerRequestDTO(Map<String, List<CartItemDTO>> mapItemsGroupedByCountry, UUID buyerId, UUID statusUuid) {
        this.mapItemsGroupedByCountry = mapItemsGroupedByCountry;
        this.buyerId = buyerId;
        this.statusUuid = statusUuid;
    }

    public PlaceOrderBuyerRequestDTO() {
    }

    public Map<String, List<CartItemDTO>> getMapItemsGroupedByCountry() {
        return mapItemsGroupedByCountry;
    }

    public void setMapItemsGroupedByCountry(Map<String, List<CartItemDTO>> mapItemsGroupedByCountry) {
        this.mapItemsGroupedByCountry = mapItemsGroupedByCountry;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    public UUID getStatusUuid() {
        return statusUuid;
    }

    public void setStatusUuid(UUID statusUuid) {
        this.statusUuid = statusUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceOrderBuyerRequestDTO that = (PlaceOrderBuyerRequestDTO) o;
        return Objects.equals(mapItemsGroupedByCountry, that.mapItemsGroupedByCountry) && Objects.equals(buyerId, that.buyerId) && Objects.equals(statusUuid, that.statusUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapItemsGroupedByCountry, buyerId, statusUuid);
    }
}
