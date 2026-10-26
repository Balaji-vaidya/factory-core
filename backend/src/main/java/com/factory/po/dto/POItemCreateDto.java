package com.factory.po.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class POItemCreateDto {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @Min(value = 1, message = "Item quantity must be at least 1")
    private int quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
    private BigDecimal unitPriceINR;

    private LocalDate expectedDeliveryDate;
    private String notes;

    public POItemCreateDto() {}

    public POItemCreateDto(String productId, int quantity, BigDecimal unitPriceINR, LocalDate expectedDeliveryDate, String notes) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPriceINR = unitPriceINR;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.notes = notes;
    }

    // Getters and Setters

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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
