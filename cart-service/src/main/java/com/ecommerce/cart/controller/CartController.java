package com.ecommerce.cart.controller;

import com.ecommerce.cart.repository.dbo.CartItemDBO;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.exceptions.*;
import com.ecommerce.sdk.exceptions.dto.ExceptionReasonBodyDTO;
import com.ecommerce.sdk.response.RequestTokenDTO;
import org.keycloak.authorization.client.AuthorizationDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.UnknownHostException;
import java.util.UUID;

@RestController
@RequestMapping(value = "/v1")
public class CartController {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private CartService cartService;

    @Autowired
    public CartController(CartService sellerService) {
        this.cartService = sellerService;
    }

    @PostMapping(value = "/item", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RequestTokenDTO addItem(Authentication authentication, @Valid @RequestBody CartItemDTO requestDTO) {
        return this.cartService.addItem(authentication, requestDTO);
    }

    @GetMapping(value = "/item/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CartItemDTO checkIfItemAdditionOrUpdateWasSuccessful(Authentication authentication, @PathVariable UUID requestId) throws RequestNotFoundException, AccessDeniedException, ProductDoesNotExistException, NotEnoughStockException, ProductUnavailableException, UnknownCartItemStatusUpdateException {
        return this.cartService.checkIfItemAdditionOrUpdateWasSuccessful(authentication, requestId);
    }

    @PutMapping(value = "/item", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RequestTokenDTO updateItem(Authentication authentication, @Valid @RequestBody CartItemDTO cartDTO) throws CartItemDoesNotExistException {
        return this.cartService.updateItem(authentication, cartDTO);
    }

    @DeleteMapping(value = "/item/{sku}")
    public void deleteItem(Authentication authentication, @PathVariable UUID sku) throws CartItemDoesNotExistException {
        this.cartService.deleteItem(authentication, sku);
    }

    @GetMapping(value = "/clear")
    public void clearCart(Authentication authentication) throws CartIsEmptyForBuyerException {
        this.cartService.clearCart(authentication);
    }

    @GetMapping(value = "/place", produces = MediaType.APPLICATION_JSON_VALUE)
    public RequestTokenDTO placeOrder(Authentication authentication) throws AccessDeniedException {
        return this.cartService.placeOrder(authentication, false);
    }

    @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
    public RequestTokenDTO get(Authentication authentication) throws AccessDeniedException {
        return this.cartService.placeOrder(authentication, true);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ExceptionReasonBodyDTO badCredentials(AuthorizationDeniedException e) {
        String errorMessageId = "cart.api.error.invalidToken";
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(UnknownHostException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionReasonBodyDTO ssoServerUnavailable(UnknownHostException e) {
        String errorMessageId = "cart.api.error.ssoServerUnavailable";
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(RequestNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ExceptionReasonBodyDTO requestNotFound(RequestNotFoundException e) {
        String errorMessageId = e.getMessage();
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

    @ExceptionHandler(ProductDoesNotExistException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ExceptionReasonBodyDTO productDoesNotExist(ProductDoesNotExistException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(NotEnoughStockException.class)
    @ResponseStatus(value = HttpStatus.INSUFFICIENT_STORAGE)
    public ExceptionReasonBodyDTO notEnoughStock(NotEnoughStockException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(ProductUnavailableException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public ExceptionReasonBodyDTO productUnavailable(ProductUnavailableException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(UnknownCartItemStatusUpdateException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public ExceptionReasonBodyDTO unknownCartStatusUpdate(UnknownCartItemStatusUpdateException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ExceptionReasonBodyDTO invalidFormat() {
        String itemId = "cart.api.error.invalidRequestFormat";
        logger.error(itemId);
        return new ExceptionReasonBodyDTO(itemId);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionReasonBodyDTO invalidFormat(MethodArgumentNotValidException e) {
        String itemId = "cart.api.error.invalidRequestData";
        logger.error(itemId);
        return new ExceptionReasonBodyDTO(itemId);
    }
}
