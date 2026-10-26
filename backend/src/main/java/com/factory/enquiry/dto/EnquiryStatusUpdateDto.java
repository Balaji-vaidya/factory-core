package com.factory.enquiry.dto;

import com.factory.enquiry.entity.EnquiryStatus;
import jakarta.validation.constraints.NotNull;

public class EnquiryStatusUpdateDto {

    @NotNull(message = "Enquiry status is required")
    private EnquiryStatus status;

    private String internalNotes;

    public EnquiryStatusUpdateDto() {
    }

    public EnquiryStatus getStatus() {
        return status;
    }

    public void setStatus(EnquiryStatus status) {
        this.status = status;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }
}
