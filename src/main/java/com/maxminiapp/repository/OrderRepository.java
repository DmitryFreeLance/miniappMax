package com.maxminiapp.repository;

import com.maxminiapp.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByUserMaxUserIdOrderByCreatedAtDesc(Long maxUserId);

    Optional<Order> findFirstByUserMaxUserIdAndClientRequestIdOrderByCreatedAtDesc(Long maxUserId, String clientRequestId);

    List<Order> findTop5ByUserMaxUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long maxUserId, LocalDateTime createdAfter);

    boolean existsByProductId(Long productId);
}
