package com.ecommerce.buyer.repository;

import com.ecommerce.buyer.repository.dbo.BuyerDBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BuyerRepository extends JpaRepository<BuyerDBO, Long> {
    BuyerDBO findByAuthId(UUID authId);
}
