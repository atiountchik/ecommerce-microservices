package com.ecommerce.order.service;

import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.dbo.OrderDBO;
import com.ecommerce.order.repository.dbo.OrderItemDBO;
import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.enums.FeesEnum;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.enums.OrderStatusEnum;
import com.ecommerce.sdk.exceptions.OrderAlreadyBeingPrcessedException;
import com.ecommerce.sdk.exceptions.OrderNotFoundException;
import com.ecommerce.sdk.request.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.nio.file.AccessDeniedException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Validated
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final KafkaProducer<String, PaymentRequestDTO> kafkaProducerPlaceOrderPaymentTopic;
    private final KafkaProducer<String, ClearCartRequestDTO> kafkaProducerClearCartTopic;
    private final KafkaProducer<String, List<ReduceProductQuantitiesRequestDTO>> kafkaProducerReduceProductQuantitiesTopic;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        KafkaProducer<String, PaymentRequestDTO> kafkaProducerPlaceOrderPaymentTopic,
                        @Qualifier(KafkaTopics.CLEAR_CART_TOPIC) KafkaProducer<String, ClearCartRequestDTO> kafkaProducerClearCartTopic,
                        @Qualifier(KafkaTopics.REDUCE_PRODUCT_QUANTITIES_TOPIC) KafkaProducer<String, List<ReduceProductQuantitiesRequestDTO>> kafkaProducerReduceProductQuantitiesTopic) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.kafkaProducerPlaceOrderPaymentTopic = kafkaProducerPlaceOrderPaymentTopic;
        this.kafkaProducerClearCartTopic = kafkaProducerClearCartTopic;
        this.kafkaProducerReduceProductQuantitiesTopic = kafkaProducerReduceProductQuantitiesTopic;
    }

    public OrderDBO getOrderStatus(Authentication authentication, UUID statusUuid) throws AccessDeniedException {
        UUID buyerId = UUID.fromString(authentication.getName());
        OrderDBO orderDBO = this.orderRepository.findByStatusUuid(statusUuid);
        if (!buyerId.equals(orderDBO.getBuyerId())) {
            throw new AccessDeniedException("order.api.status.error.notAuthorizedAccessToOrderStatus");
        }
        return orderDBO;
    }

    public List<OrderDBO> getBuyerOrderHistory(Authentication authentication) {
        UUID buyerId = UUID.fromString(authentication.getName());
        return this.orderRepository.findByBuyerIdAndStatus(buyerId, OrderStatusEnum.PROCESSED.name());
    }


    @KafkaListener(topics = KafkaTopics.PLACE_ORDER_TOPIC)
    @Transactional
    public void placeOrder(@Valid PlaceOrderRequestDTO placeOrderBuyerRequestDTO) throws IllegalArgumentException, OrderNotFoundException {
        if (placeOrderBuyerRequestDTO == null) {
            throw new IllegalArgumentException("order.api.placeOrder.error.nullInput");
        }
        String country = placeOrderBuyerRequestDTO.getCountry();
        Map<String, List<CartItemDTO>> mapItemsGroupedByCountry = placeOrderBuyerRequestDTO.getPlaceOrderBuyerRequestDTO().getMapItemsGroupedByCountry();
        String countryIter;
        List<CartItemDTO> listOfItemsOfCountry;
        Double total;
        Double subtotal = 0.0d;
        Double subtotalIter;
        Double deliveryFee = 0.0d;
        Double deliveryFeeIter = 0.0d;
        ZonedDateTime now = ZonedDateTime.now();
        UUID statusUuid = placeOrderBuyerRequestDTO.getPlaceOrderBuyerRequestDTO().getStatusUuid();
        OrderDBO orderDBO = this.orderRepository.findByStatusUuid(statusUuid);
        if (orderDBO == null) {
            orderDBO = new OrderDBO(0l, now, now, OrderStatusEnum.PENDING_INFO_CONFIRMATION, statusUuid, 1.23d, placeOrderBuyerRequestDTO.getPlaceOrderBuyerRequestDTO().getBuyerId());
            orderDBO = this.orderRepository.save(orderDBO);
        }
        OrderItemDBO orderItemDBO;
        for (Map.Entry<String, List<CartItemDTO>> entry : mapItemsGroupedByCountry.entrySet()) {
            countryIter = entry.getKey();
            listOfItemsOfCountry = entry.getValue();
            for (CartItemDTO cartItemDTO : listOfItemsOfCountry) {
                Integer quantity = cartItemDTO.getQuantity();
                subtotalIter = cartItemDTO.getPrice() * quantity;
                subtotal += subtotalIter;
                if (country.equals(countryIter)) {
                    deliveryFeeIter = FeesEnum.NATIONAL_STANDARD_FEE.getFee() + FeesEnum.NATIONAL_PER_KILO_FEE.getFee() * quantity * cartItemDTO.getWeight();
                } else {
                    deliveryFeeIter = FeesEnum.INTERNATIONAL_STANDARD_FEE.getFee() + FeesEnum.INTERNATIONAL_PER_KILO_FEE.getFee() * quantity * cartItemDTO.getWeight();
                }
                deliveryFee += deliveryFeeIter;
                orderItemDBO = new OrderItemDBO(0l, now, now, cartItemDTO.getSku(), cartItemDTO.getPrice(), cartItemDTO.getQuantity(), cartItemDTO.getWeight(), orderDBO);
                orderItemRepository.save(orderItemDBO);
            }
        }
        total = orderDBO.getVat() * (subtotal + deliveryFee);
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(total, placeOrderBuyerRequestDTO.getCountry(), orderDBO.getStatusUuid(), placeOrderBuyerRequestDTO.getPlaceOrderBuyerRequestDTO().getBuyerId());
        this.kafkaProducerPlaceOrderPaymentTopic.send(new ProducerRecord<>(KafkaTopics.PLACE_ORDER_PAYMENT_TOPIC, paymentRequestDTO));
        orderDBO.setStatus(OrderStatusEnum.PENDING_PAYMENT);
        orderDBO.setLastUpdateDate(now);
        orderRepository.save(orderDBO);
    }

    @KafkaListener(topics = KafkaTopics.SWITCH_ORDER_STATUS_TOPIC, containerFactory = "switchOrderStatusContainerFactory")
    @Transactional
    public void switchOrderStatus(@Valid SwitchOrderStatusDTO switchOrderStatusDTO) throws OrderNotFoundException {
        OrderDBO orderDBO = this.orderRepository.findByStatusUuid(switchOrderStatusDTO.getStatusUuid());
        ZonedDateTime now = ZonedDateTime.now();
        if (orderDBO == null && switchOrderStatusDTO.getOrderStatusEnum() != OrderStatusEnum.PENDING_INFO_CONFIRMATION) {
            throw new OrderNotFoundException("order.api.switchStatus.error.orderWithCorruptedStatus");
        } else if (orderDBO != null) {
            orderDBO.setStatus(switchOrderStatusDTO.getOrderStatusEnum());
            orderDBO.setLastUpdateDate(now);
            orderDBO = this.orderRepository.save(orderDBO);
        }
        if (switchOrderStatusDTO.getOrderStatusEnum() == OrderStatusEnum.PROCESSED) {
            this.kafkaProducerClearCartTopic.send(new ProducerRecord<>(KafkaTopics.CLEAR_CART_TOPIC, new ClearCartRequestDTO(orderDBO.getBuyerId())));
            List<ReduceProductQuantitiesRequestDTO> listOfItemsToReduceQuantity = new ArrayList<>();
            orderDBO.getOrderItemDBOList().forEach(item -> listOfItemsToReduceQuantity.add(new ReduceProductQuantitiesRequestDTO(item.getSku(), item.getQuantity())));
            this.kafkaProducerReduceProductQuantitiesTopic.send(new ProducerRecord<>(KafkaTopics.REDUCE_PRODUCT_QUANTITIES_TOPIC, listOfItemsToReduceQuantity));
        }
    }

}
