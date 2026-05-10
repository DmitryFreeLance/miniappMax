package com.maxminiapp.dto;

import com.maxminiapp.enums.QuantityUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateOrderItemRequest {

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin("0.001")
    private BigDecimal quantity;

    @NotNull
    private QuantityUnit quantityUnit;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public QuantityUnit getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(QuantityUnit quantityUnit) {
        this.quantityUnit = quantityUnit;
    }
}
