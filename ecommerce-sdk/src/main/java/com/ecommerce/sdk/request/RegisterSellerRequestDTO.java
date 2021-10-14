package com.ecommerce.sdk.request;

import com.ecommerce.sdk.enums.CountryEnum;

import javax.validation.constraints.*;
import java.util.Objects;

public class RegisterSellerRequestDTO {
    @Email
    private String email;
    @NotBlank
    private String password;
    private CountryEnum country;
    @NotBlank
    private String name;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    @NotNull
    private Double latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    @NotNull
    private Double longitude;

    @NotBlank
    private String countryVatNumber;
    @NotNull
    private String opensAt;
    @NotNull
    private String closesAt;

    public RegisterSellerRequestDTO(String email, String password, CountryEnum country, String name, Double latitude, Double longitude, String countryVatNumber, String opensAt, String closesAt) {
        this.email = email;
        this.password = password;
        this.country = country;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.countryVatNumber = countryVatNumber;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
    }

    public RegisterSellerRequestDTO() {
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

    public String getCountryVatNumber() {
        return countryVatNumber;
    }

    public void setCountryVatNumber(String countryVatNumber) {
        this.countryVatNumber = countryVatNumber;
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
        RegisterSellerRequestDTO that = (RegisterSellerRequestDTO) o;
        return Objects.equals(email, that.email) && Objects.equals(password, that.password) && country == that.country && Objects.equals(name, that.name) && Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude) && Objects.equals(countryVatNumber, that.countryVatNumber) && Objects.equals(opensAt, that.opensAt) && Objects.equals(closesAt, that.closesAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password, country, name, latitude, longitude, countryVatNumber, opensAt, closesAt);
    }
}
