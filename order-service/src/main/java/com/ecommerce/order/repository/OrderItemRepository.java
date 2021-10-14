package com.ecommerce.order.repository;

import com.ecommerce.order.repository.dbo.OrderDBO;
import com.ecommerce.order.repository.dbo.OrderItemDBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemDBO, Long> {
}
