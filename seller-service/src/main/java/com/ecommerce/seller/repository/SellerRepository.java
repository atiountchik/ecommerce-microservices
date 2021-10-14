package com.ecommerce.seller.repository;

import com.ecommerce.seller.repository.dbo.SellerDBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SellerRepository extends JpaRepository<SellerDBO, Long> {
    SellerDBO findByAuthId(UUID authId);
}
