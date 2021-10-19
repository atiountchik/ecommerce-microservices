package com.ecommerce.seller.controller;

import com.ecommerce.sdk.exceptions.*;
import com.ecommerce.sdk.exceptions.dto.ExceptionReasonBodyDTO;
import com.ecommerce.sdk.request.LoginRequestDTO;
import com.ecommerce.sdk.request.RegisterSellerRequestDTO;
import com.ecommerce.sdk.request.SellerDTO;
import com.ecommerce.sdk.response.LoginResponseDTO;
import com.ecommerce.seller.repository.dbo.SellerDBO;
import com.ecommerce.seller.service.SellerService;
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
public class SellerController {
    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private SellerService sellerService;

    @Autowired
    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO inputData) throws HttpResponseException, AuthorizationDeniedException, UnknownHostException {
        return this.sellerService.login(inputData);
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void register(@RequestBody @Valid RegisterSellerRequestDTO inputData) throws InvalidInputException, UserConflictException, DeleteUserException, UnknownHostException, WorkingHoursException {
        this.sellerService.register(inputData);
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public SellerDBO getProfile(Authentication authentication) {
        return this.sellerService.getProfile(authentication);
    }

    @PutMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SellerDBO updateProfile(Authentication authentication, @Valid @RequestBody SellerDTO sellerDto) throws UserDoesNotExistException, AccessDeniedException, WorkingHoursException {
        return this.sellerService.updateProfile(authentication, sellerDto);
    }

    @GetMapping(value = "/profile/{sellerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SellerDBO getSpecificSeller(Authentication authentication, @PathVariable UUID sellerId) throws UserDoesNotExistException, AccessDeniedException {
        return this.sellerService.getProfile(authentication, sellerId);
    }

    @DeleteMapping(value = "/profile/{sellerId}")
    public void deleteProfile(Authentication authentication, @PathVariable UUID sellerId) throws UserDoesNotExistException, AccessDeniedException, UnknownHostException, DeleteUserException {
        this.sellerService.deleteProfile(authentication, sellerId);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ExceptionReasonBodyDTO badCredentials(AuthorizationDeniedException e) {
        String errorMessageId = "seller.api.error.invalidCredentials";
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

    @ExceptionHandler(HttpResponseException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ExceptionReasonBodyDTO badCredentials(HttpResponseException e) {
        String errorMessageId = "seller.api.error.invalidCredentials";
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(UnknownHostException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionReasonBodyDTO ssoServerUnavailable(UnknownHostException e) {
        String errorMessageId = "seller.api.error.ssoServerUnavailable";
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

    @ExceptionHandler(UserDoesNotExistException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ExceptionReasonBodyDTO userDoesNotExist(UserDoesNotExistException e) {
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

    @ExceptionHandler(DeleteUserException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ExceptionReasonBodyDTO deleteUserException(DeleteUserException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(WorkingHoursException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ExceptionReasonBodyDTO workingHoursError(WorkingHoursException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ExceptionReasonBodyDTO invalidFormat() {
        String itemId = "seller.api.error.invalidRequestFormat";
        logger.error(itemId);
        return new ExceptionReasonBodyDTO(itemId);
    }


    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionReasonBodyDTO invalidFormat(MethodArgumentNotValidException e) {
        String itemId = "seller.api.error.invalidRequestData";
        logger.error(itemId);
        return new ExceptionReasonBodyDTO(itemId);
    }
}
