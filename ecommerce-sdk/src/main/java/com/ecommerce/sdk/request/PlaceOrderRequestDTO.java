package com.ecommerce.sdk.request;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class PlaceOrderRequestDTO {
    @NotNull
    private PlaceOrderBuyerRequestDTO placeOrderBuyerRequestDTO;
    @NotNull
    private String country;

    public PlaceOrderRequestDTO(PlaceOrderBuyerRequestDTO placeOrderBuyerRequestDTO, String country) {
        this.placeOrderBuyerRequestDTO = placeOrderBuyerRequestDTO;
        this.country = country;
    }

    public PlaceOrderRequestDTO() {
    }

    public PlaceOrderBuyerRequestDTO getPlaceOrderBuyerRequestDTO() {
        return placeOrderBuyerRequestDTO;
    }

    public void setPlaceOrderBuyerRequestDTO(PlaceOrderBuyerRequestDTO placeOrderBuyerRequestDTO) {
        this.placeOrderBuyerRequestDTO = placeOrderBuyerRequestDTO;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceOrderRequestDTO that = (PlaceOrderRequestDTO) o;
        return Objects.equals(placeOrderBuyerRequestDTO, that.placeOrderBuyerRequestDTO) && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeOrderBuyerRequestDTO, country);
    }
}
