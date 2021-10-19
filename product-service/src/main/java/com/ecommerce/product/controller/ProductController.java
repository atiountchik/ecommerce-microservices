package com.ecommerce.product.controller;

import com.ecommerce.product.repository.dbo.ProductDBO;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.sdk.exceptions.ProductAlreadyRegisteredException;
import com.ecommerce.sdk.exceptions.ProductDoesNotExistException;
import com.ecommerce.sdk.exceptions.UserDoesNotExistException;
import com.ecommerce.sdk.exceptions.dto.ExceptionReasonBodyDTO;
import com.ecommerce.sdk.request.ProductDTO;
import com.ecommerce.sdk.request.RegisterNewProductRequestDTO;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/v1")
public class ProductController {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    private ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductDBO> getAllProducts() {
        return this.productService.getAllProducts();
    }

    @PostMapping(value = "/seller/products", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductDBO registerProduct(Authentication authentication, @Valid @RequestBody RegisterNewProductRequestDTO newProductRequestDTO) throws AccessDeniedException, ProductAlreadyRegisteredException {
        return this.productService.registerProduct(authentication, newProductRequestDTO);
    }

    @GetMapping(value = "/seller/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductDBO> getSellerProducts(Authentication authentication) {
        return this.productService.getSellerProducts(authentication);
    }

    @DeleteMapping(value = "/seller/products/{sku}")
    public void deleteProduct(Authentication authentication, @PathVariable UUID sku) throws ProductDoesNotExistException, AccessDeniedException {
        this.productService.deleteProduct(authentication, sku);
    }

    @PutMapping(value = "/seller/products", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProductDBO updateProduct(Authentication authentication, @Valid @RequestBody ProductDTO productDTO) throws UserDoesNotExistException, AccessDeniedException, UserDoesNotExistException {
        return this.productService.updateProduct(authentication, productDTO);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ExceptionReasonBodyDTO badCredentials(AuthorizationDeniedException e) {
        String errorMessageId = "product.api.error.invalidToken";
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(UnknownHostException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionReasonBodyDTO ssoServerUnavailable(UnknownHostException e) {
        String errorMessageId = "product.api.error.ssoServerUnavailable";
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ExceptionReasonBodyDTO productAlreadyRegistered(AccessDeniedException e) {
        String errorMessageId = e.getMessage();
        logger.error("Message ID: " + errorMessageId);
        return new ExceptionReasonBodyDTO(errorMessageId);
    }


    @ExceptionHandler(ProductAlreadyRegisteredException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ExceptionReasonBodyDTO productAlreadyRegistered(ProductAlreadyRegisteredException e) {
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

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ExceptionReasonBodyDTO invalidFormat() {
        String itemId = "product.api.error.invalidRequestFormat";
        logger.error(itemId);
        return new ExceptionReasonBodyDTO(itemId);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionReasonBodyDTO invalidFormat(MethodArgumentNotValidException e) {
        String itemId = "product.api.error.invalidRequestData";
        logger.error(itemId);
        return new ExceptionReasonBodyDTO(itemId);
    }
}
