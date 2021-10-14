package com.ecommerce.order.repository;

import com.ecommerce.order.repository.dbo.OrderDBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderDBO, Long> {
    OrderDBO findByStatusUuid(UUID statusUuid);
    List<OrderDBO> findByBuyerIdAndStatus(UUID buyerId, String status);
}
