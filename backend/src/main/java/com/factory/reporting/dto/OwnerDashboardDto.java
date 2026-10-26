package com.factory.reporting.dto;

import com.factory.audit.dto.AuditLogResponseDto;
import com.factory.po.dto.POResponseDto;

import java.math.BigDecimal;
import java.util.List;

public class OwnerDashboardDto {
    private BigDecimal totalRevenueINR = BigDecimal.ZERO;
    private long pendingApprovalCount;
    private long openShortageCount;
    private BigDecimal totalInventoryValuationINR = BigDecimal.ZERO;
    private double poFulfillmentRatePercent;
    private long totalPOCount;
    private long totalClientCount;
    private List<POResponseDto> recentPOs;
    private List<AuditLogResponseDto> recentActivities;

    public OwnerDashboardDto() {
    }

    public BigDecimal getTotalRevenueINR() {
        return totalRevenueINR;
    }

    public void setTotalRevenueINR(BigDecimal totalRevenueINR) {
        this.totalRevenueINR = totalRevenueINR;
    }

    public long getPendingApprovalCount() {
        return pendingApprovalCount;
    }

    public void setPendingApprovalCount(long pendingApprovalCount) {
        this.pendingApprovalCount = pendingApprovalCount;
    }

    public long getOpenShortageCount() {
        return openShortageCount;
    }

    public void setOpenShortageCount(long openShortageCount) {
        this.openShortageCount = openShortageCount;
    }

    public BigDecimal getTotalInventoryValuationINR() {
        return totalInventoryValuationINR;
    }

    public void setTotalInventoryValuationINR(BigDecimal totalInventoryValuationINR) {
        this.totalInventoryValuationINR = totalInventoryValuationINR;
    }

    public double getPoFulfillmentRatePercent() {
        return poFulfillmentRatePercent;
    }

    public void setPoFulfillmentRatePercent(double poFulfillmentRatePercent) {
        this.poFulfillmentRatePercent = poFulfillmentRatePercent;
    }

    public long getTotalPOCount() {
        return totalPOCount;
    }

    public void setTotalPOCount(long totalPOCount) {
        this.totalPOCount = totalPOCount;
    }

    public long getTotalClientCount() {
        return totalClientCount;
    }

    public void setTotalClientCount(long totalClientCount) {
        this.totalClientCount = totalClientCount;
    }

    public List<POResponseDto> getRecentPOs() {
        return recentPOs;
    }

    public void setRecentPOs(List<POResponseDto> recentPOs) {
        this.recentPOs = recentPOs;
    }

    public List<AuditLogResponseDto> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<AuditLogResponseDto> recentActivities) {
        this.recentActivities = recentActivities;
    }
}
