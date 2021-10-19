package com.ecommerce.sdk.request;

import com.ecommerce.sdk.domain.CartItemDTO;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ValidateCartRequestDTO {
    @NotNull
    private List<CartItemDTO> cartItemDTOList;
    @NotNull
    private UUID buyerId;
    @NotNull
    private UUID requestId;
    @NotNull
    private Boolean isDryRun;

    public ValidateCartRequestDTO(List<CartItemDTO> cartItemDTOList, UUID buyerId, UUID requestId, Boolean isDryRun) {
        this.cartItemDTOList = cartItemDTOList;
        this.buyerId = buyerId;
        this.requestId = requestId;
        this.isDryRun = isDryRun;
    }

    public ValidateCartRequestDTO() {
    }

    public List<CartItemDTO> getCartItemDTOList() {
        return cartItemDTOList;
    }

    public void setCartItemDTOList(List<CartItemDTO> cartItemDTOList) {
        this.cartItemDTOList = cartItemDTOList;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
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
        ValidateCartRequestDTO that = (ValidateCartRequestDTO) o;
        return Objects.equals(cartItemDTOList, that.cartItemDTOList) && Objects.equals(buyerId, that.buyerId) && Objects.equals(requestId, that.requestId) && Objects.equals(isDryRun, that.isDryRun);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartItemDTOList, buyerId, requestId, isDryRun);
    }
}
