package com.factory.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StockInwardDto {

    @NotBlank(message = "Office ID is required")
    private String officeId;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Inward quantity must be at least 1")
    private Integer quantity;

    @Min(value = 0, message = "Reorder threshold cannot be negative")
    private Integer reorderThreshold;

    private String notes;

    public StockInwardDto() {
    }

    public StockInwardDto(String officeId, String productId, Integer quantity, Integer reorderThreshold, String notes) {
        this.officeId = officeId;
        this.productId = productId;
        this.quantity = quantity;
        this.reorderThreshold = reorderThreshold;
        this.notes = notes;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
