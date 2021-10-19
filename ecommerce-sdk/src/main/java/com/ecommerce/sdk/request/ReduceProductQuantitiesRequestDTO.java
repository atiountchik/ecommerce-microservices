package com.ecommerce.sdk.request;

import com.ecommerce.sdk.domain.CartItemDTO;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ReduceProductQuantitiesRequestDTO {
    @NotNull
    private List<CartItemDTO> cartItemDTOList;

    public ReduceProductQuantitiesRequestDTO(List<CartItemDTO> cartItemDTOList) {
        this.cartItemDTOList = cartItemDTOList;
    }

    public ReduceProductQuantitiesRequestDTO() {
    }

    public List<CartItemDTO> getCartItemDTOList() {
        return cartItemDTOList;
    }

    public void setCartItemDTOList(List<CartItemDTO> cartItemDTOList) {
        this.cartItemDTOList = cartItemDTOList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReduceProductQuantitiesRequestDTO that = (ReduceProductQuantitiesRequestDTO) o;
        return Objects.equals(cartItemDTOList, that.cartItemDTOList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartItemDTOList);
    }
}
