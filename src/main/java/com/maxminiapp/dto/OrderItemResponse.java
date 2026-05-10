package com.maxminiapp.dto;

import com.maxminiapp.enums.QuantityUnit;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal quantity,
        QuantityUnit quantityUnit,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
