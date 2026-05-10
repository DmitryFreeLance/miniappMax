package com.maxminiapp.dto;

import com.maxminiapp.enums.DeliveryMethod;
import com.maxminiapp.enums.OrderStatus;
import com.maxminiapp.enums.PaymentMethod;
import com.maxminiapp.enums.QuantityUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long maxUserId,
        Long productId,
        String productName,
        BigDecimal quantity,
        QuantityUnit quantityUnit,
        BigDecimal unitPrice,
        BigDecimal itemsTotal,
        BigDecimal deliveryFee,
        BigDecimal totalPrice,
        DeliveryMethod deliveryMethod,
        PaymentMethod paymentMethod,
        String fullName,
        String phone,
        String address,
        OrderStatus status,
        String paymentDetailsSnapshot,
        boolean accepted,
        String deliveryEta,
        LocalDateTime acceptedAt,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {
}
