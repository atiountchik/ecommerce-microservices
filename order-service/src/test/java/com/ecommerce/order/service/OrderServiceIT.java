package com.ecommerce.order.service;

import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.dbo.OrderDBO;
import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.domain.CartItemDetailsDTO;
import com.ecommerce.sdk.domain.PaymentTableDTO;
import com.ecommerce.sdk.enums.CountryEnum;
import com.ecommerce.sdk.enums.EcommerceAuthorities;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.enums.OrderStatusEnum;
import com.ecommerce.sdk.exceptions.CountryNotFoundException;
import com.ecommerce.sdk.exceptions.OrderNotFoundException;
import com.ecommerce.sdk.request.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.nio.file.AccessDeniedException;
import java.util.*;

import static com.ecommerce.order.service.OrderService.UNIVERSAL_VAT;

@SpringBootTest
@ActiveProfiles({"test"})
public class OrderServiceIT {

    public static final CartItemDetailsDTO SPANISH_ITEM_1 = new CartItemDetailsDTO(new CartItemDTO(UUID.randomUUID(), 10), 3.5d, 2.0d, CountryEnum.ES);
    public static final CartItemDetailsDTO SPANISH_ITEM_2 = new CartItemDetailsDTO(new CartItemDTO(UUID.randomUUID(), 5), 10.0d, 1.0d, CountryEnum.ES);
    public static final CartItemDetailsDTO PORTUGUESE_ITEM_1 = new CartItemDetailsDTO(new CartItemDTO(UUID.randomUUID(), 10), 4.00d, 1.5d, CountryEnum.PT);
    public static final CartItemDetailsDTO PORTUGUESE_ITEM_2 = new CartItemDetailsDTO(new CartItemDTO(UUID.randomUUID(), 5), 5.00d, 1.25d, CountryEnum.PT);
    public static final UUID STATUS_UUID = UUID.randomUUID();
    public static final PlaceOrderRequestDTO PLACE_ORDER_BUYER_REQUEST_DTO = new PlaceOrderRequestDTO();
    @SpyBean
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private KafkaProducer<String, PaymentRequestDTO> kafkaProducerPlaceOrderPaymentTopic;
    @MockBean
    @Qualifier(KafkaTopics.CLEAR_CART_TOPIC)
    private KafkaProducer<String, ClearCartRequestDTO> kafkaProducerClearCartTopic;
    @MockBean
    @Qualifier(KafkaTopics.REDUCE_PRODUCT_QUANTITIES_TOPIC)
    private KafkaProducer<String, List<ReduceProductQuantitiesRequestDTO>> kafkaProducerReduceProductQuantitiesTopic;

    private static final UUID REGISTERED_BUYER_UUID = UUID.randomUUID();

    @BeforeEach
    @Transactional
    public void reinitializeDb() {
        if (this.orderRepository.findAll().size() > 0) {
            this.orderRepository.deleteAll();
        }
    }

    @BeforeEach
    public void init() {

        Map<CountryEnum, List<CartItemDetailsDTO>> mapItemsGroupedByCountry = new HashMap<>();

        List<CartItemDetailsDTO> portugueseCart = new ArrayList<>();
        portugueseCart.add(PORTUGUESE_ITEM_1);
        portugueseCart.add(PORTUGUESE_ITEM_2);
        mapItemsGroupedByCountry.put(CountryEnum.PT, portugueseCart);

        List<CartItemDetailsDTO> spanishCart = new ArrayList<>();
        spanishCart.add(SPANISH_ITEM_1);
        spanishCart.add(SPANISH_ITEM_2);
        mapItemsGroupedByCountry.put(CountryEnum.ES, spanishCart);

        PLACE_ORDER_BUYER_REQUEST_DTO.setPlaceOrderBuyerRequestDTO(new PlaceOrderBuyerRequestDTO(mapItemsGroupedByCountry, REGISTERED_BUYER_UUID, STATUS_UUID, true));
        PLACE_ORDER_BUYER_REQUEST_DTO.setCountry(CountryEnum.PT);
    }

    @Test
    public void should_provideCorrectOrderStatus_when_dryRunOrderIsPlacedSuccessfully() throws AccessDeniedException, CountryNotFoundException {
        this.orderService.placeOrder(PLACE_ORDER_BUYER_REQUEST_DTO);
        PaymentTableDTO orderTotal = this.orderService.getOrderStatus(new AuthenticationStub(), STATUS_UUID);
        Assertions.assertTrue(orderTotal.getItems().contains(PORTUGUESE_ITEM_1));
        Assertions.assertTrue(orderTotal.getItems().contains(PORTUGUESE_ITEM_2));
        Assertions.assertTrue(orderTotal.getItems().contains(SPANISH_ITEM_1));
        Assertions.assertTrue(orderTotal.getItems().contains(SPANISH_ITEM_2));
        Assertions.assertEquals(orderTotal.getPaymentData().getVAT(), UNIVERSAL_VAT - 1);
        Assertions.assertEquals(orderTotal.getPaymentData().getTotal(), 289.3575d);
        Assertions.assertEquals(orderTotal.getPaymentData().getSubtotal(), 235.25d);
        Assertions.assertEquals(orderTotal.getPaymentData().getDeliveryFee(), 85.25);
        Assertions.assertEquals(orderTotal.getStatus(), OrderStatusEnum.DRY_RUN);

        List<OrderDBO> buyerOrderHistory = this.orderService.getBuyerOrderHistory(new AuthenticationStub());
        Assertions.assertTrue(buyerOrderHistory == null || buyerOrderHistory.isEmpty());
    }

    @Test
    public void should_provideCorrectOrderStatus_when_realOrderIsPlacedSuccessfully() throws AccessDeniedException, CountryNotFoundException {
        PLACE_ORDER_BUYER_REQUEST_DTO.getPlaceOrderBuyerRequestDTO().setDryRun(false);
        this.orderService.placeOrder(PLACE_ORDER_BUYER_REQUEST_DTO);
        PaymentTableDTO orderTotal = this.orderService.getOrderStatus(new AuthenticationStub(), STATUS_UUID);
        Assertions.assertTrue(orderTotal.getItems().contains(PORTUGUESE_ITEM_1));
        Assertions.assertTrue(orderTotal.getItems().contains(PORTUGUESE_ITEM_2));
        Assertions.assertTrue(orderTotal.getItems().contains(SPANISH_ITEM_1));
        Assertions.assertTrue(orderTotal.getItems().contains(SPANISH_ITEM_2));
        Assertions.assertEquals(orderTotal.getPaymentData().getVAT(), UNIVERSAL_VAT - 1);
        Assertions.assertEquals(orderTotal.getPaymentData().getTotal(), 289.3575d);
        Assertions.assertEquals(orderTotal.getPaymentData().getSubtotal(), 235.25d);
        Assertions.assertEquals(orderTotal.getPaymentData().getDeliveryFee(), 85.25);
        Assertions.assertEquals(orderTotal.getStatus(), OrderStatusEnum.PENDING_PAYMENT);
        List<OrderDBO> buyerOrderHistory = this.orderService.getBuyerOrderHistory(new AuthenticationStub());
        Assertions.assertTrue(buyerOrderHistory == null || buyerOrderHistory.isEmpty());
    }

    @Test
    public void should_provideCorrectOrderStatus_when_orderStatusIsChangedThroughSwitch() throws OrderNotFoundException, AccessDeniedException, CountryNotFoundException {
        this.orderService.placeOrder(PLACE_ORDER_BUYER_REQUEST_DTO);
        this.orderService.switchOrderStatus(new SwitchOrderStatusDTO(OrderStatusEnum.ERROR_CORRUPTED_PRODUCT, STATUS_UUID));
        PaymentTableDTO orderStatus = this.orderService.getOrderStatus(new AuthenticationStub(), STATUS_UUID);
        Assertions.assertEquals(orderStatus.getStatus(), OrderStatusEnum.ERROR_CORRUPTED_PRODUCT);
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
            return REGISTERED_BUYER_UUID.toString();
        }
    }

}
