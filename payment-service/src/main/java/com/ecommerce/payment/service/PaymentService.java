package com.ecommerce.payment.service;

import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.repository.dbo.PaymentDBO;
import com.ecommerce.sdk.enums.EcommerceAuthorities;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.enums.OrderStatusEnum;
import com.ecommerce.sdk.enums.PaymentStatusEnum;
import com.ecommerce.sdk.exceptions.OrderNotFoundException;
import com.ecommerce.sdk.exceptions.PaymentException;
import com.ecommerce.sdk.request.ConfirmPaymentDTO;
import com.ecommerce.sdk.request.PaymentRequestDTO;
import com.ecommerce.sdk.request.PlaceOrderRequestDTO;
import com.ecommerce.sdk.request.SwitchOrderStatusDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Token;
import com.stripe.param.ChargeCreateParams;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.nio.file.AccessDeniedException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
public class PaymentService {

    @Value("${stripe.apiKey}")
    private String stripeApiKey;

    private final PaymentRepository paymentRepository;
    private final KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus) {
        this.paymentRepository = paymentRepository;
        this.kafkaProducerSwitchStatus = kafkaProducerSwitchStatus;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = this.stripeApiKey;
    }

    @Transactional
    public void pay(Authentication authentication, @Valid ConfirmPaymentDTO confirmPaymentDTO, @NotNull UUID statusUuid) throws StripeException, AccessDeniedException, PaymentException {
        PaymentDBO paymentDBO = this.paymentRepository.findByStatusUuid(statusUuid);
        if (!isBuyerAdmin(authentication)) {
            if (paymentDBO == null) {
                throw new PaymentException("payment.api.placeOrder.error.paymentDoesNotExist");
            }
            UUID buyerId = UUID.fromString(authentication.getName());
            if (!paymentDBO.getBuyerId().equals(buyerId)) {
                throw new AccessDeniedException("payment.api.placeOrder.error.notAuthorized");
            }
        }
        if (!paymentDBO.getAmount().equals(confirmPaymentDTO.getAmount())) {
            throw new PaymentException("payment.api.placeOrder.error.amountMismatch");
        }
        Map<String, Object> card = new HashMap<>();
        card.put("number", confirmPaymentDTO.getCardNumber());
        card.put("exp_month", confirmPaymentDTO.getExpMonth());
        card.put("exp_year", confirmPaymentDTO.getExpYear());
        card.put("cvc", Integer.toString(confirmPaymentDTO.getCvc()));
        Map<String, Object> params = new HashMap<>();
        params.put("card", card);

        Token token = Token.create(params);

        ChargeCreateParams params2 =
                ChargeCreateParams.builder()
                        .setAmount(Math.round(confirmPaymentDTO.getAmount() * 100))
                        .setCurrency("usd")
                        // .setDescription("Example charge")
                        .setSource(token.getId())
                        .putMetadata("status_uuid", statusUuid.toString())
                        .build();

        Charge charge = Charge.create(params2);
        String chargeId = charge.getId();
        paymentDBO.setChargeId(chargeId);
        paymentDBO.setLastUpdateDate(ZonedDateTime.now());
        paymentDBO.setPaymentStatus(PaymentStatusEnum.PROCESSED.name());
        this.paymentRepository.save(paymentDBO);
        this.kafkaProducerSwitchStatus.send(new ProducerRecord<>(KafkaTopics.SWITCH_ORDER_STATUS_TOPIC, new SwitchOrderStatusDTO(OrderStatusEnum.PROCESSED, statusUuid)));
    }

    private boolean isBuyerAdmin(Authentication authentication) {
        List<GrantedAuthority> adminAuthority = authentication.getAuthorities().stream().filter(v -> ("ROLE_" + EcommerceAuthorities.BUYER_ADMIN.name()).equals(v.getAuthority())).collect(Collectors.toList());
        return adminAuthority != null && adminAuthority.size() > 0;
    }

    @KafkaListener(topics = KafkaTopics.PLACE_ORDER_PAYMENT_TOPIC)
    @Transactional
    public void placeOrder(@Valid PaymentRequestDTO paymentRequestDTO) throws IllegalArgumentException {
        PaymentDBO paymentDBO = this.paymentRepository.findByStatusUuid(paymentRequestDTO.getStatusUuid());
        if (paymentDBO != null) {
            throw new IllegalArgumentException("payment.api.placeOrder.error.paymentAlreadyProcessed");
        }
        ZonedDateTime now = ZonedDateTime.now();
        paymentDBO = new PaymentDBO(0l, now, now, null, paymentRequestDTO.getAmount(), PaymentStatusEnum.PENDING, paymentRequestDTO.getStatusUuid(), paymentRequestDTO.getBuyerId());
        this.paymentRepository.save(paymentDBO);
    }
}
