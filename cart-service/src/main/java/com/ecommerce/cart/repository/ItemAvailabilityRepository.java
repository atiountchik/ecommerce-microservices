package com.ecommerce.cart.repository;

import com.ecommerce.cart.repository.dbo.ItemAvailabilityRequestDBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemAvailabilityRepository extends JpaRepository<ItemAvailabilityRequestDBO, UUID> {
}
