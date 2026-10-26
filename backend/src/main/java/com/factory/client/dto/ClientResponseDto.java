package com.factory.client.dto;

import com.factory.client.entity.ClientStatus;
import com.factory.client.entity.ClientType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ClientResponseDto {

    private String id;
    private String companyName;
    private ClientType clientType;
    private String gstin;
    private String pan;
    private String state;

    private ContactDto primaryContact;
    private ContactDto billingContact;

    private int creditDays;
    private ClientStatus status;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ClientAddressDto> deliveryAddresses = new ArrayList<>();

    public static class ContactDto {
        private String name;
        private String designation;
        private String phone;
        private String email;

        public ContactDto() {}

        public ContactDto(String name, String designation, String phone, String email) {
            this.name = name;
            this.designation = designation;
            this.phone = phone;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesignation() {
            return designation;
        }

        public void setDesignation(String designation) {
            this.designation = designation;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public ClientResponseDto() {}

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public ContactDto getPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(ContactDto primaryContact) {
        this.primaryContact = primaryContact;
    }

    public ContactDto getBillingContact() {
        return billingContact;
    }

    public void setBillingContact(ContactDto billingContact) {
        this.billingContact = billingContact;
    }

    public int getCreditDays() {
        return creditDays;
    }

    public void setCreditDays(int creditDays) {
        this.creditDays = creditDays;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ClientAddressDto> getDeliveryAddresses() {
        return deliveryAddresses;
    }

    public void setDeliveryAddresses(List<ClientAddressDto> deliveryAddresses) {
        this.deliveryAddresses = deliveryAddresses;
    }
}
