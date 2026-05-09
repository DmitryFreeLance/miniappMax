package com.maxminiapp.dto;

import com.maxminiapp.enums.OrderStatus;
import com.maxminiapp.enums.QuantityUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long maxUserId,
        Long productId,
        String productName,
        BigDecimal quantity,
        QuantityUnit quantityUnit,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        String fullName,
        String phone,
        String address,
        OrderStatus status,
        String paymentId,
        String paymentUrl,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {
}
