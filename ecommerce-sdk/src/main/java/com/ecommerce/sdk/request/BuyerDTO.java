package com.ecommerce.sdk.request;

import com.ecommerce.sdk.enums.CountryEnum;

import javax.validation.constraints.*;
import java.util.Objects;

public class BuyerDTO {

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

    public BuyerDTO(Long id, String name, Double latitude, Double longitude, CountryEnum country) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
    }

    public BuyerDTO() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuyerDTO that = (BuyerDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude) && country == that.country;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, latitude, longitude, country);
    }
}
