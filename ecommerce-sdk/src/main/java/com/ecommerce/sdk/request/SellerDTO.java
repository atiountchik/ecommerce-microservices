package com.ecommerce.sdk.request;

import com.ecommerce.sdk.enums.CountryEnum;

import javax.validation.constraints.*;
import java.time.OffsetTime;
import java.util.Objects;

public class SellerDTO {

    @Min(1)
    private Long id;

    @Size(max = 200)
    private String name;

    @DecimalMax("90.0")
    @DecimalMin("-90.0")
    private Double latitude;

    @DecimalMax("180.0")
    @DecimalMin("-180.0")
    private Double longitude;

    @NotNull
    private CountryEnum country;

    @NotNull
    private String opensAt;
    @NotNull
    private String closesAt;

    public SellerDTO(Long id, String name, Double latitude, Double longitude, CountryEnum country, String opensAt, String closesAt) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
    }

    public SellerDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public CountryEnum getCountry() {
        return country;
    }

    public void setCountry(CountryEnum country) {
        this.country = country;
    }

    public String getOpensAt() {
        return opensAt;
    }

    public void setOpensAt(String opensAt) {
        this.opensAt = opensAt;
    }

    public String getClosesAt() {
        return closesAt;
    }

    public void setClosesAt(String closesAt) {
        this.closesAt = closesAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SellerDTO sellerDTO = (SellerDTO) o;
        return Objects.equals(id, sellerDTO.id) && Objects.equals(name, sellerDTO.name) && Objects.equals(latitude, sellerDTO.latitude) && Objects.equals(longitude, sellerDTO.longitude) && country == sellerDTO.country && Objects.equals(opensAt, sellerDTO.opensAt) && Objects.equals(closesAt, sellerDTO.closesAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, latitude, longitude, country, opensAt, closesAt);
    }
}
