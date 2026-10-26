package com.factory.po.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class POItemResponseDto {

    private String id;
    private String productId;
    private String sku;
    private String productName;
    private String unit;
    private int quantity;
    private BigDecimal unitPriceINR;
    private BigDecimal lineTotalINR;
    private int reservedQuantity;
    private int dispatchedQuantity;
    private int deliveredQuantity;
    private LocalDate expectedDeliveryDate;
    private String notes;

    public POItemResponseDto() {}

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPriceINR() {
        return unitPriceINR;
    }

    public void setUnitPriceINR(BigDecimal unitPriceINR) {
        this.unitPriceINR = unitPriceINR;
    }

    public BigDecimal getLineTotalINR() {
        return lineTotalINR;
    }

    public void setLineTotalINR(BigDecimal lineTotalINR) {
        this.lineTotalINR = lineTotalINR;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public int getDispatchedQuantity() {
        return dispatchedQuantity;
    }

    public void setDispatchedQuantity(int dispatchedQuantity) {
        this.dispatchedQuantity = dispatchedQuantity;
    }

    public int getDeliveredQuantity() {
        return deliveredQuantity;
    }

    public void setDeliveredQuantity(int deliveredQuantity) {
        this.deliveredQuantity = deliveredQuantity;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
