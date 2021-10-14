package com.ecommerce.cart.repository;

import com.ecommerce.cart.repository.dbo.CartDBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<CartDBO, Long> {
    List<CartDBO> findByBuyerId(UUID buyerId);

    CartDBO findByBuyerIdAndSku(UUID buyerId, UUID sku);

    void deleteByBuyerId(UUID buyerId);
}
