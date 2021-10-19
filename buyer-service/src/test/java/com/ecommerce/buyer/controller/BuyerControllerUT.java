package com.ecommerce.buyer.controller;

import com.ecommerce.buyer.service.BuyerService;
import com.ecommerce.sdk.request.LoginRequestDTO;
import com.ecommerce.sdk.response.LoginResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BuyerController.class)
public class BuyerControllerUT {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BuyerService buyerService;

    @MockBean
    private KeycloakConfigResolver keycloakConfigResolver;

    public static final String EMAIL = "a@b.c";
    public static final String PASSWORD = "abc";
    private LoginRequestDTO loginRequestDTO = new LoginRequestDTO(EMAIL, PASSWORD);

    public static final String VALID_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJZa1VVTlB5WWViN1phUmRlVUNtaGFhMVFMcUdOd2lGQ09DUVFlSmtidUlVIn0.eyJleHAiOjE2MzQ0OTA1NTMsImlhdCI6MTYzNDQ5MDI1MywianRpIjoiNjgyMjBmM2ItZGJlOC00YTgzLWJiMTQtZWY0MTdjZDU2MGE2IiwiaXNzIjoiaHR0cHM6Ly9sb2NhbGhvc3QvYXV0aC9yZWFsbXMvYnV5ZXItcmVhbG0iLCJhdWQiOiJidXllci1zZXJ2aWNlIiwic3ViIjoiYjMxYjEzZGYtYzVmYS00NDJmLWEyOTctNmI3NjAzNzU4ZjUxIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYnV5ZXItc2VydmljZSIsInNlc3Npb25fc3RhdGUiOiIwNzUyMzVhMi05YTEyLTRmODAtODQ3Ni0xZGQ3MDI3ODgzMzQiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImRlZmF1bHQtcm9sZXMtYnV5ZXItcmVhbG0iLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiUk9MRV9CVVlFUl9VU0VSIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiYXV0aG9yaXphdGlvbiI6eyJwZXJtaXNzaW9ucyI6W3sicnNpZCI6IjQ2NDI5ZDFkLTMzYzEtNDI5MC05MTZlLWU4ZjQwOTM3MjNiYiIsInJzbmFtZSI6IkRlZmF1bHQgUmVzb3VyY2UifV19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJzaWQiOiIwNzUyMzVhMi05YTEyLTRmODAtODQ3Ni0xZGQ3MDI3ODgzMzQiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6ImFAYi5jIiwiZW1haWwiOiJhQGIuYyJ9.J6HfmepAPNt73gTgtt0xbvz22WiQZadwt6adUW9EoLMNNi03-VqaEBfzXH2FqlbFhevqyA3VswCdmHysldPeYGEldbU8ZkuYYKryF36N5-WXku6JrxnvBnHmgW-2VijY1fiyVVeDts5W9-kBj6gzL2ScSPD6yH-gcp0oOKshuSFBPlV5Sr-9_594vJ2Ik0B_SxXFEiR7FHmxZ9nCODOV8jgqZ3wIvYL-Ha1WRepoV5odqmf52rv4AHS6HMILKYT7rwXmOKfP-qYivBkCcPBLtyvE1ZU2yrcfEGge96XvwBw7K0qLVvMaSakZTb-F-otGspUQQsmYHW_JwGiKG7I6Ww";
    private LoginResponseDTO loginResponseDTO = new LoginResponseDTO(VALID_TOKEN);

    @Test
    public void should_return200andEmptyBody_when_successfullyRegistering() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/register")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void should_return200andNonEmptyBody_when_successfullyLoggingIn() throws Exception {
        when(buyerService.login(any(LoginRequestDTO.class))).thenReturn(this.loginResponseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(asJsonString(this.loginRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    private String asJsonString(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }
}
