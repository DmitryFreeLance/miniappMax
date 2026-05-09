package com.maxminiapp.dto;

import com.maxminiapp.enums.UnitMode;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        BigDecimal price,
        BigDecimal pricePcs,
        BigDecimal priceCubicMeters,
        BigDecimal oldPrice,
        BigDecimal stockPcs,
        BigDecimal stockCubicMeters,
        UnitMode unitMode,
        boolean fixPrice,
        boolean active
) {
}
