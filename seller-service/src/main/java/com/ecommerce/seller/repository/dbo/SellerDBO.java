package com.ecommerce.seller.repository.dbo;

import com.ecommerce.sdk.enums.CountryEnum;

import javax.persistence.*;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name="sellers", schema="public")
public class SellerDBO {
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

    @Column(name = "country_vat_number")
    private String countryVatNumber;

    @Column(name = "opens_at")
    private OffsetTime opensAt;

    @Column(name = "closes_at")
    private OffsetTime closesAt;

    @Column(name = "auth_id")
    private UUID authId;

    public SellerDBO(Long id, ZonedDateTime creationDate, ZonedDateTime lastUpdateDate, String name, Double latitude, Double longitude, CountryEnum country, String countryVatNumber, OffsetTime opensAt, OffsetTime closesAt, UUID authId) {
        this.id = id;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country.name();
        this.countryVatNumber = countryVatNumber;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.authId = authId;
    }

    public SellerDBO() {
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

    public String getCountryVatNumber() {
        return countryVatNumber;
    }

    public void setCountryVatNumber(String countryVatNumber) {
        this.countryVatNumber = countryVatNumber;
    }

    public OffsetTime getOpensAt() {
        return opensAt;
    }

    public void setOpensAt(OffsetTime opensAt) {
        this.opensAt = opensAt;
    }

    public OffsetTime getClosesAt() {
        return closesAt;
    }

    public void setClosesAt(OffsetTime closesAt) {
        this.closesAt = closesAt;
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
        SellerDBO sellerDBO = (SellerDBO) o;
        return Objects.equals(id, sellerDBO.id) && Objects.equals(creationDate, sellerDBO.creationDate) && Objects.equals(lastUpdateDate, sellerDBO.lastUpdateDate) && Objects.equals(name, sellerDBO.name) && Objects.equals(latitude, sellerDBO.latitude) && Objects.equals(longitude, sellerDBO.longitude) && country == sellerDBO.country && Objects.equals(countryVatNumber, sellerDBO.countryVatNumber) && Objects.equals(opensAt, sellerDBO.opensAt) && Objects.equals(closesAt, sellerDBO.closesAt) && Objects.equals(authId, sellerDBO.authId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, lastUpdateDate, name, latitude, longitude, country, countryVatNumber, opensAt, closesAt, authId);
    }
}
