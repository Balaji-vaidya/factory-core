package com.factory.client.dto;

import java.io.Serializable;

public class ClientSnapshotDto implements Serializable {

    private String clientId;
    private String companyName;
    private String gstin;
    private String pan;
    private String state;
    private String primaryContactName;
    private String primaryContactPhone;
    private String primaryContactEmail;
    private String billingAddressLine;
    private String deliveryAddressLine;
    private int creditDays;

    public ClientSnapshotDto() {}

    public ClientSnapshotDto(String clientId, String companyName, String gstin, String pan, String state,
                             String primaryContactName, String primaryContactPhone, String primaryContactEmail,
                             String billingAddressLine, String deliveryAddressLine, int creditDays) {
        this.clientId = clientId;
        this.companyName = companyName;
        this.gstin = gstin;
        this.pan = pan;
        this.state = state;
        this.primaryContactName = primaryContactName;
        this.primaryContactPhone = primaryContactPhone;
        this.primaryContactEmail = primaryContactEmail;
        this.billingAddressLine = billingAddressLine;
        this.deliveryAddressLine = deliveryAddressLine;
        this.creditDays = creditDays;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
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

    public String getBillingAddressLine() {
        return billingAddressLine;
    }

    public void setBillingAddressLine(String billingAddressLine) {
        this.billingAddressLine = billingAddressLine;
    }

    public String getDeliveryAddressLine() {
        return deliveryAddressLine;
    }

    public void setDeliveryAddressLine(String deliveryAddressLine) {
        this.deliveryAddressLine = deliveryAddressLine;
    }

    public int getCreditDays() {
        return creditDays;
    }

    public void setCreditDays(int creditDays) {
        this.creditDays = creditDays;
    }
}
