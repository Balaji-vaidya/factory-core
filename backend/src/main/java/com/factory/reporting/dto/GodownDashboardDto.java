package com.factory.reporting.dto;

import com.factory.delivery.dto.DCResponseDto;

import java.util.List;

public class GodownDashboardDto {
    private int totalOnHandQuantity;
    private int totalReservedQuantity;
    private int totalAvailableQuantity;
    private long pendingDispatchDCCount;
    private long openShortageCount;
    private List<DCResponseDto> recentDispatches;

    public GodownDashboardDto() {
    }

    public int getTotalOnHandQuantity() {
        return totalOnHandQuantity;
    }

    public void setTotalOnHandQuantity(int totalOnHandQuantity) {
        this.totalOnHandQuantity = totalOnHandQuantity;
    }

    public int getTotalReservedQuantity() {
        return totalReservedQuantity;
    }

    public void setTotalReservedQuantity(int totalReservedQuantity) {
        this.totalReservedQuantity = totalReservedQuantity;
    }

    public int getTotalAvailableQuantity() {
        return totalAvailableQuantity;
    }

    public void setTotalAvailableQuantity(int totalAvailableQuantity) {
        this.totalAvailableQuantity = totalAvailableQuantity;
    }

    public long getPendingDispatchDCCount() {
        return pendingDispatchDCCount;
    }

    public void setPendingDispatchDCCount(long pendingDispatchDCCount) {
        this.pendingDispatchDCCount = pendingDispatchDCCount;
    }

    public long getOpenShortageCount() {
        return openShortageCount;
    }

    public void setOpenShortageCount(long openShortageCount) {
        this.openShortageCount = openShortageCount;
    }

    public List<DCResponseDto> getRecentDispatches() {
        return recentDispatches;
    }

    public void setRecentDispatches(List<DCResponseDto> recentDispatches) {
        this.recentDispatches = recentDispatches;
    }
}
