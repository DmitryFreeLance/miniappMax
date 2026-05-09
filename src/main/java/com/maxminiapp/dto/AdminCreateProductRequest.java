package com.maxminiapp.dto;

import com.maxminiapp.enums.UnitMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class AdminCreateProductRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String imageUrl;

    @DecimalMin("0.01")
    private BigDecimal pricePcs;

    @DecimalMin("0.01")
    private BigDecimal priceCubicMeters;

    @DecimalMin("0.01")
    private BigDecimal oldPrice;

    @DecimalMin("0.0")
    private BigDecimal stockPcs;

    @DecimalMin("0.0")
    private BigDecimal stockCubicMeters;

    private UnitMode unitMode;

    private Boolean fixPrice;

    private Boolean active;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getPricePcs() {
        return pricePcs;
    }

    public void setPricePcs(BigDecimal pricePcs) {
        this.pricePcs = pricePcs;
    }

    public BigDecimal getPriceCubicMeters() {
        return priceCubicMeters;
    }

    public void setPriceCubicMeters(BigDecimal priceCubicMeters) {
        this.priceCubicMeters = priceCubicMeters;
    }

    public BigDecimal getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(BigDecimal oldPrice) {
        this.oldPrice = oldPrice;
    }

    public BigDecimal getStockPcs() {
        return stockPcs;
    }

    public void setStockPcs(BigDecimal stockPcs) {
        this.stockPcs = stockPcs;
    }

    public BigDecimal getStockCubicMeters() {
        return stockCubicMeters;
    }

    public void setStockCubicMeters(BigDecimal stockCubicMeters) {
        this.stockCubicMeters = stockCubicMeters;
    }

    public UnitMode getUnitMode() {
        return unitMode;
    }

    public void setUnitMode(UnitMode unitMode) {
        this.unitMode = unitMode;
    }

    public Boolean getFixPrice() {
        return fixPrice;
    }

    public void setFixPrice(Boolean fixPrice) {
        this.fixPrice = fixPrice;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
