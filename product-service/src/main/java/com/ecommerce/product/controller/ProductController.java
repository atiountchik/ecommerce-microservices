package com.ecommerce.product.controller;

import com.ecommerce.product.repository.dbo.ProductDBO;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.sdk.exceptions.ProductAlreadyRegisteredException;
import com.ecommerce.sdk.exceptions.ProductDoesNotExistException;
import com.ecommerce.sdk.request.RegisterNewProductRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/v1")
public class ProductController {

    private ProductService sellerService;

    @Autowired
    public ProductController(ProductService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductDBO> getAllProducts() {
        return this.sellerService.getAllProducts();
    }

    @PostMapping(value = "/seller/products", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductDBO registerProduct(Authentication authentication, @Valid @RequestBody RegisterNewProductRequestDTO newProductRequestDTO) throws AccessDeniedException, ProductAlreadyRegisteredException {
        return this.sellerService.registerProduct(authentication, newProductRequestDTO);
    }

    @GetMapping(value = "/seller/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductDBO> getSellerProducts(Authentication authentication) {
        return this.sellerService.getSellerProducts(authentication);
    }

    @DeleteMapping(value = "/seller/products", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteProduct(Authentication authentication, @Valid @RequestBody ProductDBO productDBO) throws ProductDoesNotExistException, AccessDeniedException  {
        this.sellerService.deleteProduct(authentication, productDBO);
    }

    @PutMapping(value = "/seller/products", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateProduct(Authentication authentication, @Valid @RequestBody ProductDBO productDBO) throws ProductDoesNotExistException, AccessDeniedException  {
        this.sellerService.updateProduct(authentication, productDBO);
    }
}
