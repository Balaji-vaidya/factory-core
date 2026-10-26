package com.factory.inventory.entity;

import com.factory.office.entity.Office;
import com.factory.product.entity.Product;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_ledger", indexes = {
        @Index(name = "idx_ledger_inventory", columnList = "inventory_id"),
        @Index(name = "idx_ledger_office", columnList = "office_id"),
        @Index(name = "idx_ledger_product", columnList = "product_id"),
        @Index(name = "idx_ledger_tx_type", columnList = "transaction_type"),
        @Index(name = "idx_ledger_created_at", columnList = "created_at")
})
public class StockLedgerEntry {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private StockTransactionType transactionType;

    @Column(name = "quantity_delta", nullable = false)
    private int quantityDelta;

    @Column(name = "on_hand_before", nullable = false)
    private int onHandBefore;

    @Column(name = "on_hand_after", nullable = false)
    private int onHandAfter;

    @Column(name = "reserved_before", nullable = false)
    private int reservedBefore;

    @Column(name = "reserved_after", nullable = false)
    private int reservedAfter;

    @Column(name = "available_before", nullable = false)
    private int availableBefore;

    @Column(name = "available_after", nullable = false)
    private int availableAfter;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public StockLedgerEntry() {
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
