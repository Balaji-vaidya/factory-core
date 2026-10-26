package com.factory.po.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "po_version_changes")
public class POVersionChange {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private POVersion version;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "previous_value", columnDefinition = "TEXT")
    private String previousValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    public POVersionChange() {
        this.id = UUID.randomUUID().toString();
    }

    public POVersionChange(String fieldName, String previousValue, String newValue) {
        this.id = UUID.randomUUID().toString();
        this.fieldName = fieldName;
        this.previousValue = previousValue;
        this.newValue = newValue;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public POVersion getVersion() {
        return version;
    }

    public void setVersion(POVersion version) {
        this.version = version;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(String previousValue) {
        this.previousValue = previousValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
}
