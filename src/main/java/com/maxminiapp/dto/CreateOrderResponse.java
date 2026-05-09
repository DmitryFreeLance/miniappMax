package com.maxminiapp.dto;

import com.maxminiapp.enums.OrderStatus;
import com.maxminiapp.enums.PaymentMethod;

import java.math.BigDecimal;

public record CreateOrderResponse(
        Long orderId,
        BigDecimal itemsTotal,
        BigDecimal deliveryFee,
        BigDecimal totalPrice,
        OrderStatus status,
        PaymentMethod paymentMethod,
        String message
) {
}
