package com.factory.po.dto;

import com.factory.po.entity.POPriority;
import com.factory.po.entity.POStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class POResponseDto {

    private String id;
    private String poNumber;
    private String clientId;
    private String clientCompanyName;
    private String clientState;
    private String clientSnapshot;
    private String officeId;
    private String officeName;
    private String officeState;
    private POPriority priority;
    private POStatus status;
    private BigDecimal totalAmountINR;
    private LocalDate poDate;
    private LocalDate expectedDeliveryDate;
    private String notes;
    private String rejectionReason;
    private String cancellationReason;
    private int currentVersion;
    private String createdBy;
    private String approvedBy;
    private Instant approvedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<POItemResponseDto> items = new ArrayList<>();
    private List<POVersionResponseDto> versions = new ArrayList<>();

    public POResponseDto() {}

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientCompanyName() {
        return clientCompanyName;
    }

    public void setClientCompanyName(String clientCompanyName) {
        this.clientCompanyName = clientCompanyName;
    }

    public String getClientState() {
        return clientState;
    }

    public void setClientState(String clientState) {
        this.clientState = clientState;
    }

    public String getClientSnapshot() {
        return clientSnapshot;
    }

    public void setClientSnapshot(String clientSnapshot) {
        this.clientSnapshot = clientSnapshot;
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

    public String getOfficeState() {
        return officeState;
    }

    public void setOfficeState(String officeState) {
        this.officeState = officeState;
    }

    public POPriority getPriority() {
        return priority;
    }

    public void setPriority(POPriority priority) {
        this.priority = priority;
    }

    public POStatus getStatus() {
        return status;
    }

    public void setStatus(POStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmountINR() {
        return totalAmountINR;
    }

    public void setTotalAmountINR(BigDecimal totalAmountINR) {
        this.totalAmountINR = totalAmountINR;
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

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(int currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
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

    public List<POItemResponseDto> getItems() {
        return items;
    }

    public void setItems(List<POItemResponseDto> items) {
        this.items = items;
    }

    public List<POVersionResponseDto> getVersions() {
        return versions;
    }

    public void setVersions(List<POVersionResponseDto> versions) {
        this.versions = versions;
    }
}
