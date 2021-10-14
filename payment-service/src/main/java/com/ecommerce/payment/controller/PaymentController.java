package com.ecommerce.payment.controller;

import com.ecommerce.payment.repository.dbo.PaymentDBO;
import com.ecommerce.payment.service.PaymentService;
import com.ecommerce.sdk.exceptions.PaymentException;
import com.ecommerce.sdk.exceptions.ProductAlreadyRegisteredException;
import com.ecommerce.sdk.exceptions.ProductDoesNotExistException;
import com.ecommerce.sdk.request.ConfirmPaymentDTO;
import com.ecommerce.sdk.request.RegisterNewProductRequestDTO;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/v1")
@Validated
public class PaymentController {

    private PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(value = "/pay/{statusUuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void pay(Authentication authentication, @Valid @RequestBody ConfirmPaymentDTO confirmPaymentDTO, @PathVariable @NotNull UUID statusUuid) throws AccessDeniedException, StripeException, java.nio.file.AccessDeniedException, PaymentException {
        this.paymentService.pay(authentication, confirmPaymentDTO, statusUuid);
    }

}
