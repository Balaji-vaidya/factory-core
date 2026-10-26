package com.factory.auth.dto;

import java.util.List;

public class UserProfileResponse {

    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private String officeId;
    private String officeName;
    private String state;
    private List<String> allowedOffices;
    private String status;

    public UserProfileResponse() {}

    public UserProfileResponse(String id, String email, String fullName, String phone, String role,
                                String officeId, String officeName, String state, List<String> allowedOffices, String status) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.officeId = officeId;
        this.officeName = officeName;
        this.state = state;
        this.allowedOffices = allowedOffices;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<String> getAllowedOffices() {
        return allowedOffices;
    }

    public void setAllowedOffices(List<String> allowedOffices) {
        this.allowedOffices = allowedOffices;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
