package com.factory.client.dto;

import com.factory.client.entity.ClientType;
import jakarta.validation.constraints.*;

public class ClientUpdateDto {

    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name must not exceed 150 characters")
    private String companyName;

    private ClientType clientType;

    @Pattern(regexp = "^$|^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", 
             message = "Invalid 15-character Indian GSTIN format (e.g. 27AAPCA1234F1Z5)")
    private String gstin;

    @Pattern(regexp = "^$|^[A-Z]{5}[0-9]{4}[A-Z]{1}$", 
             message = "Invalid 10-character Indian PAN format (e.g. AAPCA1234F)")
    private String pan;

    @NotBlank(message = "Registered state is required")
    private String state;

    @NotBlank(message = "Primary contact name is required")
    private String primaryContactName;

    private String primaryContactDesignation;

    @NotBlank(message = "Primary contact phone is required")
    private String primaryContactPhone;

    @NotBlank(message = "Primary contact email is required")
    @Email(message = "Invalid email address format")
    private String primaryContactEmail;

    private String billingContactName;
    private String billingContactDesignation;
    private String billingContactPhone;
    private String billingContactEmail;

    @Min(value = 0, message = "Credit days cannot be negative")
    @Max(value = 365, message = "Credit days cannot exceed 365 days")
    private int creditDays = 30;

    public ClientUpdateDto() {}

    // Getters and Setters

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPrimaryContactName() {
        return primaryContactName;
    }

    public void setPrimaryContactName(String primaryContactName) {
        this.primaryContactName = primaryContactName;
    }

    public String getPrimaryContactDesignation() {
        return primaryContactDesignation;
    }

    public void setPrimaryContactDesignation(String primaryContactDesignation) {
        this.primaryContactDesignation = primaryContactDesignation;
    }

    public String getPrimaryContactPhone() {
        return primaryContactPhone;
    }

    public void setPrimaryContactPhone(String primaryContactPhone) {
        this.primaryContactPhone = primaryContactPhone;
    }

    public String getPrimaryContactEmail() {
        return primaryContactEmail;
    }

    public void setPrimaryContactEmail(String primaryContactEmail) {
        this.primaryContactEmail = primaryContactEmail;
    }

    public String getBillingContactName() {
        return billingContactName;
    }

    public void setBillingContactName(String billingContactName) {
        this.billingContactName = billingContactName;
    }

    public String getBillingContactDesignation() {
        return billingContactDesignation;
    }

    public void setBillingContactDesignation(String billingContactDesignation) {
        this.billingContactDesignation = billingContactDesignation;
    }

    public String getBillingContactPhone() {
        return billingContactPhone;
    }

    public void setBillingContactPhone(String billingContactPhone) {
        this.billingContactPhone = billingContactPhone;
    }

    public String getBillingContactEmail() {
        return billingContactEmail;
    }

    public void setBillingContactEmail(String billingContactEmail) {
        this.billingContactEmail = billingContactEmail;
    }

    public int getCreditDays() {
        return creditDays;
    }

    public void setCreditDays(int creditDays) {
        this.creditDays = creditDays;
    }
}
