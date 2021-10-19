package com.ecommerce.cart.repository;

import com.ecommerce.cart.repository.dbo.FailedItemAvailabilityRequestDBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FailedItemAvailabilityRepository extends JpaRepository<FailedItemAvailabilityRequestDBO, Long> {
    FailedItemAvailabilityRequestDBO findByItemAvailabilityRequestId(UUID itemAvailabilityRequestId);
}
