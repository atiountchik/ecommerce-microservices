package com.ecommerce.payment.service;

import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.repository.dbo.PaymentDBO;
import com.ecommerce.sdk.enums.EcommerceAuthorities;
import com.ecommerce.sdk.enums.KafkaTopics;
import com.ecommerce.sdk.enums.OrderStatusEnum;
import com.ecommerce.sdk.enums.PaymentStatusEnum;
import com.ecommerce.sdk.exceptions.PaymentException;
import com.ecommerce.sdk.request.ConfirmPaymentDTO;
import com.ecommerce.sdk.request.PaymentRequestDTO;
import com.ecommerce.sdk.request.SwitchOrderStatusDTO;
import com.stripe.exception.StripeException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
public class PaymentService {

    public static final double PAYMENT_AMOUNT_SENSITIVITY = 1e-2;

    private final StripeService stripeService;
    private final PaymentRepository paymentRepository;
    private final KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus;

    @Autowired
    public PaymentService(StripeService stripeService, PaymentRepository paymentRepository, KafkaProducer<String, SwitchOrderStatusDTO> kafkaProducerSwitchStatus) {
        this.stripeService = stripeService;
        this.paymentRepository = paymentRepository;
        this.kafkaProducerSwitchStatus = kafkaProducerSwitchStatus;
    }

    @Transactional
    public void pay(Authentication authentication, @Valid ConfirmPaymentDTO confirmPaymentDTO, @NotNull UUID statusUuid) throws StripeException, AccessDeniedException, PaymentException {
        PaymentDBO paymentDBO = this.paymentRepository.findByStatusUuid(statusUuid);
        if (paymentDBO == null) {
            throw new PaymentException("payment.api.placeOrder.error.paymentDoesNotExist");
        }
        if (!isBuyerAdmin(authentication)) {
            UUID buyerId = UUID.fromString(authentication.getName());
            if (!paymentDBO.getBuyerId().equals(buyerId)) {
                throw new AccessDeniedException("payment.api.placeOrder.error.notAuthorized");
            }
        }
        if (Math.abs(paymentDBO.getAmount() - confirmPaymentDTO.getAmount()) > PAYMENT_AMOUNT_SENSITIVITY) {
            throw new PaymentException("payment.api.placeOrder.error.amountMismatch");
        }
        String chargeId = this.stripeService.proceedWithPayment(confirmPaymentDTO, statusUuid);
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

    public List<PaymentDBO> listBuyerPayments(Authentication authentication) {
        UUID buyerId = getUserId(authentication);
        if (isBuyerAdmin(authentication)) {
            return this.paymentRepository.findAll();
        } else {
            return this.paymentRepository.findByBuyerId(buyerId);
        }
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
