package com.ecommerce.seller.service;

import com.ecommerce.sdk.exceptions.DeleteUserException;
import com.ecommerce.sdk.exceptions.InvalidInputException;
import com.ecommerce.sdk.exceptions.UserConflictException;
import com.ecommerce.sdk.exceptions.WorkingHoursException;
import com.ecommerce.sdk.request.RegisterSellerRequestDTO;
import com.ecommerce.seller.repository.SellerRepository;
import com.ecommerce.seller.repository.dbo.SellerDBO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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


import static com.ecommerce.seller.service.SellerServiceUT.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({"test"})
public class SellerServiceIT {

    @SpyBean
    private SellerService sellerService;

    @MockBean
    private KeycloakService keycloakService;
    @Autowired
    private SellerRepository sellerRepository;

    public static final RegisterSellerRequestDTO REGISTER_SELLER_REQUEST_DTO_NAME_TOO_BIG = new RegisterSellerRequestDTO(EMAIL, PASSWORD, COUNTRY, NAME_TOO_BIG, LATITUDE, LONGITUDE, COUNTRY_VAT, OPENS_AT.toString(), CLOSES_AT.toString());

    @BeforeEach
    @Transactional
    public void reinitializeDb() {
        if (this.sellerRepository.findAll().size() > 0) {
            this.sellerRepository.deleteAll();
        }
    }

    @Test
    public void should_registerWellAllData_when_inputIsValidAndUserNotExists() throws UnknownHostException, UserConflictException, InvalidInputException, DeleteUserException, WorkingHoursException {
        when(this.keycloakService.register(EMAIL, SellerServiceUT.PASSWORD)).thenReturn(SellerServiceUT.REGISTERED_USER_UUID);
        this.sellerService.register(REGISTER_SELLER_REQUEST_DTO);
        List<SellerDBO> all = this.sellerRepository.findAll();
        Assertions.assertEquals(all.size(), 1);
        SellerDBO expected = all.get(0);
        validateDBO(expected);
    }

    @Test
    public void should_getProfileSuccessfully_after_itIsCreated() throws UnknownHostException, UserConflictException, InvalidInputException, DeleteUserException, WorkingHoursException {
        when(this.keycloakService.register(EMAIL, SellerServiceUT.PASSWORD)).thenReturn(SellerServiceUT.REGISTERED_USER_UUID);
        this.sellerService.register(REGISTER_SELLER_REQUEST_DTO);
        SellerDBO profile = this.sellerService.getProfile(new AuthenticationStub());
        Assertions.assertNotNull(profile);
        validateDBO(profile);
    }

    @Test
    public void should_throwDataIntegrityViolationException_when_nameIsTooBig() throws UnknownHostException, UserConflictException, InvalidInputException, DeleteUserException {
        when(this.keycloakService.register(EMAIL, SellerServiceUT.PASSWORD)).thenReturn(SellerServiceUT.REGISTERED_USER_UUID);
        Assertions.assertThrows(
                DataIntegrityViolationException.class, () -> this.sellerService.register(REGISTER_SELLER_REQUEST_DTO_NAME_TOO_BIG));
    }


    private void validateDBO(SellerDBO expected) {
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
