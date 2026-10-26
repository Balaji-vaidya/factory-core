package com.factory.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class POFulfillmentReportItemDto {
    private String poId;
    private String poNumber;
    private String clientName;
    private String officeName;
    private LocalDate poDate;
    private LocalDate expectedDeliveryDate;
    private String status;
    private String priority;
    private int totalOrderedQuantity;
    private int totalDeliveredQuantity;
    private double fulfillmentPercent;
    private BigDecimal totalAmountINR;

    public POFulfillmentReportItemDto() {
    }

    public String getPoId() {
        return poId;
    }

    public void setPoId(String poId) {
        this.poId = poId;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public int getTotalOrderedQuantity() {
        return totalOrderedQuantity;
    }

    public void setTotalOrderedQuantity(int totalOrderedQuantity) {
        this.totalOrderedQuantity = totalOrderedQuantity;
    }

    public int getTotalDeliveredQuantity() {
        return totalDeliveredQuantity;
    }

    public void setTotalDeliveredQuantity(int totalDeliveredQuantity) {
        this.totalDeliveredQuantity = totalDeliveredQuantity;
    }

    public double getFulfillmentPercent() {
        return fulfillmentPercent;
    }

    public void setFulfillmentPercent(double fulfillmentPercent) {
        this.fulfillmentPercent = fulfillmentPercent;
    }

    public BigDecimal getTotalAmountINR() {
        return totalAmountINR;
    }

    public void setTotalAmountINR(BigDecimal totalAmountINR) {
        this.totalAmountINR = totalAmountINR;
    }
}
