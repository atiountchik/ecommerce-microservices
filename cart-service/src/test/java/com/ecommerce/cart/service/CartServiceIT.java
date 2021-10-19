package com.ecommerce.cart.service;

import com.ecommerce.cart.repository.CartItemsRepository;
import com.ecommerce.cart.repository.FailedItemAvailabilityRepository;
import com.ecommerce.cart.repository.ItemAvailabilityRepository;
import com.ecommerce.cart.repository.dbo.CartItemDBO;
import com.ecommerce.cart.repository.dbo.FailedItemAvailabilityRequestDBO;
import com.ecommerce.cart.repository.dbo.ItemAvailabilityRequestDBO;
import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.enums.EcommerceAuthorities;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.enums.ProductAvailabilityEnum;
import com.ecommerce.sdk.exceptions.*;
import com.ecommerce.sdk.request.ProductAvailabilityRequestDTO;
import com.ecommerce.sdk.request.ValidateCartRequestDTO;
import com.ecommerce.sdk.response.ProductAvailabilityResponseDTO;
import com.ecommerce.sdk.response.RequestTokenDTO;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles({"test"})
public class CartServiceIT {

    @SpyBean
    private CartService cartService;

    @MockBean
    private KafkaProducer<String, ValidateCartRequestDTO> kafkaProducerPlaceOrderCartTopic;

    @MockBean
    @Qualifier(KafkaTopics.CHECK_ITEM_AVAILABILITY_REQUEST_TOPIC)
    private KafkaProducer<String, ProductAvailabilityRequestDTO> kafkaProducerCheckAvailability;

    @Autowired
    private CartItemsRepository cartRepository;
    @Autowired
    private ItemAvailabilityRepository itemAvailabilityRepository;
    @Autowired
    private FailedItemAvailabilityRepository failedItemAvailabilityRepository;

    private static final UUID REGISTERED_BUYER_UUID = UUID.randomUUID();
    private static final UUID SKU = UUID.randomUUID();
    private static final Integer QUANTITY = 10;
    private static final CartItemDTO CART_ITEM_DTO = new CartItemDTO(SKU, QUANTITY);

    @BeforeEach
    public void reinitializeDb() {
        if (this.cartRepository.findAll().size() > 0) {
            this.cartRepository.deleteAll();
        }
        if (this.itemAvailabilityRepository.findAll().size() > 0) {
            this.itemAvailabilityRepository.deleteAll();
        }
        if (this.failedItemAvailabilityRepository.findAll().size() > 0) {
            this.failedItemAvailabilityRepository.deleteAll();
        }
    }

    @Test
    public void should_addItemRequest_and_CartItem_when_buyerAddsItemToCart() {
        RequestTokenDTO requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);
        ItemAvailabilityRequestDBO itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        Assertions.assertNotNull(itemAvailabilityRequestDBO);
        Assertions.assertEquals(itemAvailabilityRequestDBO.getProductAvailability(), null);
        Assertions.assertEquals(itemAvailabilityRequestDBO.getNewQuantity(), QUANTITY);
        Assertions.assertEquals(itemAvailabilityRequestDBO.getCartItemDBO().getSku(), SKU);
        Assertions.assertEquals(itemAvailabilityRequestDBO.getCartItemDBO().getBuyerId(), REGISTERED_BUYER_UUID);
        Assertions.assertEquals(itemAvailabilityRequestDBO.getCartItemDBO().getQuantity(), 0);
    }

    @Test
    public void should_updateItemQuantity_when_availabilityRequestReturnsAvailable() {
        RequestTokenDTO requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);

        ItemAvailabilityRequestDBO itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        CartItemDBO cartItemDBO = itemAvailabilityRequestDBO.getCartItemDBO();
        Assertions.assertEquals(cartItemDBO.getQuantity(), 0);

        this.cartService.reactToProductAvailability(new ProductAvailabilityResponseDTO(requestTokenDTO.getToken(), ProductAvailabilityEnum.AVAILABLE));

        itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        cartItemDBO = itemAvailabilityRequestDBO.getCartItemDBO();
        Assertions.assertEquals(cartItemDBO.getQuantity(), QUANTITY);
        Assertions.assertTrue(cartItemDBO.getLastUpdateDate().isAfter(cartItemDBO.getCreationDate()));

        FailedItemAvailabilityRequestDBO failedItemAvailabilityRequestDBO = this.failedItemAvailabilityRepository.findByItemAvailabilityRequestId(itemAvailabilityRequestDBO.getId());
        Assertions.assertNull(failedItemAvailabilityRequestDBO);
    }

    @Test
    public void should_rollBackCartAddition_and_deleteItemAvailabilityRequest_when_availabilityRequestReturnsNotFound() {
        RequestTokenDTO requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);

        ItemAvailabilityRequestDBO itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        UUID itemAvailabilityRequestDBOId = itemAvailabilityRequestDBO.getId();
        CartItemDBO cartItemDBO = itemAvailabilityRequestDBO.getCartItemDBO();
        Assertions.assertEquals(cartItemDBO.getQuantity(), 0);

        this.cartService.reactToProductAvailability(new ProductAvailabilityResponseDTO(requestTokenDTO.getToken(), ProductAvailabilityEnum.NOT_FOUND));

        itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        cartItemDBO = this.cartRepository.findById(cartItemDBO.getId()).orElse(null);
        Assertions.assertNull(itemAvailabilityRequestDBO);
        Assertions.assertNull(cartItemDBO);

        FailedItemAvailabilityRequestDBO failedItemAvailabilityRequestDBO = this.failedItemAvailabilityRepository.findByItemAvailabilityRequestId(itemAvailabilityRequestDBOId);
        Assertions.assertNotNull(failedItemAvailabilityRequestDBO);
        Assertions.assertEquals(failedItemAvailabilityRequestDBO.getProductAvailability(), ProductAvailabilityEnum.NOT_FOUND.name());
    }

    @Test
    public void should_rollBackCartAddition_and_deleteItemAvailabilityRequest_when_availabilityRequestReturnsUnavailable() {
        RequestTokenDTO requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);

        ItemAvailabilityRequestDBO itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        UUID itemAvailabilityRequestDBOId = itemAvailabilityRequestDBO.getId();
        CartItemDBO cartItemDBO = itemAvailabilityRequestDBO.getCartItemDBO();
        Assertions.assertEquals(cartItemDBO.getQuantity(), 0);

        this.cartService.reactToProductAvailability(new ProductAvailabilityResponseDTO(requestTokenDTO.getToken(), ProductAvailabilityEnum.UNAVAILABLE));

        itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        cartItemDBO = this.cartRepository.findById(cartItemDBO.getId()).orElse(null);
        Assertions.assertNull(itemAvailabilityRequestDBO);
        Assertions.assertNull(cartItemDBO);

        FailedItemAvailabilityRequestDBO failedItemAvailabilityRequestDBO = this.failedItemAvailabilityRepository.findByItemAvailabilityRequestId(itemAvailabilityRequestDBOId);
        Assertions.assertNotNull(failedItemAvailabilityRequestDBO);
        Assertions.assertEquals(failedItemAvailabilityRequestDBO.getProductAvailability(), ProductAvailabilityEnum.UNAVAILABLE.name());
    }

    @Test
    public void should_rollBackCartAddition_and_deleteItemAvailabilityRequest_when_availabilityRequestReturnsQuantityExceeded() {
        RequestTokenDTO requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);

        ItemAvailabilityRequestDBO itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        UUID itemAvailabilityRequestDBOId = itemAvailabilityRequestDBO.getId();
        CartItemDBO cartItemDBO = itemAvailabilityRequestDBO.getCartItemDBO();
        Assertions.assertEquals(cartItemDBO.getQuantity(), 0);

        this.cartService.reactToProductAvailability(new ProductAvailabilityResponseDTO(requestTokenDTO.getToken(), ProductAvailabilityEnum.EXCEEDS_AVAILABLE_QUANTITY));

        itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        cartItemDBO = this.cartRepository.findById(cartItemDBO.getId()).orElse(null);
        Assertions.assertNull(itemAvailabilityRequestDBO);
        Assertions.assertNull(cartItemDBO);

        FailedItemAvailabilityRequestDBO failedItemAvailabilityRequestDBO = this.failedItemAvailabilityRepository.findByItemAvailabilityRequestId(itemAvailabilityRequestDBOId);
        Assertions.assertNotNull(failedItemAvailabilityRequestDBO);
        Assertions.assertEquals(failedItemAvailabilityRequestDBO.getProductAvailability(), ProductAvailabilityEnum.EXCEEDS_AVAILABLE_QUANTITY.name());
    }

    @Test
    public void should_throwCartNotFoundException_when_tryingToUpdateAnItemThatIsNotInCart() {
        Assertions.assertThrows(CartItemDoesNotExistException.class, () -> this.cartService.updateItem(new AuthenticationStub(), new CartItemDTO(SKU, QUANTITY)));
    }

    @Test
    public void should_deleteCartItem_and_deleteItemAvailabilityRequest_and_returnRequestToken_when_quantityIsSuperiorToThePreexistingOne_and_productQuantityIsUnavailable() throws CartItemDoesNotExistException {
        RequestTokenDTO requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);
        this.cartService.reactToProductAvailability(new ProductAvailabilityResponseDTO(requestTokenDTO.getToken(), ProductAvailabilityEnum.AVAILABLE));
        int newQuantity = QUANTITY + 1;
        requestTokenDTO = this.cartService.updateItem(new AuthenticationStub(), new CartItemDTO(SKU, newQuantity));

        ItemAvailabilityRequestDBO itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        UUID itemAvailabilityRequestDBOId = itemAvailabilityRequestDBO.getId();
        CartItemDBO cartItemDBO = itemAvailabilityRequestDBO.getCartItemDBO();
        Assertions.assertEquals(cartItemDBO.getQuantity(), QUANTITY);

        this.cartService.reactToProductAvailability(new ProductAvailabilityResponseDTO(requestTokenDTO.getToken(), ProductAvailabilityEnum.UNAVAILABLE));

        itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        cartItemDBO = this.cartRepository.findById(cartItemDBO.getId()).orElse(null);
        Assertions.assertNull(itemAvailabilityRequestDBO);
        Assertions.assertNull(cartItemDBO);

        FailedItemAvailabilityRequestDBO failedItemAvailabilityRequestDBO = this.failedItemAvailabilityRepository.findByItemAvailabilityRequestId(itemAvailabilityRequestDBOId);
        Assertions.assertNotNull(failedItemAvailabilityRequestDBO);
        Assertions.assertEquals(failedItemAvailabilityRequestDBO.getProductAvailability(), ProductAvailabilityEnum.UNAVAILABLE.name());

        Assertions.assertThrows(ProductUnavailableException.class, () -> this.cartService.checkIfItemAdditionOrUpdateWasSuccessful(new AuthenticationStub(), itemAvailabilityRequestDBOId));
    }

    @Test
    public void should_deleteCartItem_and_deleteItemAvailabilityRequest_and_returnRequestToken_when_quantityIsSuperiorToThePreexistingOne_and_productQuantityIsNotFound() throws CartItemDoesNotExistException {
        RequestTokenDTO requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);
        this.cartService.reactToProductAvailability(new ProductAvailabilityResponseDTO(requestTokenDTO.getToken(), ProductAvailabilityEnum.AVAILABLE));
        int newQuantity = QUANTITY + 1;
        requestTokenDTO = this.cartService.updateItem(new AuthenticationStub(), new CartItemDTO(SKU, newQuantity));

        ItemAvailabilityRequestDBO itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        UUID itemAvailabilityRequestDBOId = itemAvailabilityRequestDBO.getId();
        CartItemDBO cartItemDBO = itemAvailabilityRequestDBO.getCartItemDBO();
        Assertions.assertEquals(cartItemDBO.getQuantity(), QUANTITY);

        this.cartService.reactToProductAvailability(new ProductAvailabilityResponseDTO(requestTokenDTO.getToken(), ProductAvailabilityEnum.NOT_FOUND));

        itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        cartItemDBO = this.cartRepository.findById(cartItemDBO.getId()).orElse(null);
        Assertions.assertNull(itemAvailabilityRequestDBO);
        Assertions.assertNull(cartItemDBO);

        FailedItemAvailabilityRequestDBO failedItemAvailabilityRequestDBO = this.failedItemAvailabilityRepository.findByItemAvailabilityRequestId(itemAvailabilityRequestDBOId);
        Assertions.assertNotNull(failedItemAvailabilityRequestDBO);
        Assertions.assertEquals(failedItemAvailabilityRequestDBO.getProductAvailability(), ProductAvailabilityEnum.NOT_FOUND.name());

        Assertions.assertThrows(ProductDoesNotExistException.class, () -> this.cartService.checkIfItemAdditionOrUpdateWasSuccessful(new AuthenticationStub(), itemAvailabilityRequestDBOId));
    }

    @Test
    public void should_deleteCartItem_and_deleteItemAvailabilityRequest_and_returnRequestToken_when_quantityIsSuperiorToThePreexistingOne_and_productQuantityIsExceeded() throws CartItemDoesNotExistException, RequestNotFoundException, ProductDoesNotExistException, NotEnoughStockException, ProductUnavailableException {
        RequestTokenDTO requestTokenDTO = this.cartService.addItem(new AuthenticationStub(), CART_ITEM_DTO);
        this.cartService.reactToProductAvailability(new ProductAvailabilityResponseDTO(requestTokenDTO.getToken(), ProductAvailabilityEnum.AVAILABLE));
        int newQuantity = QUANTITY + 1;
        requestTokenDTO = this.cartService.updateItem(new AuthenticationStub(), new CartItemDTO(SKU, newQuantity));

        ItemAvailabilityRequestDBO itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        UUID itemAvailabilityRequestDBOId = itemAvailabilityRequestDBO.getId();
        CartItemDBO cartItemDBO = itemAvailabilityRequestDBO.getCartItemDBO();
        Assertions.assertEquals(cartItemDBO.getQuantity(), QUANTITY);

        this.cartService.reactToProductAvailability(new ProductAvailabilityResponseDTO(requestTokenDTO.getToken(), ProductAvailabilityEnum.EXCEEDS_AVAILABLE_QUANTITY));

        itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestTokenDTO.getToken()).orElse(null);
        cartItemDBO = this.cartRepository.findById(cartItemDBO.getId()).orElse(null);
        Assertions.assertNull(itemAvailabilityRequestDBO);
        Assertions.assertNull(cartItemDBO);

        FailedItemAvailabilityRequestDBO failedItemAvailabilityRequestDBO = this.failedItemAvailabilityRepository.findByItemAvailabilityRequestId(itemAvailabilityRequestDBOId);
        Assertions.assertNotNull(failedItemAvailabilityRequestDBO);
        Assertions.assertEquals(failedItemAvailabilityRequestDBO.getProductAvailability(), ProductAvailabilityEnum.EXCEEDS_AVAILABLE_QUANTITY.name());

        Assertions.assertThrows(NotEnoughStockException.class, () -> this.cartService.checkIfItemAdditionOrUpdateWasSuccessful(new AuthenticationStub(), itemAvailabilityRequestDBOId));
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