package com.factory.product.dto;

import com.factory.product.entity.ProductCategory;
import com.factory.product.entity.ProductInternalStatus;
import com.factory.product.entity.ProductUnit;
import com.factory.product.entity.WebsiteVisibility;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductResponseDto {

    private String id;
    private String sku;
    private String name;
    private ProductCategory category;
    private ProductUnit unit;
    private String description;
    private BigDecimal basePriceINR;
    private int leadTimeWeeks;
    private ProductInternalStatus internalStatus;
    private WebsiteVisibility websiteVisibility;
    private String brochureUrl;
    private boolean isLockedForSkuChange;
    private ProductSpecDto specs;
    private Instant createdAt;
    private Instant updatedAt;

    public ProductResponseDto() {}

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
        this.sku = sku;
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

    public ProductSpecDto getSpecs() {
        return specs;
    }

    public void setSpecs(ProductSpecDto specs) {
        this.specs = specs;
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
