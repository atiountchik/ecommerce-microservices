package com.ecommerce.seller.controller;

import com.ecommerce.sdk.exceptions.EcommerceException;
import com.ecommerce.sdk.request.LoginRequestDTO;
import com.ecommerce.sdk.request.RegisterSellerRequestDTO;
import com.ecommerce.sdk.response.LoginResponseDTO;
import com.ecommerce.seller.repository.dbo.SellerDBO;
import com.ecommerce.seller.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/v1")
public class SellerController {

    private SellerService sellerService;

    @Autowired
    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO inputData) {
        return this.sellerService.login(inputData);
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void register(@RequestBody @Valid RegisterSellerRequestDTO inputData) throws EcommerceException {
        this.sellerService.register(inputData);
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public SellerDBO getProfile(Authentication authentication) throws EcommerceException {
        return this.sellerService.getProfile(authentication);
    }
}
