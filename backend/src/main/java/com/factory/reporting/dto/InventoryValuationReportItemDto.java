package com.factory.reporting.dto;

import java.math.BigDecimal;

public class InventoryValuationReportItemDto {
    private String officeId;
    private String officeName;
    private String productId;
    private String sku;
    private String productName;
    private String category;
    private int onHandQuantity;
    private int reservedQuantity;
    private int availableQuantity;
    private BigDecimal basePriceINR;
    private BigDecimal totalValuationINR;

    public InventoryValuationReportItemDto() {
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getOnHandQuantity() {
        return onHandQuantity;
    }

    public void setOnHandQuantity(int onHandQuantity) {
        this.onHandQuantity = onHandQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public BigDecimal getBasePriceINR() {
        return basePriceINR;
    }

    public void setBasePriceINR(BigDecimal basePriceINR) {
        this.basePriceINR = basePriceINR;
    }

    public BigDecimal getTotalValuationINR() {
        return totalValuationINR;
    }

    public void setTotalValuationINR(BigDecimal totalValuationINR) {
        this.totalValuationINR = totalValuationINR;
    }
}
