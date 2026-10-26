package com.factory.enquiry.dto;

import com.factory.enquiry.entity.EnquiryStatus;
import java.time.Instant;

public class EnquiryResponseDto {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String companyName;
    private String state;
    private String message;
    private String interestedProducts;
    private String source;
    private EnquiryStatus status;
    private String internalNotes;
    private String convertedClientId;
    private Instant createdAt;
    private Instant updatedAt;

    public EnquiryResponseDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getInterestedProducts() {
        return interestedProducts;
    }

    public void setInterestedProducts(String interestedProducts) {
        this.interestedProducts = interestedProducts;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public EnquiryStatus getStatus() {
        return status;
    }

    public void setStatus(EnquiryStatus status) {
        this.status = status;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public String getConvertedClientId() {
        return convertedClientId;
    }

    public void setConvertedClientId(String convertedClientId) {
        this.convertedClientId = convertedClientId;
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
}
