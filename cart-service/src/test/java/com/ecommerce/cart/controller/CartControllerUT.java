package com.ecommerce.cart.controller;

import com.ecommerce.cart.service.CartService;
import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.response.RequestTokenDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Disabled
public class CartControllerUT {

    private static final UUID SKU = UUID.randomUUID();
    private static final Integer QUANTITY = 5;
    private static final CartItemDTO CART_ITEM_DTO = new CartItemDTO(SKU, QUANTITY);
    private static final RequestTokenDTO REQUEST_TOKEN_DTO = new RequestTokenDTO(UUID.randomUUID());
    private static final Authentication AUTHENTICATION = new AuthenticationStub();
    private static final UUID REGISTERED_USER_UUID = UUID.randomUUID();

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private CartService cartService;

    @Autowired
    public MockMvc mockMvc;

    @Test
    public void should_returnOk_when_insertItemIsSuccess() throws Exception {
        when(this.cartService.addItem(AUTHENTICATION, CART_ITEM_DTO)).thenReturn(REQUEST_TOKEN_DTO);

        this.mockMvc.perform(post("/v1/item").content(asJsonString(CART_ITEM_DTO)).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString()).andReturn();
    }

    private static class AuthenticationStub implements Authentication {
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return null;
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }

        @Override
        public void setAuthenticated(boolean b) throws IllegalArgumentException {

        }

        @Override
        public String getName() {
            return REGISTERED_USER_UUID.toString();
        }
    }

    private String asJsonString(Object object) throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(object);
    }
}
