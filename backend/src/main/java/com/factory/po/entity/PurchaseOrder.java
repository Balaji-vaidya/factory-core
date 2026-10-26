package com.factory.po.entity;

import com.factory.client.entity.Client;
import com.factory.office.entity.Office;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purchase_orders", indexes = {
        @Index(name = "idx_po_client_id", columnList = "client_id"),
        @Index(name = "idx_po_office_id", columnList = "office_id"),
        @Index(name = "idx_po_status", columnList = "status"),
        @Index(name = "idx_po_date", columnList = "po_date")
})
public class PurchaseOrder {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "po_number", nullable = false, unique = true, length = 30)
    private String poNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "client_snapshot", nullable = false, columnDefinition = "TEXT")
    private String clientSnapshot;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private POPriority priority = POPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private POStatus status = POStatus.DRAFT;

    @Column(name = "total_amount_inr", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmountINR = BigDecimal.ZERO;

    @Column(name = "po_date", nullable = false)
    private LocalDate poDate;

    @Column(name = "expected_delivery_date", nullable = false)
    private LocalDate expectedDeliveryDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "current_version", nullable = false)
    private int currentVersion = 1;

    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    @Column(name = "approved_by", length = 36)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<POItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<POVersion> versions = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public PurchaseOrder() {
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
        recalculateTotalAmount();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        recalculateTotalAmount();
    }

    public void addItem(POItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
        recalculateTotalAmount();
    }

    public void removeItem(POItem item) {
        items.remove(item);
        item.setPurchaseOrder(null);
        recalculateTotalAmount();
    }

    public void addVersion(POVersion version) {
        versions.add(version);
        version.setPurchaseOrder(this);
    }

    public void recalculateTotalAmount() {
        if (items != null && !items.isEmpty()) {
            this.totalAmountINR = items.stream()
                    .map(POItem::getLineTotalINR)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        } else if (this.totalAmountINR == null) {
            this.totalAmountINR = BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getClientSnapshot() {
        return clientSnapshot;
    }

    public void setClientSnapshot(String clientSnapshot) {
        this.clientSnapshot = clientSnapshot;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
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

    public List<POItem> getItems() {
        return items;
    }

    public void setItems(List<POItem> items) {
        this.items = items;
        recalculateTotalAmount();
    }

    public List<POVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<POVersion> versions) {
        this.versions = versions;
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
