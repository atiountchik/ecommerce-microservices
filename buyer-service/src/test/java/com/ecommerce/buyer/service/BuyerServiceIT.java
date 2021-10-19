package com.ecommerce.buyer.service;

import com.ecommerce.buyer.repository.BuyerRepository;
import com.ecommerce.buyer.repository.dbo.BuyerDBO;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.exceptions.CountryNotFoundException;
import com.ecommerce.sdk.exceptions.DeleteUserException;
import com.ecommerce.sdk.exceptions.InvalidInputException;
import com.ecommerce.sdk.exceptions.UserConflictException;
import com.ecommerce.sdk.request.PlaceOrderRequestDTO;
import com.ecommerce.sdk.request.RegisterBuyerRequestDTO;
import com.ecommerce.sdk.request.SwitchOrderStatusDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

import static com.ecommerce.buyer.service.BuyerServiceUT.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({"test"})
public class BuyerServiceIT {

    @SpyBean
    private BuyerService buyerService;

    @MockBean
    private KeycloakService keycloakService;
    @Autowired
    private BuyerRepository buyerRepository;
    @MockBean
    private KafkaProducer<String, PlaceOrderRequestDTO> kafkaProducerPlaceOrderTopic;
    @MockBean
    @Qualifier(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC)
    private KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus;

    public static final RegisterBuyerRequestDTO REGISTER_BUYER_REQUEST_DTO_NAME_TOO_BIG = new RegisterBuyerRequestDTO(EMAIL, PASSWORD, COUNTRY, NAME_TOO_BIG, LATITUDE, LONGITUDE);

    @BeforeEach
    @Transactional
    public void reinitializeDb() {
        if (this.buyerRepository.findAll().size() > 0) {
            this.buyerRepository.deleteAll();
        }
    }

    @Test
    public void should_registerWellAllData_when_inputIsValidAndUserNotExists() throws UnknownHostException, UserConflictException, InvalidInputException, DeleteUserException {
        when(this.keycloakService.register(EMAIL, BuyerServiceUT.PASSWORD)).thenReturn(BuyerServiceUT.REGISTERED_USER_UUID);
        this.buyerService.register(REGISTER_BUYER_REQUEST_DTO);
        List<BuyerDBO> all = this.buyerRepository.findAll();
        Assertions.assertEquals(all.size(), 1);
        BuyerDBO expected = all.get(0);
        validateDBO(expected);
    }

    @Test
    public void should_getProfileSuccessfully_after_itIsCreated() throws UnknownHostException, UserConflictException, InvalidInputException, DeleteUserException, CountryNotFoundException {
        when(this.keycloakService.register(EMAIL, BuyerServiceUT.PASSWORD)).thenReturn(BuyerServiceUT.REGISTERED_USER_UUID);
        this.buyerService.register(REGISTER_BUYER_REQUEST_DTO);
        BuyerDBO profile = this.buyerService.getProfile(new AuthenticationStub());
        Assertions.assertNotNull(profile);
        validateDBO(profile);
    }

    @Test
    public void should_throwDataIntegrityViolationException_when_nameIsTooBig() throws UnknownHostException, UserConflictException, InvalidInputException, DeleteUserException {
        when(this.keycloakService.register(EMAIL, BuyerServiceUT.PASSWORD)).thenReturn(BuyerServiceUT.REGISTERED_USER_UUID);
        Assertions.assertThrows(
                DataIntegrityViolationException.class, () -> this.buyerService.register(REGISTER_BUYER_REQUEST_DTO_NAME_TOO_BIG));
    }


    private void validateDBO(BuyerDBO expected) {
        Assertions.assertEquals(expected.getName(), NAME);
        Assertions.assertEquals(expected.getCountry(), COUNTRY.name());
        Assertions.assertEquals(new BigDecimal(expected.getLatitude(), new MathContext(6, RoundingMode.HALF_EVEN)), new BigDecimal(LATITUDE, new MathContext(6, RoundingMode.HALF_EVEN)));
        Assertions.assertEquals(new BigDecimal(expected.getLongitude(), new MathContext(6, RoundingMode.HALF_EVEN)), new BigDecimal(LONGITUDE, new MathContext(6, RoundingMode.HALF_EVEN)));
    }


    private class AuthenticationStub implements Authentication {
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

}
