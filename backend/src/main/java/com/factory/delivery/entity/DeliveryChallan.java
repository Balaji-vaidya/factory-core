package com.factory.delivery.entity;

import com.factory.client.entity.Client;
import com.factory.office.entity.Office;
import com.factory.po.entity.PurchaseOrder;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "delivery_challans", indexes = {
        @Index(name = "idx_dc_po", columnList = "purchase_order_id"),
        @Index(name = "idx_dc_client", columnList = "client_id"),
        @Index(name = "idx_dc_office", columnList = "office_id"),
        @Index(name = "idx_dc_status", columnList = "status")
})
public class DeliveryChallan {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "dc_number", nullable = false, unique = true, length = 30)
    private String dcNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "delivery_address_id", length = 36)
    private String deliveryAddressId;

    @Column(name = "delivery_address_snapshot", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddressSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DCStatus status = DCStatus.DRAFT;

    @Column(name = "vehicle_number", nullable = false, length = 30)
    private String vehicleNumber;

    @Column(name = "driver_name", nullable = false, length = 100)
    private String driverName;

    @Column(name = "driver_phone", nullable = false, length = 20)
    private String driverPhone;

    @Column(name = "transporter_name", nullable = false, length = 150)
    private String transporterName;

    @Column(name = "lr_number", length = 50)
    private String lrNumber;

    @Column(name = "eway_bill_number", length = 20)
    private String ewayBillNumber;

    @Column(name = "eway_bill_date")
    private LocalDate ewayBillDate;

    @Column(name = "challan_date", nullable = false)
    private LocalDate challanDate;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "dispatched_by", length = 36)
    private String dispatchedBy;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "delivery_confirmed_by", length = 36)
    private String deliveryConfirmedBy;

    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Column(name = "proof_of_delivery_url", length = 255)
    private String proofOfDeliveryUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    @OneToMany(mappedBy = "deliveryChallan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DCItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public DeliveryChallan() {
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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addItem(DCItem item) {
        items.add(item);
        item.setDeliveryChallan(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDcNumber() {
        return dcNumber;
    }

    public void setDcNumber(String dcNumber) {
        this.dcNumber = dcNumber;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public String getDeliveryAddressId() {
        return deliveryAddressId;
    }

    public void setDeliveryAddressId(String deliveryAddressId) {
        this.deliveryAddressId = deliveryAddressId;
    }

    public String getDeliveryAddressSnapshot() {
        return deliveryAddressSnapshot;
    }

    public void setDeliveryAddressSnapshot(String deliveryAddressSnapshot) {
        this.deliveryAddressSnapshot = deliveryAddressSnapshot;
    }

    public DCStatus getStatus() {
        return status;
    }

    public void setStatus(DCStatus status) {
        this.status = status;
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

    public Instant getDispatchedAt() {
        return dispatchedAt;
    }

    public void setDispatchedAt(Instant dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    public String getDispatchedBy() {
        return dispatchedBy;
    }

    public void setDispatchedBy(String dispatchedBy) {
        this.dispatchedBy = dispatchedBy;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getDeliveryConfirmedBy() {
        return deliveryConfirmedBy;
    }

    public void setDeliveryConfirmedBy(String deliveryConfirmedBy) {
        this.deliveryConfirmedBy = deliveryConfirmedBy;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getProofOfDeliveryUrl() {
        return proofOfDeliveryUrl;
    }

    public void setProofOfDeliveryUrl(String proofOfDeliveryUrl) {
        this.proofOfDeliveryUrl = proofOfDeliveryUrl;
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

    public List<DCItem> getItems() {
        return items;
    }

    public void setItems(List<DCItem> items) {
        this.items = items;
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
