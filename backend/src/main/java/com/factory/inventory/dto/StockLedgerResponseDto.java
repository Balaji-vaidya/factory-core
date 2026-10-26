package com.factory.inventory.dto;

import com.factory.inventory.entity.StockTransactionType;

import java.time.Instant;

public class StockLedgerResponseDto {

    private String id;
    private String inventoryId;
    private String officeId;
    private String officeName;
    private String productId;
    private String sku;
    private String productName;
    private StockTransactionType transactionType;
    private int quantityDelta;
    private int onHandBefore;
    private int onHandAfter;
    private int reservedBefore;
    private int reservedAfter;
    private int availableBefore;
    private int availableAfter;
    private String referenceId;
    private String referenceType;
    private String notes;
    private String createdBy;
    private Instant createdAt;

    public StockLedgerResponseDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public StockTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(StockTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public int getQuantityDelta() {
        return quantityDelta;
    }

    public void setQuantityDelta(int quantityDelta) {
        this.quantityDelta = quantityDelta;
    }

    public int getOnHandBefore() {
        return onHandBefore;
    }

    public void setOnHandBefore(int onHandBefore) {
        this.onHandBefore = onHandBefore;
    }

    public int getOnHandAfter() {
        return onHandAfter;
    }

    public void setOnHandAfter(int onHandAfter) {
        this.onHandAfter = onHandAfter;
    }

    public int getReservedBefore() {
        return reservedBefore;
    }

    public void setReservedBefore(int reservedBefore) {
        this.reservedBefore = reservedBefore;
    }

    public int getReservedAfter() {
        return reservedAfter;
    }

    public void setReservedAfter(int reservedAfter) {
        this.reservedAfter = reservedAfter;
    }

    public int getAvailableBefore() {
        return availableBefore;
    }

    public void setAvailableBefore(int availableBefore) {
        this.availableBefore = availableBefore;
    }

    public int getAvailableAfter() {
        return availableAfter;
    }

    public void setAvailableAfter(int availableAfter) {
        this.availableAfter = availableAfter;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
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
}
