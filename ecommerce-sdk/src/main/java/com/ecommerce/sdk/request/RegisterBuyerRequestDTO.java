package com.ecommerce.sdk.request;

import com.ecommerce.sdk.enums.CountryEnum;

import javax.validation.constraints.*;
import java.util.Objects;

public class RegisterBuyerRequestDTO {
    @Email
    private String email;
    @NotBlank
    private String password;
    private CountryEnum country;

    @NotBlank
    @Size(max = 200)
    private String name;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    @NotNull
    private Double latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    @NotNull
    private Double longitude;

    public RegisterBuyerRequestDTO(String email, String password, CountryEnum country, String name, Double latitude, Double longitude) {
        this.email = email;
        this.password = password;
        this.country = country;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public RegisterBuyerRequestDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public CountryEnum getCountry() {
        return country;
    }

    public void setCountry(CountryEnum country) {
        this.country = country;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterBuyerRequestDTO that = (RegisterBuyerRequestDTO) o;
        return Objects.equals(email, that.email) && Objects.equals(password, that.password) && country == that.country && Objects.equals(name, that.name) && Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password, country, name, latitude, longitude);
    }
}
