package com.valhora.backend.orders;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
