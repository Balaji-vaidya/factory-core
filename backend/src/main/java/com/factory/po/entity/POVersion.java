package com.factory.po.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "po_versions", indexes = {
        @Index(name = "idx_po_versions_po_id", columnList = "po_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_po_versions_po_version", columnNames = {"po_id", "version_number"})
})
public class POVersion {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "changed_by", nullable = false, length = 36)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt = Instant.now();

    @Column(name = "change_summary", nullable = false, length = 255)
    private String changeSummary;

    @Column(name = "snapshot_data", nullable = false, columnDefinition = "LONGTEXT")
    private String snapshotData;

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<POVersionChange> changes = new ArrayList<>();

    public POVersion() {
        this.id = UUID.randomUUID().toString();
    }

    public void addChange(POVersionChange change) {
        changes.add(change);
        change.setVersion(this);
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

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    public String getSnapshotData() {
        return snapshotData;
    }

    public void setSnapshotData(String snapshotData) {
        this.snapshotData = snapshotData;
    }

    public List<POVersionChange> getChanges() {
        return changes;
    }

    public void setChanges(List<POVersionChange> changes) {
        this.changes = changes;
    }
}
