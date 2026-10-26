package com.factory.inventory.entity;

import com.factory.office.entity.Office;
import com.factory.product.entity.Product;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory", indexes = {
        @Index(name = "idx_inventory_office", columnList = "office_id"),
        @Index(name = "idx_inventory_product", columnList = "product_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_office_product", columnNames = {"office_id", "product_id"})
})
public class Inventory {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "on_hand_quantity", nullable = false)
    private int onHandQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity = 0;

    @Column(name = "reorder_threshold", nullable = false)
    private int reorderThreshold = 2;

    @Column(name = "last_adjusted_at")
    private Instant lastAdjustedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Inventory() {
    }

    public Inventory(Office office, Product product, int onHandQuantity, int reorderThreshold) {
        this.office = office;
        this.product = product;
        this.onHandQuantity = onHandQuantity;
        this.reservedQuantity = 0;
        this.availableQuantity = onHandQuantity;
        this.reorderThreshold = reorderThreshold;
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
        recalculateAvailable();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        recalculateAvailable();
    }

    public void recalculateAvailable() {
        this.availableQuantity = this.onHandQuantity - this.reservedQuantity;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getOnHandQuantity() {
        return onHandQuantity;
    }

    public void setOnHandQuantity(int onHandQuantity) {
        this.onHandQuantity = onHandQuantity;
        recalculateAvailable();
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
        recalculateAvailable();
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(int reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public Instant getLastAdjustedAt() {
        return lastAdjustedAt;
    }

    public void setLastAdjustedAt(Instant lastAdjustedAt) {
        this.lastAdjustedAt = lastAdjustedAt;
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
