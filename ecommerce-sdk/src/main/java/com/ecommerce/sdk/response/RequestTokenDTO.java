package com.ecommerce.sdk.response;

import java.util.Objects;
import java.util.UUID;

public class RequestTokenDTO {
    private UUID token;

    public RequestTokenDTO(UUID token) {
        this.token = token;
    }

    public RequestTokenDTO() {
    }

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestTokenDTO that = (RequestTokenDTO) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }
}
