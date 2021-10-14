package com.ecommerce.buyer.repository.dbo;


import com.ecommerce.sdk.enums.CountryEnum;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "buyers", schema = "public")
public class BuyerDBO {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creation_date")
    private ZonedDateTime creationDate;

    @Column(name = "last_update_date")
    private ZonedDateTime lastUpdateDate;

    @Column(name = "name")
    private String name;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "country")
    private String country;

    @Column(name = "auth_id")
    private UUID authId;

    public BuyerDBO(Long id, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, String name, Double latitude, Double longitude, CountryEnum country, UUID authId) {
        this.id = id;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country.name();
        this.authId = authId;
    }

    public BuyerDBO() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public UUID getAuthId() {
        return authId;
    }

    public void setAuthId(UUID authId) {
        this.authId = authId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuyerDBO buyerDBO = (BuyerDBO) o;
        return Objects.equals(id, buyerDBO.id) && Objects.equals(creationDate, buyerDBO.creationDate) && Objects.equals(lastUpdateDate, buyerDBO.lastUpdateDate) && Objects.equals(name, buyerDBO.name) && Objects.equals(latitude, buyerDBO.latitude) && Objects.equals(longitude, buyerDBO.longitude) && Objects.equals(country, buyerDBO.country) && Objects.equals(authId, buyerDBO.authId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, lastUpdateDate, name, latitude, longitude, country, authId);
    }
}
