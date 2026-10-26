package com.factory.reporting.dto;

import com.factory.po.dto.POResponseDto;

import java.util.List;

public class OuterStateDashboardDto {
    private String officeId;
    private String officeName;
    private long totalPOCount;
    private long draftPOCount;
    private long pendingApprovalCount;
    private long approvedPOCount;
    private long partiallyDeliveredPOCount;
    private long deliveredPOCount;
    private List<POResponseDto> recentPOs;

    public OuterStateDashboardDto() {
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public long getTotalPOCount() {
        return totalPOCount;
    }

    public void setTotalPOCount(long totalPOCount) {
        this.totalPOCount = totalPOCount;
    }

    public long getDraftPOCount() {
        return draftPOCount;
    }

    public void setDraftPOCount(long draftPOCount) {
        this.draftPOCount = draftPOCount;
    }

    public long getPendingApprovalCount() {
        return pendingApprovalCount;
    }

    public void setPendingApprovalCount(long pendingApprovalCount) {
        this.pendingApprovalCount = pendingApprovalCount;
    }

    public long getApprovedPOCount() {
        return approvedPOCount;
    }

    public void setApprovedPOCount(long approvedPOCount) {
        this.approvedPOCount = approvedPOCount;
    }

    public long getPartiallyDeliveredPOCount() {
        return partiallyDeliveredPOCount;
    }

    public void setPartiallyDeliveredPOCount(long partiallyDeliveredPOCount) {
        this.partiallyDeliveredPOCount = partiallyDeliveredPOCount;
    }

    public long getDeliveredPOCount() {
        return deliveredPOCount;
    }

    public void setDeliveredPOCount(long deliveredPOCount) {
        this.deliveredPOCount = deliveredPOCount;
    }

    public List<POResponseDto> getRecentPOs() {
        return recentPOs;
    }

    public void setRecentPOs(List<POResponseDto> recentPOs) {
        this.recentPOs = recentPOs;
    }
}
