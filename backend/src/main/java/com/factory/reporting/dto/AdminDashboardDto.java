package com.factory.reporting.dto;

import com.factory.audit.dto.AuditLogResponseDto;

import java.util.List;

public class AdminDashboardDto {
    private long totalUsersCount;
    private long activeOfficesCount;
    private long totalProductsCount;
    private long totalAuditLogsCount;
    private List<AuditLogResponseDto> recentAuditLogs;

    public AdminDashboardDto() {
    }

    public long getTotalUsersCount() {
        return totalUsersCount;
    }

    public void setTotalUsersCount(long totalUsersCount) {
        this.totalUsersCount = totalUsersCount;
    }

    public long getActiveOfficesCount() {
        return activeOfficesCount;
    }

    public void setActiveOfficesCount(long activeOfficesCount) {
        this.activeOfficesCount = activeOfficesCount;
    }

    public long getTotalProductsCount() {
        return totalProductsCount;
    }

    public void setTotalProductsCount(long totalProductsCount) {
        this.totalProductsCount = totalProductsCount;
    }

    public long getTotalAuditLogsCount() {
        return totalAuditLogsCount;
    }

    public void setTotalAuditLogsCount(long totalAuditLogsCount) {
        this.totalAuditLogsCount = totalAuditLogsCount;
    }

    public List<AuditLogResponseDto> getRecentAuditLogs() {
        return recentAuditLogs;
    }

    public void setRecentAuditLogs(List<AuditLogResponseDto> recentAuditLogs) {
        this.recentAuditLogs = recentAuditLogs;
    }
}
