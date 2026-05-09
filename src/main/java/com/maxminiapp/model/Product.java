package com.maxminiapp.model;

import com.maxminiapp.enums.UnitMode;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 4000)
    private String description;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "price_pcs", precision = 12, scale = 2)
    private BigDecimal pricePcs;

    @Column(name = "price_cubic_meters", precision = 12, scale = 2)
    private BigDecimal priceCubicMeters;

    @Column(name = "old_price", precision = 12, scale = 2)
    private BigDecimal oldPrice;

    @Column(name = "stock_pcs", precision = 14, scale = 3)
    private BigDecimal stockPcs;

    @Column(name = "stock_cubic_meters", precision = 14, scale = 3)
    private BigDecimal stockCubicMeters;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_mode", nullable = false)
    private UnitMode unitMode = UnitMode.BOTH;

    @Column(name = "is_fix_price")
    private Boolean fixPrice = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(BigDecimal oldPrice) {
        this.oldPrice = oldPrice;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
