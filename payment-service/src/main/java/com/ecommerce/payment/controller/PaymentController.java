package com.ecommerce.payment.controller;

import com.ecommerce.payment.repository.dbo.PaymentDBO;
import com.ecommerce.payment.service.PaymentService;
import com.ecommerce.sdk.exceptions.PaymentException;
import com.ecommerce.sdk.exceptions.dto.ExceptionReasonBodyDTO;
import com.ecommerce.sdk.request.ConfirmPaymentDTO;
import com.stripe.exception.StripeException;
import org.keycloak.authorization.client.AuthorizationDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/v1")
@Validated
public class PaymentController {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(value = "/pay/{statusUuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void pay(Authentication authentication, @Valid @RequestBody ConfirmPaymentDTO confirmPaymentDTO, @PathVariable @NotNull UUID statusUuid) throws AccessDeniedException, StripeException, PaymentException {
        this.paymentService.pay(authentication, confirmPaymentDTO, statusUuid);
    }

    @GetMapping(value = "/pay/buyer/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public List<PaymentDBO> listBuyerPayments(Authentication authentication) {
        return this.paymentService.listBuyerPayments(authentication);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ExceptionReasonBodyDTO badCredentials(AuthorizationDeniedException e) {
        String errorMessageId = "payment.api.error.invalidToken";
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(UnknownHostException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionReasonBodyDTO ssoServerUnavailable(UnknownHostException e) {
        String errorMessageId = "payment.api.error.ssoServerUnavailable";
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }


    @ExceptionHandler(StripeException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ExceptionReasonBodyDTO stripeException(StripeException e) {
        String errorMessageId = "payment.api.pay.error.stripePaymentFailed";
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(PaymentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ExceptionReasonBodyDTO paymentException(PaymentException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ExceptionReasonBodyDTO invalidFormat() {
        String itemId = "payment.api.error.invalidRequestFormat";
        logger.error(itemId);
        return new ExceptionReasonBodyDTO(itemId);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionReasonBodyDTO invalidFormat(MethodArgumentNotValidException e) {
        String itemId = "payment.api.error.invalidRequestData";
        logger.error(itemId);
        return new ExceptionReasonBodyDTO(itemId);
    }
}
