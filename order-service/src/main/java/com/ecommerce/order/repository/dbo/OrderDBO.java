package com.ecommerce.order.repository.dbo;

import com.ecommerce.sdk.enums.OrderStatusEnum;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name="orders", schema="public")
public class OrderDBO {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creation_date")
    private ZonedDateTime creationDate;

    @Column(name = "last_update_date")
    private ZonedDateTime lastUpdateDate;

    @Column(name = "status")
    private String status;

    @Column(name = "status_uuid")
    private UUID statusUuid;

    @Column(name = "vat")
    private Double vat;

    @Column(name = "buyer_id")
    private UUID buyerId;

    @OneToMany(mappedBy = "orderDBO", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<OrderItemDBO> orderItemDBOList;

    public OrderDBO(Long id, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, OrderStatusEnum status, UUID statusUuid, Double vat, UUID buyerId,List<OrderItemDBO> orderItemDBOList) {
        this.id = id;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.status = status.name();
        this.statusUuid = statusUuid;
        this.vat = vat;
        this.buyerId = buyerId;
        this.orderItemDBOList = orderItemDBOList;
    }

    public OrderDBO(Long id, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, OrderStatusEnum status, UUID statusUuid, Double vat, UUID buyerId) {
        this(id, creationDate, lastUpdateDate, status, statusUuid, vat, buyerId, null);
    }

    public OrderDBO() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status.name();
    }

    public UUID getStatusUuid() {
        return statusUuid;
    }

    public void setStatusUuid(UUID statusUuid) {
        this.statusUuid = statusUuid;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItemDBO> getOrderItemDBOList() {
        return orderItemDBOList;
    }

    public void setOrderItemDBOList(List<OrderItemDBO> orderItemDBOList) {
        this.orderItemDBOList = orderItemDBOList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDBO orderDBO = (OrderDBO) o;
        return Objects.equals(id, orderDBO.id) && Objects.equals(creationDate, orderDBO.creationDate) && Objects.equals(lastUpdateDate, orderDBO.lastUpdateDate) && Objects.equals(status, orderDBO.status) && Objects.equals(statusUuid, orderDBO.statusUuid) && Objects.equals(vat, orderDBO.vat) && Objects.equals(buyerId, orderDBO.buyerId) && Objects.equals(orderItemDBOList, orderDBO.orderItemDBOList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, lastUpdateDate, status, statusUuid, vat, buyerId, orderItemDBOList);
    }
}
