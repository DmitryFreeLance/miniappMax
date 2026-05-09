package com.maxminiapp.service;

import com.maxminiapp.enums.OrderStatus;

public record PaymentInitResult(
        String paymentId,
        String paymentUrl,
        OrderStatus status,
        String message
) {
}
