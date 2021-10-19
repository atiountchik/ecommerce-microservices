package com.ecommerce.cart.service;

import com.ecommerce.cart.repository.CartItemsRepository;
import com.ecommerce.cart.repository.FailedItemAvailabilityRepository;
import com.ecommerce.cart.repository.ItemAvailabilityRepository;
import com.ecommerce.cart.repository.dbo.CartItemDBO;
import com.ecommerce.cart.repository.dbo.ItemAvailabilityRequestDBO;
import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.enums.EcommerceAuthorities;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.request.ProductAvailabilityRequestDTO;
import com.ecommerce.sdk.request.ValidateCartRequestDTO;
import com.ecommerce.sdk.response.RequestTokenDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class CartServiceUT {

    @SpyBean
    private CartService cartService;

    @MockBean
    private CartItemsRepository cartItemsRepository;
    @MockBean
    private ItemAvailabilityRepository itemAvailabilityRepository;
    @MockBean
    private FailedItemAvailabilityRepository failedItemAvailabilityRepository;
    @MockBean
    private KafkaProducer<String, ValidateCartRequestDTO> kafkaProducerPlaceOrderCartTopic;
    @MockBean
    @Qualifier(KafkaTopics.CHECK_ITEM_AVAILABILITY_REQUEST_TOPIC)
    private KafkaProducer<String, ProductAvailabilityRequestDTO> kafkaProducerCheckAvailability;

    @Mock
    private CartItemDBO cartItemDBOMock;
    @Mock
    private ItemAvailabilityRequestDBO itemAvailabilityRequestDBO;

    private static final UUID REGISTERED_BUYER_UUID = UUID.randomUUID();
    private static final UUID ITEM_AVAILABILITY_REQUEST_UUID = UUID.randomUUID();
    private static final UUID SKU = UUID.randomUUID();
    private static final Integer QUANTITY = 10;
    private static final CartItemDTO CART_ITEM_DTO = new CartItemDTO(SKU, QUANTITY);

    @BeforeEach
    public void init() {
        when(this.cartItemsRepository.findByBuyerIdAndSku(REGISTERED_BUYER_UUID, SKU)).thenReturn(null);
        when(this.cartItemsRepository.save(any(CartItemDBO.class))).thenReturn(this.cartItemDBOMock);
        when(this.itemAvailabilityRepository.save(any(ItemAvailabilityRequestDBO.class))).thenReturn(this.itemAvailabilityRequestDBO);
        when(this.cartItemDBOMock.getQuantity()).thenReturn(QUANTITY);
        when(this.itemAvailabilityRequestDBO.getId()).thenReturn(ITEM_AVAILABILITY_REQUEST_UUID);
    }

    @Test
    public void should_addItemWithZeroQuantity_when_firstAddingItem() {
        RequestTokenDTO requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);
        Assertions.assertEquals(requestTokenDTO.getToken(), ITEM_AVAILABILITY_REQUEST_UUID);
        verify(this.cartItemsRepository, times(1)).findByBuyerIdAndSku(REGISTERED_BUYER_UUID, SKU);
        verify(this.cartItemsRepository, times(1)).save(any(CartItemDBO.class));
        verify(this.itemAvailabilityRepository, times(1)).save(any(ItemAvailabilityRequestDBO.class));
        verify(this.kafkaProducerCheckAvailability, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    public void should_addItemQuantityToPreviousOne_when_addingItemSecondTime() {
        RequestTokenDTO requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);
        when(this.cartItemsRepository.findByBuyerIdAndSku(REGISTERED_BUYER_UUID, SKU)).thenReturn(cartItemDBOMock);
        requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);
        Assertions.assertEquals(requestTokenDTO.getToken(), ITEM_AVAILABILITY_REQUEST_UUID);

        verify(this.cartItemsRepository, times(2)).findByBuyerIdAndSku(REGISTERED_BUYER_UUID, SKU);
        verify(this.cartItemsRepository, times(1)).save(any(CartItemDBO.class));
        verify(this.itemAvailabilityRepository, times(2)).save(any(ItemAvailabilityRequestDBO.class));
        verify(this.kafkaProducerCheckAvailability, times(2)).send(any(ProducerRecord.class));
    }

    @Test
    public void should_deleteItem_when_addingItemSecondTime() {
        RequestTokenDTO requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);
        when(this.cartItemsRepository.findByBuyerIdAndSku(REGISTERED_BUYER_UUID, SKU)).thenReturn(cartItemDBOMock);
        requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);
        Assertions.assertEquals(requestTokenDTO.getToken(), ITEM_AVAILABILITY_REQUEST_UUID);

        verify(this.cartItemsRepository, times(2)).findByBuyerIdAndSku(REGISTERED_BUYER_UUID, SKU);
        verify(this.cartItemsRepository, times(1)).save(any(CartItemDBO.class));
        verify(this.itemAvailabilityRepository, times(2)).save(any(ItemAvailabilityRequestDBO.class));
        verify(this.kafkaProducerCheckAvailability, times(2)).send(any(ProducerRecord.class));
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
