package com.ecommerce.product.repository;

import com.ecommerce.product.repository.dbo.ProductDBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductDBO, Long> {
    List<ProductDBO> findBySellerId(UUID sellerId);

    ProductDBO findBySellerIdAndNameIgnoreCase(UUID sellerId, String name);

    ProductDBO findBySku(UUID sku);
}
