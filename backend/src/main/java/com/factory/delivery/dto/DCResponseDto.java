package com.factory.delivery.dto;

import com.factory.delivery.entity.DCStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DCResponseDto {

    private String id;
    private String dcNumber;
    private String purchaseOrderId;
    private String poNumber;
    private String clientId;
    private String clientCompanyName;
    private String officeId;
    private String officeName;
    private String deliveryAddressId;
    private String deliveryAddressSnapshot;
    private DCStatus status;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;
    private String transporterName;
    private String lrNumber;
    private String ewayBillNumber;
    private LocalDate ewayBillDate;
    private LocalDate challanDate;
    private Instant dispatchedAt;
    private String dispatchedBy;
    private Instant deliveredAt;
    private String deliveryConfirmedBy;
    private String recipientName;
    private String recipientPhone;
    private String proofOfDeliveryUrl;
    private String notes;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private List<DCItemResponseDto> items = new ArrayList<>();

    public DCResponseDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDcNumber() {
        return dcNumber;
    }

    public void setDcNumber(String dcNumber) {
        this.dcNumber = dcNumber;
    }

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
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

    public String getDeliveryAddressId() {
        return deliveryAddressId;
    }

    public void setDeliveryAddressId(String deliveryAddressId) {
        this.deliveryAddressId = deliveryAddressId;
    }

    public String getDeliveryAddressSnapshot() {
        return deliveryAddressSnapshot;
    }

    public void setDeliveryAddressSnapshot(String deliveryAddressSnapshot) {
        this.deliveryAddressSnapshot = deliveryAddressSnapshot;
    }

    public DCStatus getStatus() {
        return status;
    }

    public void setStatus(DCStatus status) {
        this.status = status;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getTransporterName() {
        return transporterName;
    }

    public void setTransporterName(String transporterName) {
        this.transporterName = transporterName;
    }

    public String getLrNumber() {
        return lrNumber;
    }

    public void setLrNumber(String lrNumber) {
        this.lrNumber = lrNumber;
    }

    public String getEwayBillNumber() {
        return ewayBillNumber;
    }

    public void setEwayBillNumber(String ewayBillNumber) {
        this.ewayBillNumber = ewayBillNumber;
    }

    public LocalDate getEwayBillDate() {
        return ewayBillDate;
    }

    public void setEwayBillDate(LocalDate ewayBillDate) {
        this.ewayBillDate = ewayBillDate;
    }

    public LocalDate getChallanDate() {
        return challanDate;
    }

    public void setChallanDate(LocalDate challanDate) {
        this.challanDate = challanDate;
    }

    public Instant getDispatchedAt() {
        return dispatchedAt;
    }

    public void setDispatchedAt(Instant dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    public String getDispatchedBy() {
        return dispatchedBy;
    }

    public void setDispatchedBy(String dispatchedBy) {
        this.dispatchedBy = dispatchedBy;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getDeliveryConfirmedBy() {
        return deliveryConfirmedBy;
    }

    public void setDeliveryConfirmedBy(String deliveryConfirmedBy) {
        this.deliveryConfirmedBy = deliveryConfirmedBy;
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

    public List<DCItemResponseDto> getItems() {
        return items;
    }

    public void setItems(List<DCItemResponseDto> items) {
        this.items = items;
    }
}
