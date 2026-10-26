package com.factory.product.dto;

import com.factory.product.entity.ProductCategory;
import com.factory.product.entity.ProductUnit;
import com.factory.product.entity.WebsiteVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductUpdateDto {

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9-_]+$", message = "SKU must contain only alphanumeric characters, dashes, and underscores")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must not exceed 150 characters")
    private String name;

    @NotNull(message = "Category is required")
    private ProductCategory category;

    private ProductUnit unit = ProductUnit.UNIT;

    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be positive or zero")
    private BigDecimal basePriceINR = BigDecimal.ZERO;

    @Min(value = 1, message = "Lead time must be at least 1 week")
    @Max(value = 52, message = "Lead time must not exceed 52 weeks")
    private int leadTimeWeeks = 4;

    private WebsiteVisibility websiteVisibility = WebsiteVisibility.PUBLIC;

    private String brochureUrl;

    @Valid
    private ProductSpecDto specs;

    public ProductUpdateDto() {}

    // Getters and Setters

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

    public ProductSpecDto getSpecs() {
        return specs;
    }

    public void setSpecs(ProductSpecDto specs) {
        this.specs = specs;
    }
}
