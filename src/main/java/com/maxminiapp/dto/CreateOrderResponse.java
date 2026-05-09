package com.maxminiapp.dto;

import com.maxminiapp.enums.OrderStatus;

import java.math.BigDecimal;

public record CreateOrderResponse(
        Long orderId,
        BigDecimal totalPrice,
        OrderStatus status,
        String paymentId,
        String paymentUrl,
        String message
) {
}
