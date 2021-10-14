package com.ecommerce.cart.service;

import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.cart.repository.dbo.CartDBO;
import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.exceptions.CartIsEmptyForBuyerException;
import com.ecommerce.sdk.exceptions.CartItemDoesNotExistException;
import com.ecommerce.sdk.exceptions.OrderNotFoundException;
import com.ecommerce.sdk.request.ClearCartRequestDTO;
import com.ecommerce.sdk.request.PlaceOrderRequestDTO;
import com.ecommerce.sdk.request.ValidateCartRequestDTO;
import com.ecommerce.sdk.response.RequestTokenDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Validated
public class CartService {

    private final CartRepository cartRepository;
    private final KafkaProducer<String, ValidateCartRequestDTO> kafkaProducerPlaceOrderCartTopic;

    @Autowired
    public CartService(CartRepository cartRepository, KafkaProducer<String, ValidateCartRequestDTO> kafkaProducerPlaceOrderCartTopic) {
        this.cartRepository = cartRepository;
        this.kafkaProducerPlaceOrderCartTopic = kafkaProducerPlaceOrderCartTopic;
    }

    public List<CartDBO> getCart(Authentication authentication) {
        UUID buyerId = getBuyerId(authentication);
        return this.cartRepository.findByBuyerId(buyerId);
    }

    private UUID getBuyerId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    @Transactional
    public void addItem(Authentication authentication, CartItemDTO requestDTO) {
        UUID buyerId = getBuyerId(authentication);
        UUID sku = requestDTO.getSku();
        CartDBO alreadyExistingItem = this.cartRepository.findByBuyerIdAndSku(buyerId, sku);
        if (alreadyExistingItem == null) {
            ZonedDateTime now = ZonedDateTime.now();
            this.cartRepository.save(new CartDBO(0l, now, now, requestDTO.getSku(), requestDTO.getPrice(), requestDTO.getWeight(), requestDTO.getQuantity(), buyerId));
        } else {
            alreadyExistingItem.setQuantity(alreadyExistingItem.getQuantity() + requestDTO.getQuantity());
            this.cartRepository.save(alreadyExistingItem);
        }
    }

    @Transactional
    public void updateItem(Authentication authentication, CartDBO cartDBO) throws CartItemDoesNotExistException {
        UUID buyerId = getBuyerId(authentication);
        UUID sku = cartDBO.getSku();
        CartDBO alreadyExistingItem = this.cartRepository.findByBuyerIdAndSku(buyerId, sku);
        if (alreadyExistingItem == null) {
            throw new CartItemDoesNotExistException("cart.api.update.error.itemDoesNotExist");
        } else {
            this.cartRepository.save(cartDBO);
        }
    }

    @Transactional
    public void deleteItem(Authentication authentication, CartDBO cartDBO) throws CartItemDoesNotExistException {
        UUID buyerId = getBuyerId(authentication);
        UUID sku = cartDBO.getSku();
        CartDBO alreadyExistingItem = this.cartRepository.findByBuyerIdAndSku(buyerId, sku);
        if (alreadyExistingItem == null) {
            throw new CartItemDoesNotExistException("cart.api.update.error.itemDoesNotExist");
        } else {
            this.cartRepository.deleteById(cartDBO.getId());
        }
    }

    @Transactional
    public void clearCart(Authentication authentication) throws CartIsEmptyForBuyerException {
        UUID buyerId = getBuyerId(authentication);
        List<CartDBO> cartItemsForBuyer = this.cartRepository.findByBuyerId(buyerId);
        if (cartItemsForBuyer == null || cartItemsForBuyer.isEmpty()) {
            throw new CartIsEmptyForBuyerException("cart.api.clear.error.noItemsInCart");
        }
        this.cartRepository.deleteByBuyerId(buyerId);
    }

    public RequestTokenDTO placeOrder(Authentication authentication) {
        UUID buyerId = getBuyerId(authentication);
        List<CartDBO> cartItemsForBuyer = this.cartRepository.findByBuyerId(buyerId);
        List<CartItemDTO> cartItemDTOS = new ArrayList<>();
        for (CartDBO cartDBO : cartItemsForBuyer) {
            cartItemDTOS.add(cartDBO.toCartItemDTO());
        }
        UUID statusUuid = UUID.randomUUID();
        ValidateCartRequestDTO validateCartRequestDTO = new ValidateCartRequestDTO(cartItemDTOS, buyerId, statusUuid);
        this.kafkaProducerPlaceOrderCartTopic.send(new ProducerRecord<>(KafkaTopics.PLACE_ORDER_CART_TOPIC, validateCartRequestDTO));
        return new RequestTokenDTO(statusUuid);
    }

    @KafkaListener(topics = KafkaTopics.CLEAR_CART_TOPIC)
    @Transactional
    public void placeOrder(@Valid ClearCartRequestDTO clearCartRequestDTO) {
        this.cartRepository.deleteByBuyerId(clearCartRequestDTO.getBuyerId());
    }
}
