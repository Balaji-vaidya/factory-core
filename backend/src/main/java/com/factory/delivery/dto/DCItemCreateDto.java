package com.factory.delivery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DCItemCreateDto {

    @NotBlank(message = "PO Item ID is required")
    private String poItemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Dispatch quantity must be at least 1")
    private Integer quantity;

    private String notes;

    public DCItemCreateDto() {
    }

    public DCItemCreateDto(String poItemId, Integer quantity, String notes) {
        this.poItemId = poItemId;
        this.quantity = quantity;
        this.notes = notes;
    }

    public String getPoItemId() {
        return poItemId;
    }

    public void setPoItemId(String poItemId) {
        this.poItemId = poItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
