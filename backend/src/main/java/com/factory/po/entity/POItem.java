package com.factory.po.entity;

import com.factory.product.entity.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "po_items", indexes = {
        @Index(name = "idx_po_items_po_id", columnList = "po_id"),
        @Index(name = "idx_po_items_product_id", columnList = "product_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_po_items_po_product", columnNames = {"po_id", "product_id"})
})
public class POItem {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_snapshot", nullable = false, columnDefinition = "TEXT")
    private String productSnapshot;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price_inr", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPriceINR;

    @Column(name = "line_total_inr", nullable = false, precision = 15, scale = 2)
    private BigDecimal lineTotalINR;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity = 0;

    @Column(name = "dispatched_quantity", nullable = false)
    private int dispatchedQuantity = 0;

    @Column(name = "delivered_quantity", nullable = false)
    private int deliveredQuantity = 0;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public POItem() {
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
        calculateLineTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        calculateLineTotal();
    }

    public void calculateLineTotal() {
        if (unitPriceINR != null && quantity > 0) {
            this.lineTotalINR = unitPriceINR.multiply(BigDecimal.valueOf(quantity)).setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getProductSnapshot() {
        return productSnapshot;
    }

    public void setProductSnapshot(String productSnapshot) {
        this.productSnapshot = productSnapshot;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateLineTotal();
    }

    public BigDecimal getUnitPriceINR() {
        return unitPriceINR;
    }

    public void setUnitPriceINR(BigDecimal unitPriceINR) {
        this.unitPriceINR = unitPriceINR;
        calculateLineTotal();
    }

    public BigDecimal getLineTotalINR() {
        if (lineTotalINR == null && unitPriceINR != null && quantity > 0) {
            this.lineTotalINR = unitPriceINR.multiply(BigDecimal.valueOf(quantity)).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return lineTotalINR != null ? lineTotalINR : BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public void setLineTotalINR(BigDecimal lineTotalINR) {
        this.lineTotalINR = lineTotalINR;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public int getDispatchedQuantity() {
        return dispatchedQuantity;
    }

    public void setDispatchedQuantity(int dispatchedQuantity) {
        this.dispatchedQuantity = dispatchedQuantity;
    }

    public int getDeliveredQuantity() {
        return deliveredQuantity;
    }

    public void setDeliveredQuantity(int deliveredQuantity) {
        this.deliveredQuantity = deliveredQuantity;
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
