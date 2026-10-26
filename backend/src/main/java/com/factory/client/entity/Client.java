package com.factory.client.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "clients", indexes = {
        @Index(name = "idx_clients_company_name", columnList = "company_name"),
        @Index(name = "idx_clients_state", columnList = "state"),
        @Index(name = "idx_clients_status", columnList = "status")
})
public class Client {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false, length = 30)
    private ClientType clientType = ClientType.SEED_PRODUCER;

    @Column(unique = true, length = 15)
    private String gstin;

    @Column(length = 10)
    private String pan;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(name = "primary_contact_name", nullable = false, length = 100)
    private String primaryContactName;

    @Column(name = "primary_contact_designation", length = 100)
    private String primaryContactDesignation = "Procurement Officer";

    @Column(name = "primary_contact_phone", nullable = false, length = 20)
    private String primaryContactPhone;

    @Column(name = "primary_contact_email", nullable = false, length = 100)
    private String primaryContactEmail;

    @Column(name = "billing_contact_name", length = 100)
    private String billingContactName;

    @Column(name = "billing_contact_designation", length = 100)
    private String billingContactDesignation;

    @Column(name = "billing_contact_phone", length = 20)
    private String billingContactPhone;

    @Column(name = "billing_contact_email", length = 100)
    private String billingContactEmail;

    @Column(name = "credit_days", nullable = false)
    private int creditDays = 30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClientStatus status = ClientStatus.ACTIVE;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClientAddress> addresses = new ArrayList<>();

    public Client() {
        this.id = UUID.randomUUID().toString();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addAddress(ClientAddress address) {
        addresses.add(address);
        address.setClient(this);
    }

    public void removeAddress(ClientAddress address) {
        addresses.remove(address);
        address.setClient(null);
    }

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
        this.gstin = gstin != null ? gstin.trim().toUpperCase() : null;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan != null ? pan.trim().toUpperCase() : null;
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

    public List<ClientAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<ClientAddress> addresses) {
        this.addresses = addresses;
    }
}
