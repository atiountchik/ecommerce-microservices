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
import com.ecommerce.sdk.request.ClearCartRequestDTO;
import com.ecommerce.sdk.request.ProductAvailabilityRequestDTO;
import com.ecommerce.sdk.request.ValidateCartRequestDTO;
import com.ecommerce.sdk.response.ProductAvailabilityResponseDTO;
import com.ecommerce.sdk.response.RequestTokenDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
public class CartService {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final CartItemsRepository cartItemsRepository;
    private final ItemAvailabilityRepository itemAvailabilityRepository;
    private final FailedItemAvailabilityRepository failedItemAvailabilityRepository;
    private final KafkaProducer<String, ValidateCartRequestDTO> kafkaProducerPlaceOrderCartTopic;
    private final KafkaProducer<String, ProductAvailabilityRequestDTO> kafkaProducerCheckAvailability;

    @Autowired
    public CartService(CartItemsRepository cartItemsRepository,
                       ItemAvailabilityRepository itemAvailabilityRepository,
                       FailedItemAvailabilityRepository failedItemAvailabilityRepository,
                       KafkaProducer<String, ValidateCartRequestDTO> kafkaProducerPlaceOrderCartTopic,
                       @Qualifier(KafkaTopics.CHECK_ITEM_AVAILABILITY_REQUEST_TOPIC) KafkaProducer<String, ProductAvailabilityRequestDTO> kafkaProducerCheckAvailability) {
        this.cartItemsRepository = cartItemsRepository;
        this.itemAvailabilityRepository = itemAvailabilityRepository;
        this.failedItemAvailabilityRepository = failedItemAvailabilityRepository;
        this.kafkaProducerPlaceOrderCartTopic = kafkaProducerPlaceOrderCartTopic;
        this.kafkaProducerCheckAvailability = kafkaProducerCheckAvailability;
    }

    public List<CartItemDBO> getCart(Authentication authentication) {
        UUID buyerId = getBuyerId(authentication);
        return this.cartItemsRepository.findByBuyerId(buyerId);
    }

    private UUID getBuyerId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    @Transactional
    public RequestTokenDTO addItem(Authentication authentication, CartItemDTO requestDTO) {
        UUID buyerId = getBuyerId(authentication);
        UUID sku = requestDTO.getSku();
        CartItemDBO cartItemDBO = this.cartItemsRepository.findByBuyerIdAndSku(buyerId, sku);
        ZonedDateTime now = ZonedDateTime.now();
        if (cartItemDBO == null) {
            cartItemDBO = this.cartItemsRepository.save(new CartItemDBO(0l, now, now, requestDTO.getSku(), 0, buyerId));
        } else {
            Integer quantity = requestDTO.getQuantity() + cartItemDBO.getQuantity();
            requestDTO.setQuantity(quantity);
        }
        UUID requestId = submitProductAvailabilityRequest(requestDTO, cartItemDBO);
        return new RequestTokenDTO(requestId);
    }

    @Transactional
    public RequestTokenDTO updateItem(Authentication authentication, CartItemDTO requestDTO) throws CartItemDoesNotExistException {
        UUID buyerId = getBuyerId(authentication);
        UUID sku = requestDTO.getSku();
        CartItemDBO cartItemDBO = this.cartItemsRepository.findByBuyerIdAndSku(buyerId, sku);
        if (cartItemDBO == null) {
            throw new CartItemDoesNotExistException("cart.api.update.error.cartItemDoesNotExist");
        }
        UUID requestId = submitProductAvailabilityRequest(requestDTO, cartItemDBO);
        return new RequestTokenDTO(requestId);
    }

    private UUID submitProductAvailabilityRequest(CartItemDTO requestDTO, CartItemDBO cartItemDBO) {
        ZonedDateTime now = ZonedDateTime.now();
        ItemAvailabilityRequestDBO itemAvailabilityRequestDBO = new ItemAvailabilityRequestDBO(null, now, now, requestDTO.getQuantity(), null, cartItemDBO);
        itemAvailabilityRequestDBO = this.itemAvailabilityRepository.save(itemAvailabilityRequestDBO);

        UUID requestId = itemAvailabilityRequestDBO.getId();
        ProductAvailabilityRequestDTO productAvailabilityRequestDTO = new ProductAvailabilityRequestDTO(requestId, requestDTO);
        this.kafkaProducerCheckAvailability.send(new ProducerRecord<>(KafkaTopics.CHECK_ITEM_AVAILABILITY_REQUEST_TOPIC, productAvailabilityRequestDTO));
        return requestId;
    }

    @Transactional
    public void deleteItem(Authentication authentication, UUID sku) throws CartItemDoesNotExistException {
        UUID buyerId = getBuyerId(authentication);
        CartItemDBO alreadyExistingItem = this.cartItemsRepository.findByBuyerIdAndSku(buyerId, sku);
        if (alreadyExistingItem == null) {
            throw new CartItemDoesNotExistException("cart.api.update.error.itemDoesNotExist");
        } else {
            this.cartItemsRepository.delete(alreadyExistingItem);
        }
    }

    @Transactional
    public void clearCart(Authentication authentication) throws CartIsEmptyForBuyerException {
        UUID buyerId = getBuyerId(authentication);
        List<CartItemDBO> cartItemsForBuyer = this.cartItemsRepository.findByBuyerId(buyerId);
        if (cartItemsForBuyer == null || cartItemsForBuyer.isEmpty()) {
            throw new CartIsEmptyForBuyerException("cart.api.clear.error.noItemsInCart");
        }
        this.cartItemsRepository.deleteByBuyerId(buyerId);
    }

    public RequestTokenDTO placeOrder(Authentication authentication, Boolean isDryRun) {
        UUID buyerId = getBuyerId(authentication);
        List<CartItemDBO> cartItemsForBuyer = this.cartItemsRepository.findByBuyerId(buyerId);
        List<CartItemDTO> cartItemDTOS = new ArrayList<>();
        for (CartItemDBO cartItemDBO : cartItemsForBuyer) {
            cartItemDTOS.add(cartItemDBO.toCartItemDTO());
        }
        UUID statusUuid = UUID.randomUUID();
        ValidateCartRequestDTO validateCartRequestDTO = new ValidateCartRequestDTO(cartItemDTOS, buyerId, statusUuid, isDryRun);
        this.kafkaProducerPlaceOrderCartTopic.send(new ProducerRecord<>(KafkaTopics.PLACE_ORDER_CART_TOPIC, validateCartRequestDTO));
        return new RequestTokenDTO(statusUuid);
    }

    @KafkaListener(topics = KafkaTopics.CLEAR_CART_TOPIC)
    @Transactional
    public void placeOrder(@Valid ClearCartRequestDTO clearCartRequestDTO) {
        this.cartItemsRepository.deleteByBuyerId(clearCartRequestDTO.getBuyerId());
    }

    @KafkaListener(topics = KafkaTopics.CHECK_ITEM_AVAILABILITY_RESPONSE_TOPIC, containerFactory = "productAvailabilityResponseContainerFactory")
    @Transactional
    public void reactToProductAvailability(@Valid ProductAvailabilityResponseDTO productAvailabilityResponseDTO) {
        if (productAvailabilityResponseDTO == null) {
            logger.error("cart.api.checkProductAvailability.error.nullResponseFromProductService");
            return;
        }
        ProductAvailabilityEnum productAvailabilityEnum = productAvailabilityResponseDTO.getProductAvailabilityEnum();
        UUID requestId = productAvailabilityResponseDTO.getRequestId();

        ItemAvailabilityRequestDBO itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestId).orElse(null);
        if (itemAvailabilityRequestDBO == null) {
            logger.error("cart.api.checkProductAvailability.error.requestIdNotFound | " + requestId);
            return;
        }
        Integer quantity = itemAvailabilityRequestDBO.getNewQuantity();
        ZonedDateTime now = ZonedDateTime.now();

        CartItemDBO cartItemDBO = itemAvailabilityRequestDBO.getCartItemDBO();

        if (productAvailabilityEnum == ProductAvailabilityEnum.AVAILABLE) {
            itemAvailabilityRequestDBO.setLastUpdateDate(now);
            itemAvailabilityRequestDBO.setProductAvailability(productAvailabilityEnum.name());
            this.itemAvailabilityRepository.save(itemAvailabilityRequestDBO);

            cartItemDBO.setLastUpdateDate(now);
            cartItemDBO.setQuantity(quantity);
            this.cartItemsRepository.save(cartItemDBO);
        } else if (productAvailabilityEnum == ProductAvailabilityEnum.UNAVAILABLE || productAvailabilityEnum == ProductAvailabilityEnum.EXCEEDS_AVAILABLE_QUANTITY || productAvailabilityEnum == ProductAvailabilityEnum.NOT_FOUND) {
            this.failedItemAvailabilityRepository.save(new FailedItemAvailabilityRequestDBO(0l, now, productAvailabilityEnum, requestId, cartItemDBO.getBuyerId()));
            this.itemAvailabilityRepository.delete(itemAvailabilityRequestDBO);
            this.cartItemsRepository.delete(cartItemDBO);
        }
    }

    public CartItemDTO checkIfItemAdditionOrUpdateWasSuccessful(Authentication authentication, UUID requestId) throws RequestNotFoundException, AccessDeniedException, ProductDoesNotExistException, NotEnoughStockException, ProductUnavailableException, UnknownCartItemStatusUpdateException {
        UUID buyerId = getBuyerId(authentication);
        ItemAvailabilityRequestDBO itemAvailabilityRequestDBO = this.itemAvailabilityRepository.findById(requestId).orElse(null);
        if (itemAvailabilityRequestDBO == null) {
            FailedItemAvailabilityRequestDBO failedItemAvailabilityRequestDBO = this.failedItemAvailabilityRepository.findByItemAvailabilityRequestId(requestId);
            if (failedItemAvailabilityRequestDBO == null) {
                throw new RequestNotFoundException("cart.api.checkIfItemAdditionOrUpdateWasSuccessful.error.noRequestFound");
            }
            if (ProductAvailabilityEnum.NOT_FOUND.name().equals(failedItemAvailabilityRequestDBO.getProductAvailability())) {
                throw new ProductDoesNotExistException("cart.api.checkIfItemAdditionOrUpdateWasSuccessful.invalidItemSKU");
            } else if (ProductAvailabilityEnum.EXCEEDS_AVAILABLE_QUANTITY.name().equals(failedItemAvailabilityRequestDBO.getProductAvailability())) {
                throw new NotEnoughStockException("cart.api.checkIfItemAdditionOrUpdateWasSuccessful.quantityExceeded");
            } else {
                throw new ProductUnavailableException("cart.api.checkIfItemAdditionOrUpdateWasSuccessful.unavailable");
            }
        } else if (!isBuyerAdmin(authentication) && !buyerId.equals(itemAvailabilityRequestDBO.getCartItemDBO().getBuyerId())) {
            throw new AccessDeniedException("cart.api.checkIfItemAdditionOrUpdateWasSuccessful.error.forbidden");
        }
        String productAvailability = itemAvailabilityRequestDBO.getProductAvailability();
        if (ProductAvailabilityEnum.AVAILABLE.name().equals(productAvailability)) {
            return itemAvailabilityRequestDBO.getCartItemDBO().toCartItemDTO();
        } else if (productAvailability == null) {
            return null;
        } else {
            throw new UnknownCartItemStatusUpdateException("cart.api.checkIfItemAdditionOrUpdateWasSuccessful.error.unknown");
        }
    }

    private boolean isBuyerAdmin(Authentication authentication) {
        List<GrantedAuthority> adminAuthority = authentication.getAuthorities().stream().filter(v -> ("ROLE_" + EcommerceAuthorities.BUYER_ADMIN.name()).equals(v.getAuthority())).collect(Collectors.toList());
        return adminAuthority != null && adminAuthority.size() > 0;
    }
}
