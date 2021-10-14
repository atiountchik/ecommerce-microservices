package com.ecommerce.payment.repository;

import com.ecommerce.payment.repository.dbo.PaymentDBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentDBO, Long> {

    PaymentDBO findByStatusUuid(UUID statusUuid);
}
