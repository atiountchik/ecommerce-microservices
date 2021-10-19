package com.ecommerce.buyer.controller;

import com.ecommerce.buyer.repository.dbo.BuyerDBO;
import com.ecommerce.buyer.service.BuyerService;
import com.ecommerce.sdk.exceptions.*;
import com.ecommerce.sdk.exceptions.dto.ExceptionReasonBodyDTO;
import com.ecommerce.sdk.request.BuyerDTO;
import com.ecommerce.sdk.request.LoginRequestDTO;
import com.ecommerce.sdk.request.RegisterBuyerRequestDTO;
import com.ecommerce.sdk.response.LoginResponseDTO;
import org.keycloak.authorization.client.AuthorizationDeniedException;
import org.keycloak.authorization.client.util.HttpResponseException;
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
public class BuyerController {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private BuyerService buyerService;

    @Autowired
    public BuyerController(BuyerService buyerService) {
        this.buyerService = buyerService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO inputData) throws HttpResponseException, AuthorizationDeniedException, UnknownHostException {
        return this.buyerService.login(inputData);
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void register(@RequestBody @Valid RegisterBuyerRequestDTO inputData) throws InvalidInputException, UserConflictException, DeleteUserException, UnknownHostException {
        this.buyerService.register(inputData);
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public BuyerDBO getProfile(Authentication authentication) throws CountryNotFoundException {
        return this.buyerService.getProfile(authentication);
    }

    @PutMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BuyerDBO updateProfile(Authentication authentication, @Valid @RequestBody BuyerDTO buyerDTO) throws UserDoesNotExistException, AccessDeniedException {
        return this.buyerService.updateProfile(authentication, buyerDTO);
    }

    @GetMapping(value = "/profile/{buyerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BuyerDBO getSpecificBuyer(Authentication authentication, @PathVariable UUID buyerId) throws UserDoesNotExistException, AccessDeniedException {
        return this.buyerService.getProfile(authentication, buyerId);
    }

    @DeleteMapping(value = "/profile/{buyerId}")
    public void deleteProfile(Authentication authentication, @PathVariable UUID buyerId) throws UserDoesNotExistException, AccessDeniedException, UnknownHostException, DeleteUserException {
        this.buyerService.deleteProfile(authentication, buyerId);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ExceptionReasonBodyDTO badCredentials(AuthorizationDeniedException e) {
        String errorMessageId = "buyer.api.error.invalidCredentials";
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(HttpResponseException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ExceptionReasonBodyDTO badCredentials(HttpResponseException e) {
        String errorMessageId = "buyer.api.error.invalidCredentials";
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ExceptionReasonBodyDTO badCredentials(AccessDeniedException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(UnknownHostException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionReasonBodyDTO ssoServerUnavailable(UnknownHostException e) {
        String errorMessageId = "buyer.api.error.ssoServerUnavailable";
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(InvalidInputException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ExceptionReasonBodyDTO invalidInput(InvalidInputException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(UserConflictException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ExceptionReasonBodyDTO userConflict(UserConflictException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(UserDoesNotExistException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ExceptionReasonBodyDTO userDoesNotExist(UserDoesNotExistException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(DeleteUserException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ExceptionReasonBodyDTO deleteUserException(DeleteUserException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(CountryNotFoundException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ExceptionReasonBodyDTO countryNotFound(CountryNotFoundException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ExceptionReasonBodyDTO invalidFormat(HttpMessageNotReadableException e) {
        String itemId = "buyer.api.error.invalidRequestFormat";
        logger.error(itemId);
        return new ExceptionReasonBodyDTO(itemId);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionReasonBodyDTO invalidFormat(MethodArgumentNotValidException e) {
        String itemId = "buyer.api.error.invalidRequestData";
        logger.error(itemId);
        return new ExceptionReasonBodyDTO(itemId);
    }

}
