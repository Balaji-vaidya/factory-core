package com.factory.product.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_category", columnList = "category"),
        @Index(name = "idx_products_internal_status", columnList = "internal_status"),
        @Index(name = "idx_products_website_visibility", columnList = "website_visibility")
})
public class Product {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductUnit unit = ProductUnit.UNIT;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price_inr", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePriceINR = BigDecimal.ZERO;

    @Column(name = "lead_time_weeks", nullable = false)
    private int leadTimeWeeks = 4;

    @Enumerated(EnumType.STRING)
    @Column(name = "internal_status", nullable = false, length = 20)
    private ProductInternalStatus internalStatus = ProductInternalStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "website_visibility", nullable = false, length = 20)
    private WebsiteVisibility websiteVisibility = WebsiteVisibility.PUBLIC;

    @Column(name = "brochure_url", length = 255)
    private String brochureUrl;

    @Column(name = "is_locked_for_sku_change", nullable = false)
    private boolean isLockedForSkuChange = false;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private ProductSpec specs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Product() {
        this.id = UUID.randomUUID().toString();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void setSpecs(ProductSpec specs) {
        this.specs = specs;
        if (specs != null) {
            specs.setProduct(this);
        }
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku != null ? sku.trim().toUpperCase() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public ProductUnit getUnit() {
        return unit;
    }

    public void setUnit(ProductUnit unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePriceINR() {
        return basePriceINR;
    }

    public void setBasePriceINR(BigDecimal basePriceINR) {
        this.basePriceINR = basePriceINR;
    }

    public int getLeadTimeWeeks() {
        return leadTimeWeeks;
    }

    public void setLeadTimeWeeks(int leadTimeWeeks) {
        this.leadTimeWeeks = leadTimeWeeks;
    }

    public ProductInternalStatus getInternalStatus() {
        return internalStatus;
    }

    public void setInternalStatus(ProductInternalStatus internalStatus) {
        this.internalStatus = internalStatus;
    }

    public WebsiteVisibility getWebsiteVisibility() {
        return websiteVisibility;
    }

    public void setWebsiteVisibility(WebsiteVisibility websiteVisibility) {
        this.websiteVisibility = websiteVisibility;
    }

    public String getBrochureUrl() {
        return brochureUrl;
    }

    public void setBrochureUrl(String brochureUrl) {
        this.brochureUrl = brochureUrl;
    }

    public boolean isLockedForSkuChange() {
        return isLockedForSkuChange;
    }

    public void setLockedForSkuChange(boolean lockedForSkuChange) {
        isLockedForSkuChange = lockedForSkuChange;
    }

    public ProductSpec getSpecs() {
        return specs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
