package com.factory.delivery.entity;

import com.factory.po.entity.POItem;
import com.factory.product.entity.Product;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dc_items", indexes = {
        @Index(name = "idx_dc_items_challan", columnList = "delivery_challan_id"),
        @Index(name = "idx_dc_items_po_item", columnList = "po_item_id")
})
public class DCItem {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_challan_id", nullable = false)
    private DeliveryChallan deliveryChallan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "po_item_id", nullable = false)
    private POItem poItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_snapshot", nullable = false, columnDefinition = "TEXT")
    private String productSnapshot;

    @Column(nullable = false)
    private int quantity;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public DCItem() {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DeliveryChallan getDeliveryChallan() {
        return deliveryChallan;
    }

    public void setDeliveryChallan(DeliveryChallan deliveryChallan) {
        this.deliveryChallan = deliveryChallan;
    }

    public POItem getPoItem() {
        return poItem;
    }

    public void setPoItem(POItem poItem) {
        this.poItem = poItem;
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
}
