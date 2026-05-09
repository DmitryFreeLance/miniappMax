package com.maxminiapp.repository;

import com.maxminiapp.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByCreatedAtDesc();

    boolean existsByProductId(Long productId);
}
