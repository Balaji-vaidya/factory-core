package com.factory.product.dto;

import com.factory.product.entity.ProductCategory;
import com.factory.product.entity.ProductUnit;

import java.io.Serializable;
import java.math.BigDecimal;

public class ProductSnapshotDto implements Serializable {

    private String productId;
    private String sku;
    private String name;
    private ProductCategory category;
    private ProductUnit unit;
    private BigDecimal basePriceINR;
    private String capacity;
    private String motorPower;

    public ProductSnapshotDto() {}

    public ProductSnapshotDto(String productId, String sku, String name, ProductCategory category,
                              ProductUnit unit, BigDecimal basePriceINR, String capacity, String motorPower) {
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.basePriceINR = basePriceINR;
        this.capacity = capacity;
        this.motorPower = motorPower;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public BigDecimal getBasePriceINR() {
        return basePriceINR;
    }

    public void setBasePriceINR(BigDecimal basePriceINR) {
        this.basePriceINR = basePriceINR;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getMotorPower() {
        return motorPower;
    }

    public void setMotorPower(String motorPower) {
        this.motorPower = motorPower;
    }
}
