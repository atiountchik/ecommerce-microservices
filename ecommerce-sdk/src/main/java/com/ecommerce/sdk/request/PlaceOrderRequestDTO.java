package com.ecommerce.sdk.request;

import com.ecommerce.sdk.enums.CountryEnum;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class PlaceOrderRequestDTO {
    @NotNull
    private PlaceOrderBuyerRequestDTO placeOrderBuyerRequestDTO;
    @NotNull
    private CountryEnum country;

    public PlaceOrderRequestDTO(PlaceOrderBuyerRequestDTO placeOrderBuyerRequestDTO, CountryEnum country) {
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

    public CountryEnum getCountry() {
        return country;
    }

    public void setCountry(CountryEnum country) {
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceOrderRequestDTO that = (PlaceOrderRequestDTO) o;
        return Objects.equals(placeOrderBuyerRequestDTO, that.placeOrderBuyerRequestDTO) && country == that.country;
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeOrderBuyerRequestDTO, country);
    }
}
