package com.ecommerce.payment.repository.dbo;

import com.ecommerce.sdk.enums.PaymentStatusEnum;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payments", schema = "public")
public class PaymentDBO {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creation_date")
    private ZonedDateTime creationDate;

    @Column(name = "last_update_date")
    private ZonedDateTime lastUpdateDate;

    @Column(name = "charge_id")
    private String chargeId;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "status_uuid")
    private UUID statusUuid;

    @Column(name = "buyer_id")
    private UUID buyerId;

    public PaymentDBO(Long id, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, String chargeId, Double amount, PaymentStatusEnum paymentStatus, UUID statusUuid, UUID buyerId) {
        this.id = id;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.chargeId = chargeId;
        this.amount = amount;
        this.paymentStatus = paymentStatus.name();
        this.statusUuid = statusUuid;
        this.buyerId = buyerId;
    }

    public PaymentDBO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public ZonedDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(ZonedDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getChargeId() {
        return chargeId;
    }

    public void setChargeId(String chargeId) {
        this.chargeId = chargeId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
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

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentDBO that = (PaymentDBO) o;
        return Objects.equals(id, that.id) && Objects.equals(creationDate, that.creationDate) && Objects.equals(lastUpdateDate, that.lastUpdateDate) && Objects.equals(chargeId, that.chargeId) && Objects.equals(amount, that.amount) && Objects.equals(paymentStatus, that.paymentStatus) && Objects.equals(statusUuid, that.statusUuid) && Objects.equals(buyerId, that.buyerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, lastUpdateDate, chargeId, amount, paymentStatus, statusUuid, buyerId);
    }
}
