package com.maxminiapp.repository;

import com.maxminiapp.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByCreatedAtDesc();

    Optional<Order> findByPaymentId(String paymentId);

    boolean existsByProductId(Long productId);
}
