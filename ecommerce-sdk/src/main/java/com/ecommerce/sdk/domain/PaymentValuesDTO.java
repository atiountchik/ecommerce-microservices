package com.ecommerce.sdk.domain;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.util.Objects;

public class PaymentValuesDTO {
    @DecimalMin("0.01")
    private Double VAT;
    @DecimalMin("0.01")
    private Double deliveryFee;
    @DecimalMin("0.01")
    private Double subtotal;
    @DecimalMin("0.01")
    private Double total;

    public PaymentValuesDTO(Double VAT, Double deliveryFee, Double subtotal, Double total) {
        this.VAT = VAT;
        this.deliveryFee = deliveryFee;
        this.subtotal = subtotal;
        this.total = total;
    }

    public PaymentValuesDTO() {
    }

    public Double getVAT() {
        return VAT;
    }

    public void setVAT(Double VAT) {
        this.VAT = VAT;
    }

    public Double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(Double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentValuesDTO that = (PaymentValuesDTO) o;
        return Objects.equals(VAT, that.VAT) && Objects.equals(deliveryFee, that.deliveryFee) && Objects.equals(subtotal, that.subtotal) && Objects.equals(total, that.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(VAT, deliveryFee, subtotal, total);
    }
}
