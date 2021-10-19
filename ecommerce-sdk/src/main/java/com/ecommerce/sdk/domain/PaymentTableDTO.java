package com.ecommerce.sdk.domain;

import com.ecommerce.sdk.enums.OrderStatusEnum;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

public class PaymentTableDTO {
    @NotNull
    private List<CartItemDetailsDTO> items;
    @NotNull
    private PaymentValuesDTO paymentData;
    private OrderStatusEnum status;

    public PaymentTableDTO(List<CartItemDetailsDTO> items, PaymentValuesDTO paymentData, OrderStatusEnum status) {
        this.items = items;
        this.paymentData = paymentData;
        this.status = status;
    }

    public PaymentTableDTO() {
    }

    public List<CartItemDetailsDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDetailsDTO> items) {
        this.items = items;
    }

    public PaymentValuesDTO getPaymentData() {
        return paymentData;
    }

    public void setPaymentData(PaymentValuesDTO paymentData) {
        this.paymentData = paymentData;
    }

    public OrderStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentTableDTO that = (PaymentTableDTO) o;
        return Objects.equals(items, that.items) && Objects.equals(paymentData, that.paymentData) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, paymentData, status);
    }
}
