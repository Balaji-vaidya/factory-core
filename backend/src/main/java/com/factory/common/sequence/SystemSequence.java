package com.factory.common.sequence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "system_sequences")
public class SystemSequence {

    @Id
    @Column(name = "sequence_name", length = 50, nullable = false)
    private String sequenceName;

    @Column(name = "current_value", nullable = false)
    private Long currentValue = 0L;

    public SystemSequence() {
    }

    public SystemSequence(String sequenceName, Long currentValue) {
        this.sequenceName = sequenceName;
        this.currentValue = currentValue;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public Long getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Long currentValue) {
        this.currentValue = currentValue;
    }
}
