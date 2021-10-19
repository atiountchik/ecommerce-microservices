package com.ecommerce.cart.repository;

import com.ecommerce.cart.repository.dbo.CartItemDBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItemDBO, Long> {
    List<CartItemDBO> findByBuyerId(UUID buyerId);

    CartItemDBO findByBuyerIdAndSku(UUID buyerId, UUID sku);

    void deleteByBuyerId(UUID buyerId);
}
