package com.ecommerce.buyer.service;

import com.ecommerce.buyer.repository.BuyerRepository;
import com.ecommerce.buyer.repository.dbo.BuyerDBO;
import com.ecommerce.sdk.enums.CountryEnum;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.exceptions.DeleteUserException;
import com.ecommerce.sdk.exceptions.InvalidInputException;
import com.ecommerce.sdk.exceptions.UserConflictException;
import com.ecommerce.sdk.request.LoginRequestDTO;
import com.ecommerce.sdk.request.PlaceOrderRequestDTO;
import com.ecommerce.sdk.request.RegisterBuyerRequestDTO;
import com.ecommerce.sdk.request.SwitchOrderStatusDTO;
import com.ecommerce.sdk.response.LoginResponseDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authorization.client.AuthorizationDeniedException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.UnknownHostException;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class BuyerServiceUT {

    @SpyBean
    private BuyerService buyerService;

    @MockBean
    private KeycloakService keycloakService;
    @MockBean
    private BuyerRepository buyerRepository;
    @MockBean
    private KafkaProducer<String, PlaceOrderRequestDTO> kafkaProducerPlaceOrderTopic;
    @MockBean
    @Qualifier(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC)
    private KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus;

    public static final String EMAIL = "a@b.c";
    public static final String PASSWORD = "abc";
    public static final LoginRequestDTO LOGIN_REQUEST_DTO = new LoginRequestDTO(EMAIL, PASSWORD);
    public static final String VALID_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJZa1VVTlB5WWViN1phUmRlVUNtaGFhMVFMcUdOd2lGQ09DUVFlSmtidUlVIn0.eyJleHAiOjE2MzQ0OTA1NTMsImlhdCI6MTYzNDQ5MDI1MywianRpIjoiNjgyMjBmM2ItZGJlOC00YTgzLWJiMTQtZWY0MTdjZDU2MGE2IiwiaXNzIjoiaHR0cHM6Ly9sb2NhbGhvc3QvYXV0aC9yZWFsbXMvYnV5ZXItcmVhbG0iLCJhdWQiOiJidXllci1zZXJ2aWNlIiwic3ViIjoiYjMxYjEzZGYtYzVmYS00NDJmLWEyOTctNmI3NjAzNzU4ZjUxIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYnV5ZXItc2VydmljZSIsInNlc3Npb25fc3RhdGUiOiIwNzUyMzVhMi05YTEyLTRmODAtODQ3Ni0xZGQ3MDI3ODgzMzQiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImRlZmF1bHQtcm9sZXMtYnV5ZXItcmVhbG0iLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiUk9MRV9CVVlFUl9VU0VSIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwiYXV0aG9yaXphdGlvbiI6eyJwZXJtaXNzaW9ucyI6W3sicnNpZCI6IjQ2NDI5ZDFkLTMzYzEtNDI5MC05MTZlLWU4ZjQwOTM3MjNiYiIsInJzbmFtZSI6IkRlZmF1bHQgUmVzb3VyY2UifV19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJzaWQiOiIwNzUyMzVhMi05YTEyLTRmODAtODQ3Ni0xZGQ3MDI3ODgzMzQiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6ImFAYi5jIiwiZW1haWwiOiJhQGIuYyJ9.J6HfmepAPNt73gTgtt0xbvz22WiQZadwt6adUW9EoLMNNi03-VqaEBfzXH2FqlbFhevqyA3VswCdmHysldPeYGEldbU8ZkuYYKryF36N5-WXku6JrxnvBnHmgW-2VijY1fiyVVeDts5W9-kBj6gzL2ScSPD6yH-gcp0oOKshuSFBPlV5Sr-9_594vJ2Ik0B_SxXFEiR7FHmxZ9nCODOV8jgqZ3wIvYL-Ha1WRepoV5odqmf52rv4AHS6HMILKYT7rwXmOKfP-qYivBkCcPBLtyvE1ZU2yrcfEGge96XvwBw7K0qLVvMaSakZTb-F-otGspUQQsmYHW_JwGiKG7I6Ww";

    public static final CountryEnum COUNTRY = CountryEnum.UM;
    public static final String NAME = "First Name Last Name";
    public static final String NAME_TOO_BIG = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    public static final Double LATITUDE = 12.012345d;
    public static final Double LONGITUDE = -178.123456;
    public static final RegisterBuyerRequestDTO REGISTER_BUYER_REQUEST_DTO = new RegisterBuyerRequestDTO(EMAIL, PASSWORD, COUNTRY, NAME, LATITUDE, LONGITUDE);
    public static final UUID REGISTERED_USER_UUID = UUID.randomUUID();

    @Test
    public void should_loginSuccessfully_when_credentialsAreCorrect() throws UnknownHostException {
        when(this.keycloakService.obtainToken(EMAIL, PASSWORD)).thenReturn(this.VALID_TOKEN);
        LoginResponseDTO loginResponseDTO = this.buyerService.login(this.LOGIN_REQUEST_DTO);
        Assertions.assertEquals(loginResponseDTO, new LoginResponseDTO(this.VALID_TOKEN));
    }

    @Test
    public void should_throwAuthorizationDeniedException_when_credentialsAreIncorrect() throws UnknownHostException, DeleteUserException {
        when(this.keycloakService.obtainToken(EMAIL, PASSWORD)).thenThrow(AuthorizationDeniedException.class);

        Assertions.assertThrows(
                AuthorizationDeniedException.class, () -> this.buyerService.login(this.LOGIN_REQUEST_DTO));

        verify(this.keycloakService, times(1)).obtainToken(EMAIL, PASSWORD);
        verify(this.buyerRepository, never()).findByAuthId(any(UUID.class));
        verify(this.keycloakService, never()).deleteUser(any(UUID.class));
        verify(this.buyerRepository, never()).save(any(BuyerDBO.class));
    }

    @Test
    public void should_throwUnknownHostException_when_ssoServerIsUnavailable_on_login() throws UnknownHostException, DeleteUserException {
        when(this.keycloakService.obtainToken(EMAIL, PASSWORD)).thenThrow(UnknownHostException.class);

        Assertions.assertThrows(
                UnknownHostException.class, () -> this.buyerService.login(this.LOGIN_REQUEST_DTO));

        verify(this.buyerRepository, never()).findByAuthId(any(UUID.class));
        verify(this.keycloakService, never()).deleteUser(any(UUID.class));
        verify(this.buyerRepository, never()).save(any(BuyerDBO.class));
    }

    @Test
    public void should_registerSuccessfully_when_buyerDoesNotExistYet() throws UnknownHostException, UserConflictException, InvalidInputException, DeleteUserException {
        when(this.keycloakService.register(EMAIL, PASSWORD)).thenReturn(this.REGISTERED_USER_UUID);
        when(this.buyerRepository.findByAuthId(this.REGISTERED_USER_UUID)).thenReturn(null);
        when(this.buyerRepository.save(any(BuyerDBO.class))).thenReturn(null);

        this.buyerService.register(this.REGISTER_BUYER_REQUEST_DTO);

        verify(this.keycloakService, times(1)).register(EMAIL, PASSWORD);
        verify(this.buyerRepository, times(1)).findByAuthId(this.REGISTERED_USER_UUID);
        verify(this.keycloakService, never()).deleteUser(any(UUID.class));
        verify(this.buyerRepository, times(1)).save(any(BuyerDBO.class));
    }

    @Test
    public void should_throwInvalidInputException_when_inputIsNull() throws UnknownHostException, UserConflictException, DeleteUserException {
        Assertions.assertThrows(
                InvalidInputException.class, () -> this.buyerService.register(null));

        verify(this.keycloakService, never()).register(any(String.class), any(String.class));
        verify(this.buyerRepository, never()).findByAuthId(any(UUID.class));
        verify(this.keycloakService, never()).deleteUser(any(UUID.class));
        verify(this.buyerRepository, never()).save(any(BuyerDBO.class));
    }

    @Test
    public void should_throwUserConflictException_when_buyerAlreadyExistsInKeycloak() throws UnknownHostException, UserConflictException, DeleteUserException {
        when(this.keycloakService.register(EMAIL, PASSWORD)).thenThrow(UserConflictException.class);

        Assertions.assertThrows(
                UserConflictException.class, () -> this.buyerService.register(this.REGISTER_BUYER_REQUEST_DTO));

        verify(this.keycloakService, times(1)).register(EMAIL, PASSWORD);
        verify(this.buyerRepository, never()).findByAuthId(any(UUID.class));
        verify(this.keycloakService, never()).deleteUser(any(UUID.class));
        verify(this.buyerRepository, never()).save(any(BuyerDBO.class));
    }

    @Test
    public void should_throwUnknownHostException_when_ssoServerIsUnavailable_on_register() throws UnknownHostException, UserConflictException, DeleteUserException {
        when(this.keycloakService.register(EMAIL, PASSWORD)).thenThrow(UnknownHostException.class);
        Assertions.assertThrows(
                UnknownHostException.class, () -> this.buyerService.register(this.REGISTER_BUYER_REQUEST_DTO));

        verify(this.keycloakService, times(1)).register(EMAIL, PASSWORD);
        verify(this.buyerRepository, never()).findByAuthId(any(UUID.class));
        verify(this.keycloakService, never()).deleteUser(any(UUID.class));
        verify(this.buyerRepository, never()).save(any(BuyerDBO.class));
    }

    @Test
    public void should_throwUserConflictException_when_buyerInfoAlreadyExistsInDatabase() throws UnknownHostException, UserConflictException, DeleteUserException {
        when(this.keycloakService.register(EMAIL, PASSWORD)).thenReturn(this.REGISTERED_USER_UUID);
        BuyerDBO mock = mock(BuyerDBO.class);
        when(this.buyerRepository.findByAuthId(this.REGISTERED_USER_UUID)).thenReturn(mock);

        Assertions.assertThrows(
                UserConflictException.class, () -> this.buyerService.register(this.REGISTER_BUYER_REQUEST_DTO));

        verify(this.keycloakService, times(1)).register(EMAIL, PASSWORD);
        verify(this.buyerRepository, times(1)).findByAuthId(any(UUID.class));
        verify(this.keycloakService, times(1)).deleteUser(this.REGISTERED_USER_UUID);
        verify(this.buyerRepository, never()).save(any(BuyerDBO.class));
    }

    @Test
    public void should_throwIllegalArgumentException_when_inputIsNull_for_placeOrder() throws IllegalArgumentException {
        Assertions.assertThrows(
                IllegalArgumentException.class, () -> this.buyerService.placeOrderBuyer(null));

    }
}
