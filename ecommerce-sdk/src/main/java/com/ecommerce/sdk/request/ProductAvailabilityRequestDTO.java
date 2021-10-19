package com.ecommerce.sdk.request;

import com.ecommerce.sdk.domain.CartItemDTO;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

public class ProductAvailabilityRequestDTO {
    @NotNull
    private UUID requestId;
    @NotNull
    private CartItemDTO cartItemDTO;

    public ProductAvailabilityRequestDTO(UUID requestId, CartItemDTO cartItemDTO) {
        this.requestId = requestId;
        this.cartItemDTO = cartItemDTO;
    }

    public ProductAvailabilityRequestDTO() {
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public CartItemDTO getCartItemDTO() {
        return cartItemDTO;
    }

    public void setCartItemDTO(CartItemDTO cartItemDTO) {
        this.cartItemDTO = cartItemDTO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductAvailabilityRequestDTO that = (ProductAvailabilityRequestDTO) o;
        return Objects.equals(requestId, that.requestId) && Objects.equals(cartItemDTO, that.cartItemDTO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, cartItemDTO);
    }
}
