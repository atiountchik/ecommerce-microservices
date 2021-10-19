package com.ecommerce.order.controller;

import com.ecommerce.order.repository.dbo.OrderDBO;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.sdk.domain.PaymentTableDTO;
import com.ecommerce.sdk.exceptions.CountryNotFoundException;
import com.ecommerce.sdk.exceptions.DeleteUserException;
import com.ecommerce.sdk.exceptions.InvalidInputException;
import com.ecommerce.sdk.exceptions.UserConflictException;
import com.ecommerce.sdk.exceptions.dto.ExceptionReasonBodyDTO;
import org.keycloak.authorization.client.AuthorizationDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/v1")
public class OrderController {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(value = "/status/{statusUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PaymentTableDTO getOrderStatus(Authentication authentication, @PathVariable UUID statusUuid) throws AccessDeniedException, CountryNotFoundException {
        return this.orderService.getOrderStatus(authentication, statusUuid);
    }

    @GetMapping(value = "/buyer/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrderDBO> getBuyerOrderHistory(Authentication authentication) {
        return this.orderService.getBuyerOrderHistory(authentication);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ExceptionReasonBodyDTO badCredentials(AuthorizationDeniedException e) {
        String errorMessageId = "order.api.error.invalidToken";
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ExceptionReasonBodyDTO forbidden(AccessDeniedException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(CountryNotFoundException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ExceptionReasonBodyDTO countryNotFound(CountryNotFoundException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(UnknownHostException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionReasonBodyDTO ssoServerUnavailable(UnknownHostException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ExceptionReasonBodyDTO invalidFormat() {
        String itemId = "order.api.error.invalidRequestFormat";
        logger.error(itemId);
        return new ExceptionReasonBodyDTO(itemId);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionReasonBodyDTO invalidFormat(MethodArgumentNotValidException e) {
        String itemId = "order.api.error.invalidRequestData";
        logger.error(itemId);
        return new ExceptionReasonBodyDTO(itemId);
    }
}
