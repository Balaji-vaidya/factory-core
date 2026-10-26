package com.factory.delivery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public class DCCreateDto {

    @NotBlank(message = "Purchase Order ID is required")
    private String purchaseOrderId;

    private String deliveryAddressId;

    @NotBlank(message = "Vehicle number is required (e.g. MH-21-BH-4512)")
    private String vehicleNumber;

    @NotBlank(message = "Driver name is required")
    private String driverName;

    @NotBlank(message = "Driver phone is required")
    private String driverPhone;

    @NotBlank(message = "Transporter name is required (e.g. VRL Logistics Ltd)")
    private String transporterName;

    private String lrNumber;
    private String ewayBillNumber;
    private LocalDate ewayBillDate;
    private LocalDate challanDate;
    private String notes;

    @NotEmpty(message = "At least one item must be included in the delivery challan")
    @Valid
    private List<DCItemCreateDto> items;

    public DCCreateDto() {
    }

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getDeliveryAddressId() {
        return deliveryAddressId;
    }

    public void setDeliveryAddressId(String deliveryAddressId) {
        this.deliveryAddressId = deliveryAddressId;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<DCItemCreateDto> getItems() {
        return items;
    }

    public void setItems(List<DCItemCreateDto> items) {
        this.items = items;
    }
}
