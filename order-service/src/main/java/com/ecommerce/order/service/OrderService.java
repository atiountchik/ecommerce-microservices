package com.ecommerce.order.service;

import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.dbo.OrderDBO;
import com.ecommerce.order.repository.dbo.OrderItemDBO;
import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.domain.CartItemDetailsDTO;
import com.ecommerce.sdk.domain.PaymentTableDTO;
import com.ecommerce.sdk.domain.PaymentValuesDTO;
import com.ecommerce.sdk.enums.*;
import com.ecommerce.sdk.exceptions.CountryNotFoundException;
import com.ecommerce.sdk.exceptions.OrderNotFoundException;
import com.ecommerce.sdk.request.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
public class OrderService {

    public static final double UNIVERSAL_VAT = new BigDecimal(1.23d, new MathContext(3, RoundingMode.HALF_EVEN)).doubleValue();
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final KafkaProducer<String, PaymentRequestDTO> kafkaProducerPlaceOrderPaymentTopic;
    private final KafkaProducer<String, ClearCartRequestDTO> kafkaProducerClearCartTopic;
    private final KafkaProducer<String, ReduceProductQuantitiesRequestDTO> kafkaProducerReduceProductQuantitiesTopic;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        KafkaProducer<String, PaymentRequestDTO> kafkaProducerPlaceOrderPaymentTopic,
                        @Qualifier(KafkaTopics.CLEAR_CART_TOPIC) KafkaProducer<String, ClearCartRequestDTO> kafkaProducerClearCartTopic,
                        @Qualifier(KafkaTopics.REDUCE_PRODUCT_QUANTITIES_TOPIC) KafkaProducer<String, ReduceProductQuantitiesRequestDTO> kafkaProducerReduceProductQuantitiesTopic) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.kafkaProducerPlaceOrderPaymentTopic = kafkaProducerPlaceOrderPaymentTopic;
        this.kafkaProducerClearCartTopic = kafkaProducerClearCartTopic;
        this.kafkaProducerReduceProductQuantitiesTopic = kafkaProducerReduceProductQuantitiesTopic;
    }

    public PaymentTableDTO getOrderStatus(Authentication authentication, UUID statusUuid) throws AccessDeniedException, CountryNotFoundException {
        UUID buyerId = UUID.fromString(authentication.getName());
        OrderDBO orderDBO = this.orderRepository.findByStatusUuid(statusUuid);
        if (!isBuyerAdmin(authentication) && !buyerId.equals(orderDBO.getBuyerId())) {
            throw new AccessDeniedException("order.api.status.error.notAuthorizedAccessToOrderStatus");
        }
        PaymentTableDTO paymentTableDTO = new PaymentTableDTO();
        List<CartItemDetailsDTO> allItems = new ArrayList<>();
        Map<CountryEnum, List<CartItemDetailsDTO>> mapItemsGroupedByCountry = new HashMap<>();
        CountryEnum country;
        List<OrderItemDBO> orderItemDBOList = orderDBO.getOrderItemDBOList();
        for (OrderItemDBO orderItemDBO : orderItemDBOList) {
            country = CountryEnum.getCountryEnum(orderItemDBO.getCountry());
            List<CartItemDetailsDTO> cartItemDetailsDTOS = mapItemsGroupedByCountry.get(country);
            if (cartItemDetailsDTOS == null) {
                cartItemDetailsDTOS = new ArrayList<>();
                mapItemsGroupedByCountry.put(country, cartItemDetailsDTOS);
            }
            cartItemDetailsDTOS.add(orderItemDBO.toCartItemDetailsDTO());
            allItems.add(orderItemDBO.toCartItemDetailsDTO());
        }
        PaymentValuesDTO paymentValuesDTO = computePaymentValues(mapItemsGroupedByCountry, CountryEnum.getCountryEnum(orderDBO.getBuyerCountry()), null);
        paymentTableDTO.setItems(allItems);
        paymentTableDTO.setPaymentData(paymentValuesDTO);
        paymentTableDTO.setStatus(OrderStatusEnum.getOrderStatusEnum(orderDBO.getStatus()));
        return paymentTableDTO;
    }

    public List<OrderDBO> getBuyerOrderHistory(Authentication authentication) {
        UUID buyerId = UUID.fromString(authentication.getName());
        return this.orderRepository.findByBuyerIdAndStatus(buyerId, OrderStatusEnum.PROCESSED.name());
    }

    private boolean isBuyerAdmin(Authentication authentication) {
        List<GrantedAuthority> adminAuthority = authentication.getAuthorities().stream().filter(v -> ("ROLE_" + EcommerceAuthorities.BUYER_ADMIN.name()).equals(v.getAuthority())).collect(Collectors.toList());
        return adminAuthority != null && adminAuthority.size() > 0;
    }


    @KafkaListener(topics = KafkaTopics.PLACE_ORDER_TOPIC)
    @Transactional
    public void placeOrder(@Valid PlaceOrderRequestDTO placeOrderRequestDTO) throws IllegalArgumentException {
        if (placeOrderRequestDTO == null) {
            throw new IllegalArgumentException("order.api.placeOrder.error.nullInput");
        }
        UUID buyerId = placeOrderRequestDTO.getPlaceOrderBuyerRequestDTO().getBuyerId();
        Boolean isDryRun = placeOrderRequestDTO.getPlaceOrderBuyerRequestDTO().getDryRun();
        if (isDryRun) {
            List<OrderDBO> dryRunList = this.orderRepository.findByBuyerIdAndStatus(buyerId, OrderStatusEnum.DRY_RUN.name());
            dryRunList.forEach(orderDBO -> this.orderRepository.delete(orderDBO));
        }
        CountryEnum country = placeOrderRequestDTO.getCountry();
        Map<CountryEnum, List<CartItemDetailsDTO>> mapItemsGroupedByCountry = placeOrderRequestDTO.getPlaceOrderBuyerRequestDTO().getMapItemsGroupedByCountry();

        ZonedDateTime now = ZonedDateTime.now();
        UUID statusUuid = placeOrderRequestDTO.getPlaceOrderBuyerRequestDTO().getStatusUuid();
        OrderDBO orderDBO = this.orderRepository.findByStatusUuid(statusUuid);
        if (orderDBO == null) {
            OrderStatusEnum orderStatusEnum;
            if (isDryRun) {
                orderStatusEnum = OrderStatusEnum.DRY_RUN;
            } else {
                orderStatusEnum = OrderStatusEnum.PENDING_INFO_CONFIRMATION;
            }
            orderDBO = new OrderDBO(0l, now, now, orderStatusEnum, statusUuid, UNIVERSAL_VAT, country, buyerId);
            orderDBO = this.orderRepository.save(orderDBO);
        }

        PaymentValuesDTO paymentValuesDTO = computePaymentValues(mapItemsGroupedByCountry, country, orderDBO);
        if (!isDryRun) {
            PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(paymentValuesDTO.getTotal(), country, statusUuid, buyerId);
            this.kafkaProducerPlaceOrderPaymentTopic.send(new ProducerRecord<>(KafkaTopics.PLACE_ORDER_PAYMENT_TOPIC, paymentRequestDTO));
            orderDBO.setStatus(OrderStatusEnum.PENDING_PAYMENT);
            orderDBO.setLastUpdateDate(now);
            orderRepository.save(orderDBO);
        }
    }

    private PaymentValuesDTO computePaymentValues(Map<CountryEnum, List<CartItemDetailsDTO>> mapItemsGroupedByCountry, CountryEnum buyerCountry, OrderDBO orderDBO) {
        Double deliveryFeeIter;
        CountryEnum countryIter;
        List<CartItemDetailsDTO> listOfItemsOfCountry;
        Double total;
        Double subtotal = 0.0d;
        Double subtotalIter;
        Double deliveryFee = 0.0d;
        OrderItemDBO orderItemDBO;
        ZonedDateTime now = ZonedDateTime.now();
        for (Map.Entry<CountryEnum, List<CartItemDetailsDTO>> entry : mapItemsGroupedByCountry.entrySet()) {
            countryIter = entry.getKey();
            listOfItemsOfCountry = entry.getValue();
            for (CartItemDetailsDTO cartItemDTO : listOfItemsOfCountry) {
                Integer quantity = cartItemDTO.getItem().getQuantity();
                subtotalIter = cartItemDTO.getPrice() * quantity;
                if (buyerCountry.equals(countryIter)) {
                    deliveryFeeIter = FeesEnum.NATIONAL_STANDARD_FEE.getFee() + FeesEnum.NATIONAL_PER_KILO_FEE.getFee() * quantity * cartItemDTO.getWeight();
                } else {
                    deliveryFeeIter = FeesEnum.INTERNATIONAL_STANDARD_FEE.getFee() + FeesEnum.INTERNATIONAL_PER_KILO_FEE.getFee() * quantity * cartItemDTO.getWeight();
                }
                deliveryFee += deliveryFeeIter;
                subtotal += subtotalIter + deliveryFeeIter;
                if (orderDBO != null) {
                    orderItemDBO = new OrderItemDBO(0l, now, now, cartItemDTO.getItem().getSku(), cartItemDTO.getPrice(), cartItemDTO.getItem().getQuantity(), cartItemDTO.getWeight(), cartItemDTO.getCountry(), orderDBO);
                    orderItemRepository.save(orderItemDBO);
                }
            }
        }
        total = UNIVERSAL_VAT * subtotal;
        return new PaymentValuesDTO(UNIVERSAL_VAT - 1, deliveryFee, subtotal, total);
    }

    @KafkaListener(topics = KafkaTopics.SWITCH_ORDER_STATUS_TOPIC, containerFactory = "switchOrderStatusContainerFactory")
    @Transactional
    public void switchOrderStatus(@Valid SwitchOrderStatusDTO switchOrderStatusDTO) throws OrderNotFoundException {
        OrderDBO orderDBO = this.orderRepository.findByStatusUuid(switchOrderStatusDTO.getStatusUuid());
        ZonedDateTime now = ZonedDateTime.now();
        if (orderDBO == null && switchOrderStatusDTO.getOrderStatusEnum() != OrderStatusEnum.PENDING_INFO_CONFIRMATION && switchOrderStatusDTO.getOrderStatusEnum() != OrderStatusEnum.DRY_RUN) {
            throw new OrderNotFoundException("order.api.switchStatus.error.orderWithCorruptedStatus");
        } else if (orderDBO != null) {
            orderDBO.setStatus(switchOrderStatusDTO.getOrderStatusEnum());
            orderDBO.setLastUpdateDate(now);
            orderDBO = this.orderRepository.save(orderDBO);
        }
        if (switchOrderStatusDTO.getOrderStatusEnum() == OrderStatusEnum.PROCESSED) {
            this.kafkaProducerClearCartTopic.send(new ProducerRecord<>(KafkaTopics.CLEAR_CART_TOPIC, new ClearCartRequestDTO(orderDBO.getBuyerId())));
            ReduceProductQuantitiesRequestDTO reduceProductQuantitiesRequestDTO = new ReduceProductQuantitiesRequestDTO();
            List<CartItemDTO> cartItemDTOList = new ArrayList<>();
            orderDBO.getOrderItemDBOList().forEach(item -> cartItemDTOList.add(new CartItemDTO(item.getSku(), item.getQuantity())));
            reduceProductQuantitiesRequestDTO.setCartItemDTOList(cartItemDTOList);
            this.kafkaProducerReduceProductQuantitiesTopic.send(new ProducerRecord<>(KafkaTopics.REDUCE_PRODUCT_QUANTITIES_TOPIC, reduceProductQuantitiesRequestDTO));
        }
    }

}
