package com.ecommerce.sdk.response;

import com.ecommerce.sdk.enums.ProductAvailabilityEnum;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

public class ProductAvailabilityResponseDTO {
    @NotNull
    private UUID requestId;
    @NotNull
    private ProductAvailabilityEnum productAvailabilityEnum;

    public ProductAvailabilityResponseDTO(UUID requestId, ProductAvailabilityEnum productAvailabilityEnum) {
        this.requestId = requestId;
        this.productAvailabilityEnum = productAvailabilityEnum;
    }

    public ProductAvailabilityResponseDTO() {
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public ProductAvailabilityEnum getProductAvailabilityEnum() {
        return productAvailabilityEnum;
    }

    public void setProductAvailabilityEnum(ProductAvailabilityEnum productAvailabilityEnum) {
        this.productAvailabilityEnum = productAvailabilityEnum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductAvailabilityResponseDTO that = (ProductAvailabilityResponseDTO) o;
        return Objects.equals(requestId, that.requestId) && productAvailabilityEnum == that.productAvailabilityEnum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, productAvailabilityEnum);
    }
}
