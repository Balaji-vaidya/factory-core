package com.factory.po.dto;

import com.factory.po.entity.POPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class POUpdateDto {

    private String clientId;
    private String deliveryAddressId;
    private POPriority priority;

    @NotNull(message = "Expected delivery date is required")
    private LocalDate expectedDeliveryDate;

    private String notes;

    @NotEmpty(message = "Purchase order must contain at least one line item")
    @Valid
    private List<POItemCreateDto> items;

    private String changeReason;
    private boolean submitForApproval = false;

    public POUpdateDto() {}

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

    public POPriority getPriority() {
        return priority;
    }

    public void setPriority(POPriority priority) {
        this.priority = priority;
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

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public boolean isSubmitForApproval() {
        return submitForApproval;
    }

    public void setSubmitForApproval(boolean submitForApproval) {
        this.submitForApproval = submitForApproval;
    }
}
