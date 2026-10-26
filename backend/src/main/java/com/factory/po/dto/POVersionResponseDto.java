package com.factory.po.dto;

import java.time.Instant;
import java.util.List;

public class POVersionResponseDto {

    private int versionNumber;
    private String changedBy;
    private Instant changedAt;
    private String changeSummary;
    private List<POVersionChangeDto> changes;
    private String snapshotData;

    public static class POVersionChangeDto {
        private String fieldName;
        private String previousValue;
        private String newValue;

        public POVersionChangeDto() {}

        public POVersionChangeDto(String fieldName, String previousValue, String newValue) {
            this.fieldName = fieldName;
            this.previousValue = previousValue;
            this.newValue = newValue;
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

    public POVersionResponseDto() {}

    // Getters and Setters

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

    public List<POVersionChangeDto> getChanges() {
        return changes;
    }

    public void setChanges(List<POVersionChangeDto> changes) {
        this.changes = changes;
    }

    public String getSnapshotData() {
        return snapshotData;
    }

    public void setSnapshotData(String snapshotData) {
        this.snapshotData = snapshotData;
    }
}
