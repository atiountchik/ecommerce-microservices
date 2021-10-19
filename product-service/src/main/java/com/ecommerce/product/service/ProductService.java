package com.ecommerce.product.service;

import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.dbo.ProductDBO;
import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.domain.CartItemDetailsDTO;
import com.ecommerce.sdk.enums.*;
import com.ecommerce.sdk.exceptions.*;
import com.ecommerce.sdk.request.*;
import com.ecommerce.sdk.response.ProductAvailabilityResponseDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
public class ProductService {

    private final ProductRepository productRepository;
    private final KafkaProducer<String, PlaceOrderBuyerRequestDTO> kafkaProducerPlaceOrderTopic;
    private final KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus;
    private final KafkaProducer<String, ProductAvailabilityResponseDTO> kafkaProducerProductAvailability;

    @Autowired
    public ProductService(ProductRepository productRepository, KafkaProducer<String, PlaceOrderBuyerRequestDTO> kafkaProducerPlaceOrderTopic,
                          @Qualifier(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC) KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus,
                          @Qualifier(KafkaTopics.CHECK_ITEM_AVAILABILITY_RESPONSE_TOPIC) KafkaProducer<String, ProductAvailabilityResponseDTO> kafkaProducerProductAvailability) {
        this.productRepository = productRepository;
        this.kafkaProducerPlaceOrderTopic = kafkaProducerPlaceOrderTopic;
        this.kafkaProducerSwitchStatus = kafkaProducerSwitchStatus;
        this.kafkaProducerProductAvailability = kafkaProducerProductAvailability;
    }

    public List<ProductDBO> getAllProducts() {
        return this.productRepository.findAll();
    }

    @Transactional
    public ProductDBO registerProduct(Authentication authentication, RegisterNewProductRequestDTO newProductRequestDTO) throws AccessDeniedException, ProductAlreadyRegisteredException {
        UUID sellerId = getSellerId(authentication);
        ProductDBO productDBO = this.productRepository.findBySellerIdAndNameIgnoreCase(sellerId, newProductRequestDTO.getName());
        if (productDBO == null) {
            ZonedDateTime now = ZonedDateTime.now();
            productDBO = this.productRepository.save(new ProductDBO(0l, now, now, newProductRequestDTO.getName(), newProductRequestDTO.getPrice(), newProductRequestDTO.getWeight(), newProductRequestDTO.getStockAmount(), newProductRequestDTO.getCountry(), UUID.randomUUID(), sellerId));
        } else {
            throw new ProductAlreadyRegisteredException("product.api.register.error.productAlreadyRegisteredForSeller");
        }
        return productDBO;
    }

    public List<ProductDBO> getSellerProducts(Authentication authentication) {
        UUID sellerId = getSellerId(authentication);
        return this.productRepository.findBySellerId(sellerId);
    }

    private UUID getSellerId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    @Transactional
    public void deleteProduct(Authentication authentication, UUID sku) throws ProductDoesNotExistException, AccessDeniedException {
        UUID sellerId = getSellerId(authentication);
        ProductDBO productDBOFetchedFromDB = this.productRepository.findBySku(sku);
        if (productDBOFetchedFromDB == null) {
            throw new ProductDoesNotExistException("product.api.delete.error.productDoesNotExist");
        }
        if (!isSellerAdmin(authentication) && !productDBOFetchedFromDB.getSellerId().equals(sellerId)) {
            throw new AccessDeniedException("product.api.delete.error.productDoesNotBelongToThisOwner");
        }
        this.productRepository.delete(productDBOFetchedFromDB);
    }

    public ProductDBO updateProduct(Authentication authentication, ProductDTO productDTO) throws UserDoesNotExistException, AccessDeniedException {
        UUID sellerId = getSellerId(authentication);
        ProductDBO fetchedBuyerDBO = this.productRepository.findById(productDTO.getId()).orElse(null);
        if (fetchedBuyerDBO == null) {
            throw new UserDoesNotExistException("product.api.update.error.idNotFound");
        }
        if (!isSellerAdmin(authentication) && !fetchedBuyerDBO.getSellerId().equals(sellerId)) {
            throw new AccessDeniedException("product.api.update.error.forbidden");
        }
        fetchedBuyerDBO.setCountry(productDTO.getCountry().name());
        fetchedBuyerDBO.setLastUpdateDate(ZonedDateTime.now());
        fetchedBuyerDBO.setPrice(productDTO.getPrice());
        fetchedBuyerDBO.setWeight(productDTO.getWeight());
        fetchedBuyerDBO.setStockAmount(productDTO.getStockAmount());
        fetchedBuyerDBO.setName(productDTO.getName());
        return this.productRepository.save(fetchedBuyerDBO);
    }

    private boolean isSellerAdmin(Authentication authentication) {
        List<GrantedAuthority> adminAuthority = authentication.getAuthorities().stream().filter(v -> ("ROLE_" + EcommerceAuthorities.SELLER_ADMIN.name()).equals(v.getAuthority())).collect(Collectors.toList());
        return adminAuthority != null && adminAuthority.size() > 0;
    }

    @KafkaListener(topics = KafkaTopics.PLACE_ORDER_CART_TOPIC)
    @Transactional
    public void placeOrderFromCart(@Valid ValidateCartRequestDTO cartRequestDTO) throws CountryNotFoundException {
        try {
            sanitizePlaceOrderCartRequest(cartRequestDTO);
        } catch (CartIsEmptyForBuyerException e) {
            this.kafkaProducerSwitchStatus.send(new ProducerRecord<>(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC, new SwitchOrderStatusDTO(OrderStatusEnum.ERROR_EMPTY_CART, cartRequestDTO.getRequestId())));
            return;
        } catch (ProductDoesNotExistException e) {
            this.kafkaProducerSwitchStatus.send(new ProducerRecord<>(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC, new SwitchOrderStatusDTO(OrderStatusEnum.ERROR_INVALID_PRODUCT, cartRequestDTO.getRequestId())));
            return;
        } catch (CorruptedProductException e) {
            this.kafkaProducerSwitchStatus.send(new ProducerRecord<>(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC, new SwitchOrderStatusDTO(OrderStatusEnum.ERROR_CORRUPTED_PRODUCT, cartRequestDTO.getRequestId())));
            return;
        }
        List<CartItemDTO> cartItemDTOList = cartRequestDTO.getCartItemDTOList();
        PlaceOrderBuyerRequestDTO placeOrderBuyerRequestDTO = new PlaceOrderBuyerRequestDTO();
        Map<CountryEnum, List<CartItemDetailsDTO>> countryToCartItemsMap = new HashMap<>();
        List<CartItemDetailsDTO> cartItemDTOS;
        ProductDBO productDBOIter;
        CountryEnum countryIter;
        for (CartItemDTO cartItem : cartItemDTOList) {
            productDBOIter = this.productRepository.findBySku(cartItem.getSku());
            countryIter = CountryEnum.getCountryEnum(productDBOIter.getCountry());
            cartItemDTOS = countryToCartItemsMap.get(countryIter);
            if (cartItemDTOS == null || cartItemDTOS.isEmpty()) {
                cartItemDTOS = new ArrayList<>();
                countryToCartItemsMap.put(countryIter, cartItemDTOS);
            }
            cartItemDTOS.add(new CartItemDetailsDTO(cartItem, productDBOIter.getPrice(), productDBOIter.getWeight(), countryIter));
        }
        placeOrderBuyerRequestDTO.setMapItemsGroupedByCountry(countryToCartItemsMap);
        placeOrderBuyerRequestDTO.setBuyerId(cartRequestDTO.getBuyerId());
        placeOrderBuyerRequestDTO.setStatusUuid(cartRequestDTO.getRequestId());
        placeOrderBuyerRequestDTO.setDryRun(cartRequestDTO.getDryRun());
        this.kafkaProducerPlaceOrderTopic.send(new ProducerRecord<>(KafkaTopics.PLACE_ORDER_BUYER_TOPIC, placeOrderBuyerRequestDTO));

    }

    @KafkaListener(topics = KafkaTopics.REDUCE_PRODUCT_QUANTITIES_TOPIC, containerFactory = "reduceProductQuantitiesContainerFactory")
    @Transactional
    public void placeOrderFromCart(@Valid ReduceProductQuantitiesRequestDTO reduceProductQuantitiesRequestDTO) {
        ProductDBO productDBO;
        ZonedDateTime now = ZonedDateTime.now();
        for (CartItemDTO cartItemDTO : reduceProductQuantitiesRequestDTO.getCartItemDTOList()) {
            productDBO = this.productRepository.findBySku(cartItemDTO.getSku());
            productDBO.setStockAmount(productDBO.getStockAmount() - cartItemDTO.getQuantity());
            productDBO.setLastUpdateDate(now);
            this.productRepository.save(productDBO);
        }
    }

    private void sanitizePlaceOrderCartRequest(ValidateCartRequestDTO cartRequestDTO) throws CartIsEmptyForBuyerException, ProductDoesNotExistException, CorruptedProductException {
        if (cartRequestDTO == null) {
            throw new CartIsEmptyForBuyerException("product.api.placeOrder.error.emptyCart");
        }
        List<CartItemDTO> cartItemDTOList = cartRequestDTO.getCartItemDTOList();
        for (CartItemDTO cartItem : cartItemDTOList) {
            ProductDBO productDBO = this.productRepository.findBySku(cartItem.getSku());
            if (productDBO == null) {
                throw new ProductDoesNotExistException("product.api.placeOrder.error.nonExistentProduct");
            } else if (productDBO.getStockAmount().compareTo(cartItem.getQuantity()) < 0) {
                throw new CorruptedProductException("product.api.placeOrder.error.insufficientStockForAProduct");
            }
        }
    }

    @KafkaListener(topics = KafkaTopics.CHECK_ITEM_AVAILABILITY_REQUEST_TOPIC, containerFactory = "productAvailabilityRequestContainerFactory")
    @Transactional
    public void checkProductAvailability(@Valid ProductAvailabilityRequestDTO productAvailabilityRequestDTO) {
        UUID sku = productAvailabilityRequestDTO.getCartItemDTO().getSku();
        UUID requestId = productAvailabilityRequestDTO.getRequestId();
        ProductDBO productDBO = this.productRepository.findBySku(sku);
        if (productDBO == null) {
            this.kafkaProducerProductAvailability.send(new ProducerRecord<>(KafkaTopics.CHECK_ITEM_AVAILABILITY_RESPONSE_TOPIC, new ProductAvailabilityResponseDTO(requestId, ProductAvailabilityEnum.NOT_FOUND)));
            return;
        } else if (productDBO.getStockAmount() == 0) {
            this.kafkaProducerProductAvailability.send(new ProducerRecord<>(KafkaTopics.CHECK_ITEM_AVAILABILITY_RESPONSE_TOPIC, new ProductAvailabilityResponseDTO(requestId, ProductAvailabilityEnum.UNAVAILABLE)));
            return;
        }
        Integer stockAmount = productDBO.getStockAmount();
        Integer quantity = productAvailabilityRequestDTO.getCartItemDTO().getQuantity();
        if (quantity > stockAmount) {
            this.kafkaProducerProductAvailability.send(new ProducerRecord<>(KafkaTopics.CHECK_ITEM_AVAILABILITY_RESPONSE_TOPIC, new ProductAvailabilityResponseDTO(requestId, ProductAvailabilityEnum.EXCEEDS_AVAILABLE_QUANTITY)));
        } else {
            this.kafkaProducerProductAvailability.send(new ProducerRecord<>(KafkaTopics.CHECK_ITEM_AVAILABILITY_RESPONSE_TOPIC, new ProductAvailabilityResponseDTO(requestId, ProductAvailabilityEnum.AVAILABLE)));
        }
    }
}
