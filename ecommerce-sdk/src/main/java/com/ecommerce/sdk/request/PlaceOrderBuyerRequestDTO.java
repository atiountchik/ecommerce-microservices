package com.ecommerce.sdk.request;

import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.domain.CartItemDetailsDTO;
import com.ecommerce.sdk.enums.CountryEnum;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlaceOrderBuyerRequestDTO {
    @NotNull
    private Map<CountryEnum, List<CartItemDetailsDTO>> mapItemsGroupedByCountry;
    @NotNull
    private UUID buyerId;
    @NotNull
    private UUID statusUuid;
    @NotNull
    private Boolean isDryRun;

    public PlaceOrderBuyerRequestDTO(Map<CountryEnum, List<CartItemDetailsDTO>> mapItemsGroupedByCountry, UUID buyerId, UUID statusUuid, Boolean isDryRun) {
        this.mapItemsGroupedByCountry = mapItemsGroupedByCountry;
        this.buyerId = buyerId;
        this.statusUuid = statusUuid;
        this.isDryRun = isDryRun;
    }

    public PlaceOrderBuyerRequestDTO() {
    }

    public Map<CountryEnum, List<CartItemDetailsDTO>> getMapItemsGroupedByCountry() {
        return mapItemsGroupedByCountry;
    }

    public void setMapItemsGroupedByCountry(Map<CountryEnum, List<CartItemDetailsDTO>> mapItemsGroupedByCountry) {
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

    public Boolean getDryRun() {
        return isDryRun;
    }

    public void setDryRun(Boolean dryRun) {
        isDryRun = dryRun;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceOrderBuyerRequestDTO that = (PlaceOrderBuyerRequestDTO) o;
        return Objects.equals(mapItemsGroupedByCountry, that.mapItemsGroupedByCountry) && Objects.equals(buyerId, that.buyerId) && Objects.equals(statusUuid, that.statusUuid) && Objects.equals(isDryRun, that.isDryRun);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapItemsGroupedByCountry, buyerId, statusUuid, isDryRun);
    }
}
