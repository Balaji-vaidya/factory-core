package com.factory.po.dto;

import com.factory.po.entity.POPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class POCreateDto {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    private String deliveryAddressId;

    @NotBlank(message = "Office ID is required")
    private String officeId;

    private POPriority priority = POPriority.NORMAL;

    private LocalDate poDate;

    @NotNull(message = "Expected delivery date is required")
    private LocalDate expectedDeliveryDate;

    private String notes;

    @NotEmpty(message = "Purchase order must contain at least one line item")
    @Valid
    private List<POItemCreateDto> items;

    private boolean submitForApproval = false;

    public POCreateDto() {}

    // Getters and Setters

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDeliveryAddressId() {
        return deliveryAddressId;
    }

    public void setDeliveryAddressId(String deliveryAddressId) {
        this.deliveryAddressId = deliveryAddressId;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public POPriority getPriority() {
        return priority;
    }

    public void setPriority(POPriority priority) {
        this.priority = priority;
    }

    public LocalDate getPoDate() {
        return poDate;
    }

    public void setPoDate(LocalDate poDate) {
        this.poDate = poDate;
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

    public List<POItemCreateDto> getItems() {
        return items;
    }

    public void setItems(List<POItemCreateDto> items) {
        this.items = items;
    }

    public boolean isSubmitForApproval() {
        return submitForApproval;
    }

    public void setSubmitForApproval(boolean submitForApproval) {
        this.submitForApproval = submitForApproval;
    }
}
