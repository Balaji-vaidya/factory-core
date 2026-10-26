package com.factory.inventory.entity;

import com.factory.office.entity.Office;
import com.factory.po.entity.POItem;
import com.factory.po.entity.PurchaseOrder;
import com.factory.product.entity.Product;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_shortages", indexes = {
        @Index(name = "idx_shortage_po_id", columnList = "po_id"),
        @Index(name = "idx_shortage_office_id", columnList = "office_id"),
        @Index(name = "idx_shortage_product_id", columnList = "product_id"),
        @Index(name = "idx_shortage_status", columnList = "status")
})
public class StockShortage {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "po_item_id", nullable = false)
    private POItem poItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "required_quantity", nullable = false)
    private int requiredQuantity;

    @Column(name = "allocated_quantity", nullable = false)
    private int allocatedQuantity = 0;

    @Column(name = "shortage_quantity", nullable = false)
    private int shortageQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShortageStatus status = ShortageStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public StockShortage() {
    }

    public StockShortage(PurchaseOrder po, POItem poItem, Office office, Product product, int requiredQuantity, int allocatedQuantity) {
        this.purchaseOrder = po;
        this.poItem = poItem;
        this.office = office;
        this.product = product;
        this.requiredQuantity = requiredQuantity;
        this.allocatedQuantity = allocatedQuantity;
        this.shortageQuantity = requiredQuantity - allocatedQuantity;
        updateStatus();
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
        updateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        updateStatus();
    }

    public void updateStatus() {
        this.shortageQuantity = Math.max(0, this.requiredQuantity - this.allocatedQuantity);
        if (this.status != ShortageStatus.CANCELLED) {
            if (this.shortageQuantity == 0) {
                this.status = ShortageStatus.RESOLVED;
            } else if (this.allocatedQuantity > 0) {
                this.status = ShortageStatus.PARTIALLY_RESOLVED;
            } else {
                this.status = ShortageStatus.OPEN;
            }
        }
    }

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

    public POItem getPoItem() {
        return poItem;
    }

    public void setPoItem(POItem poItem) {
        this.poItem = poItem;
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

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(int requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
        updateStatus();
    }

    public int getAllocatedQuantity() {
        return allocatedQuantity;
    }

    public void setAllocatedQuantity(int allocatedQuantity) {
        this.allocatedQuantity = allocatedQuantity;
        updateStatus();
    }

    public int getShortageQuantity() {
        return shortageQuantity;
    }

    public void setShortageQuantity(int shortageQuantity) {
        this.shortageQuantity = shortageQuantity;
    }

    public ShortageStatus getStatus() {
        return status;
    }

    public void setStatus(ShortageStatus status) {
        this.status = status;
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
