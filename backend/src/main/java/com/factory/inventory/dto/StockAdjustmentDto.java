package com.factory.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StockAdjustmentDto {

    @NotBlank(message = "Office ID is required")
    private String officeId;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull(message = "New on-hand quantity is required")
    @Min(value = 0, message = "On-hand quantity cannot be negative")
    private Integer newOnHandQuantity;

    @NotBlank(message = "Reason for manual adjustment is mandatory")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String reason;

    public StockAdjustmentDto() {
    }

    public StockAdjustmentDto(String officeId, String productId, Integer newOnHandQuantity, String reason) {
        this.officeId = officeId;
        this.productId = productId;
        this.newOnHandQuantity = newOnHandQuantity;
        this.reason = reason;
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

    public Integer getNewOnHandQuantity() {
        return newOnHandQuantity;
    }

    public void setNewOnHandQuantity(Integer newOnHandQuantity) {
        this.newOnHandQuantity = newOnHandQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
