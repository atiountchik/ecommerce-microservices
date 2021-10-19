package com.ecommerce.payment.service;

import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.repository.dbo.PaymentDBO;
import com.ecommerce.sdk.enums.CountryEnum;
import com.ecommerce.sdk.enums.EcommerceAuthorities;
import com.ecommerce.sdk.enums.PaymentStatusEnum;
import com.ecommerce.sdk.exceptions.PaymentException;
import com.ecommerce.sdk.request.ConfirmPaymentDTO;
import com.ecommerce.sdk.request.PaymentRequestDTO;
import com.ecommerce.sdk.request.SwitchOrderStatusDTO;
import com.stripe.exception.StripeException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles({"test"})
public class PaymentServiceIT {

    @SpyBean
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean
    private KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus;

    private static final UUID REGISTERED_USER_UUID = UUID.randomUUID();
    private static final UUID STATUS_UUID = UUID.randomUUID();
    private static final CountryEnum COUNTRY_ENUM = CountryEnum.AF;
    private static final Double AMOUNT = 10.0d;
    private static final PaymentRequestDTO PAYMENT_REQUEST_DTO = new PaymentRequestDTO(AMOUNT, COUNTRY_ENUM, STATUS_UUID, REGISTERED_USER_UUID);
    private static final ConfirmPaymentDTO CONFIRM_PAYMENT_DTO_GOOD_CARD = new ConfirmPaymentDTO("4242424242424242", 3, 2050, 123, AMOUNT);
    private static final ConfirmPaymentDTO CONFIRM_PAYMENT_DTO_BAD_CARD_ERROR = new ConfirmPaymentDTO("4000000000000101", 3, 2050, 123, AMOUNT);
    private static final ConfirmPaymentDTO CONFIRM_PAYMENT_DTO_BAD_CARD_EXPIRED = new ConfirmPaymentDTO("4000000000000101", 3, 1999, 123, AMOUNT);

    @BeforeEach
    public void init() {
        List<PaymentDBO> allPayments = this.paymentRepository.findAll();
        if (allPayments != null && !allPayments.isEmpty()) {
            this.paymentRepository.deleteAll();
        }
        this.paymentService.placeOrder(PAYMENT_REQUEST_DTO);
    }

    @Test
    public void should_processWellThePayment_when_inputIsGood() throws StripeException, PaymentException {
        this.paymentService.pay(new AuthenticationStub(), CONFIRM_PAYMENT_DTO_GOOD_CARD, STATUS_UUID);
        List<PaymentDBO> all = this.paymentRepository.findAll();
        Assertions.assertNotNull(all);
        Assertions.assertEquals(all.size(), 1);
        PaymentDBO expected = all.get(0);
        Assertions.assertEquals(expected.getAmount(), AMOUNT);
        Assertions.assertEquals(expected.getBuyerId(), REGISTERED_USER_UUID);
        Assertions.assertEquals(expected.getStatusUuid(), STATUS_UUID);
        Assertions.assertEquals(expected.getPaymentStatus(), PaymentStatusEnum.PROCESSED.name());
        Assertions.assertNotNull(expected.getChargeId());
    }

    @Test
    public void should_failPayment_when_cartIsBad() {
        Assertions.assertThrows(StripeException.class, () -> this.paymentService.pay(new AuthenticationStub(), CONFIRM_PAYMENT_DTO_BAD_CARD_ERROR, STATUS_UUID));
    }

    @Test
    public void should_failPayment_when_cardIsExpired() {
        Assertions.assertThrows(Exception.class, () -> this.paymentService.pay(new AuthenticationStub(), CONFIRM_PAYMENT_DTO_BAD_CARD_EXPIRED, STATUS_UUID));
    }

    private class AuthenticationStub implements Authentication {
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Arrays.asList(new SimpleGrantedAuthority(EcommerceAuthorities.BUYER_USER.name()));
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
