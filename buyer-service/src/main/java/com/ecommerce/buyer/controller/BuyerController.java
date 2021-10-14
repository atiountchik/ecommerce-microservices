package com.ecommerce.buyer.controller;

import com.ecommerce.buyer.repository.dbo.BuyerDBO;
import com.ecommerce.buyer.service.BuyerService;
import com.ecommerce.sdk.exceptions.EcommerceException;
import com.ecommerce.sdk.request.LoginRequestDTO;
import com.ecommerce.sdk.request.RegisterBuyerRequestDTO;
import com.ecommerce.sdk.response.LoginResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/v1")
public class BuyerController {

    private BuyerService buyerService;

    @Autowired
    public BuyerController(BuyerService buyerService) {
        this.buyerService = buyerService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO inputData) {
        return this.buyerService.login(inputData);
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void register(@RequestBody @Valid RegisterBuyerRequestDTO inputData) throws EcommerceException {
        this.buyerService.register(inputData);
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public BuyerDBO getProfile(Authentication authentication) throws EcommerceException {
        return this.buyerService.getProfile(authentication);
    }
}
