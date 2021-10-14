package com.ecommerce.product.service;

import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.dbo.ProductDBO;
import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.enums.EcommerceAuthorities;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.enums.OrderStatusEnum;
import com.ecommerce.sdk.exceptions.CartIsEmptyForBuyerException;
import com.ecommerce.sdk.exceptions.CorruptedProductException;
import com.ecommerce.sdk.exceptions.ProductAlreadyRegisteredException;
import com.ecommerce.sdk.exceptions.ProductDoesNotExistException;
import com.ecommerce.sdk.request.*;
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

    @Autowired
    public ProductService(ProductRepository productRepository, KafkaProducer<String, PlaceOrderBuyerRequestDTO> kafkaProducerPlaceOrderTopic,
                          @Qualifier(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC) KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus) {
        this.productRepository = productRepository;
        this.kafkaProducerPlaceOrderTopic = kafkaProducerPlaceOrderTopic;
        this.kafkaProducerSwitchStatus = kafkaProducerSwitchStatus;
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
    public void deleteProduct(Authentication authentication, ProductDBO productDBO) throws ProductDoesNotExistException, AccessDeniedException {
        if (!isSellerAdmin(authentication)) {
            UUID sellerId = getSellerId(authentication);
            ProductDBO productDBOFetchedFromDB = this.productRepository.findById(productDBO.getId()).orElse(null);
            if (productDBOFetchedFromDB == null) {
                throw new ProductDoesNotExistException("product.api.delete.error.productDoesNotExist");
            } else if (!productDBOFetchedFromDB.getSellerId().equals(sellerId)) {
                throw new AccessDeniedException("product.api.delete.error.productDoesNotBelongToThisOwner");
            }
        }
        this.productRepository.deleteById(productDBO.getId());
    }

    public void updateProduct(Authentication authentication, ProductDBO productDBO) throws ProductDoesNotExistException, AccessDeniedException {
        if (!isSellerAdmin(authentication)) {
            UUID sellerId = getSellerId(authentication);
            ProductDBO productDBOFetchedFromDB = this.productRepository.findById(productDBO.getId()).orElse(null);
            if (productDBOFetchedFromDB == null) {
                throw new ProductDoesNotExistException("product.api.delete.error.productDoesNotExist");
            } else if (!productDBOFetchedFromDB.getSellerId().equals(sellerId)) {
                throw new AccessDeniedException("product.api.delete.error.productDoesNotBelongToThisOwner");
            }
        }
        productDBO.setLastUpdateDate(ZonedDateTime.now());
        this.productRepository.save(productDBO);
    }

    private boolean isSellerAdmin(Authentication authentication) {
        List<GrantedAuthority> adminAuthority = authentication.getAuthorities().stream().filter(v -> ("ROLE_" + EcommerceAuthorities.SELLER_ADMIN.name()).equals(v.getAuthority())).collect(Collectors.toList());
        return adminAuthority != null && adminAuthority.size() > 0;
    }

    @KafkaListener(topics = KafkaTopics.PLACE_ORDER_CART_TOPIC)
    @Transactional
    public void placeOrderFromCart(@Valid ValidateCartRequestDTO cartRequestDTO) {
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
        Map<String, List<CartItemDTO>> countryToCartItemsMap = new HashMap<>();
        List<CartItemDTO> cartItemDTOS;
        ProductDBO productDBOIter;
        for (CartItemDTO cartItem : cartItemDTOList) {
            productDBOIter = this.productRepository.findBySku(cartItem.getSku());
            String country = productDBOIter.getCountry();
            cartItemDTOS = countryToCartItemsMap.get(country);
            if (cartItemDTOS == null || cartItemDTOS.isEmpty()) {
                cartItemDTOS = new ArrayList<>();
                countryToCartItemsMap.put(country, cartItemDTOS);
            }
            cartItemDTOS.add(cartItem);
        }
        placeOrderBuyerRequestDTO.setMapItemsGroupedByCountry(countryToCartItemsMap);
        placeOrderBuyerRequestDTO.setBuyerId(cartRequestDTO.getBuyerId());
        placeOrderBuyerRequestDTO.setStatusUuid(cartRequestDTO.getRequestId());
        this.kafkaProducerPlaceOrderTopic.send(new ProducerRecord<>(KafkaTopics.PLACE_ORDER_BUYER_TOPIC, placeOrderBuyerRequestDTO));

    }

    @KafkaListener(topics = KafkaTopics.REDUCE_PRODUCT_QUANTITIES_TOPIC, containerFactory = "reduceProductQuantitiesConsumerFactoryContainerFactory")
    @Transactional
    public void placeOrderFromCart(@Valid List<ReduceProductQuantitiesRequestDTO> reduceProductQuantitiesRequestDTOList) {
        ProductDBO productDBO;
        ZonedDateTime now = ZonedDateTime.now();
        for (ReduceProductQuantitiesRequestDTO requestDTO : reduceProductQuantitiesRequestDTOList) {
            productDBO = this.productRepository.findBySku(requestDTO.getSku());
            productDBO.setStockAmount(productDBO.getStockAmount() - requestDTO.getQuantity());
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
            } else if (!productDBO.getPrice().equals(cartItem.getPrice())) {
                throw new CorruptedProductException("product.api.placeOrder.error.priceMismatchForAProduct");
            } else if (productDBO.getStockAmount().compareTo(cartItem.getQuantity()) < 0) {
                throw new CorruptedProductException("product.api.placeOrder.error.insuficientStockForAProduct");
            }
        }
    }

}
