package com.ecommerce.cart.controller;

import com.ecommerce.cart.repository.dbo.CartDBO;
import com.ecommerce.cart.service.CartService;
import com.ecommerce.sdk.domain.CartItemDTO;
import com.ecommerce.sdk.exceptions.CartIsEmptyForBuyerException;
import com.ecommerce.sdk.exceptions.CartItemDoesNotExistException;
import com.ecommerce.sdk.response.RequestTokenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/v1")
public class CartController {

    private CartService cartService;

    @Autowired
    public CartController(CartService sellerService) {
        this.cartService = sellerService;
    }

    @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CartDBO> getCart(Authentication authentication) {
        return this.cartService.getCart(authentication);
    }

    @PostMapping(value = "/item", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addItem(Authentication authentication, @RequestBody CartItemDTO requestDTO) {
        this.cartService.addItem(authentication, requestDTO);
    }

    @PutMapping(value = "/item", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateItem(Authentication authentication, @RequestBody CartDBO cartDBO) throws CartItemDoesNotExistException {
        this.cartService.updateItem(authentication, cartDBO);
    }

    @DeleteMapping(value = "/item", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteItem(Authentication authentication, @RequestBody CartDBO cartDBO) throws CartItemDoesNotExistException{
        this.cartService.deleteItem(authentication, cartDBO);
    }

    @GetMapping(value = "/clear")
    public void clearCart(Authentication authentication) throws CartIsEmptyForBuyerException {
        this.cartService.clearCart(authentication);
    }

    @GetMapping(value = "/place")
    public RequestTokenDTO placeOrder(Authentication authentication) throws AccessDeniedException {
        return this.cartService.placeOrder(authentication);
    }
}
