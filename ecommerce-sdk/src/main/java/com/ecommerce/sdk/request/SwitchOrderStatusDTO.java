package com.ecommerce.sdk.request;

import com.ecommerce.sdk.enums.OrderStatusEnum;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

public class SwitchOrderStatusDTO {
    private OrderStatusEnum orderStatusEnum;
    @NotNull
    private UUID statusUuid;

    public SwitchOrderStatusDTO(OrderStatusEnum orderStatusEnum, UUID statusUuid) {
        this.orderStatusEnum = orderStatusEnum;
        this.statusUuid = statusUuid;
    }

    public SwitchOrderStatusDTO() {
    }

    public OrderStatusEnum getOrderStatusEnum() {
        return orderStatusEnum;
    }

    public void setOrderStatusEnum(OrderStatusEnum orderStatusEnum) {
        this.orderStatusEnum = orderStatusEnum;
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
        SwitchOrderStatusDTO that = (SwitchOrderStatusDTO) o;
        return orderStatusEnum == that.orderStatusEnum && Objects.equals(statusUuid, that.statusUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderStatusEnum, statusUuid);
    }
}
