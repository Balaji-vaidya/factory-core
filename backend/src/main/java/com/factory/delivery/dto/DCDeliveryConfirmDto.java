package com.factory.delivery.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class DCDeliveryConfirmDto {

    @NotBlank(message = "Recipient contact name is required")
    private String recipientName;

    @NotBlank(message = "Recipient contact phone is required")
    private String recipientPhone;

    private LocalDate deliveryDate;
    private String proofOfDeliveryUrl;
    private String notes;

    public DCDeliveryConfirmDto() {
    }

    public DCDeliveryConfirmDto(String recipientName, String recipientPhone, LocalDate deliveryDate, String proofOfDeliveryUrl, String notes) {
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.deliveryDate = deliveryDate;
        this.proofOfDeliveryUrl = proofOfDeliveryUrl;
        this.notes = notes;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getProofOfDeliveryUrl() {
        return proofOfDeliveryUrl;
    }

    public void setProofOfDeliveryUrl(String proofOfDeliveryUrl) {
        this.proofOfDeliveryUrl = proofOfDeliveryUrl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
