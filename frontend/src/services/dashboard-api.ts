import { apiClient, ApiResponse } from './api-client';
import { POResponseDto } from './purchase-order-api';
import { AuditLogItem } from './audit-api';
import { DCResponseDto } from './delivery-api';

export interface OwnerDashboardData {
  totalRevenueINR: number;
  pendingApprovalCount: number;
  openShortageCount: number;
  totalInventoryValuationINR: number;
  poFulfillmentRatePercent: number;
  totalPOCount: number;
  totalClientCount: number;
  recentPOs: POResponseDto[];
  recentActivities: AuditLogItem[];
}

export interface AdminDashboardData {
  totalUsersCount: number;
  activeOfficesCount: number;
  totalProductsCount: number;
  totalAuditLogsCount: number;
  recentAuditLogs: AuditLogItem[];
}

export interface MainOfficeDashboardData {
  totalPOCount: number;
  draftPOCount: number;
  pendingApprovalCount: number;
  approvedPOCount: number;
  partiallyDeliveredPOCount: number;
  deliveredPOCount: number;
  openShortageCount: number;
  recentPOs: POResponseDto[];
}

export interface OuterStateDashboardData {
  officeId: string;
  officeName: string;
  totalPOCount: number;
  draftPOCount: number;
  pendingApprovalCount: number;
  approvedPOCount: number;
  partiallyDeliveredPOCount: number;
  deliveredPOCount: number;
  recentPOs: POResponseDto[];
}

export interface GodownDashboardData {
  totalOnHandQuantity: number;
  totalReservedQuantity: number;
  totalAvailableQuantity: number;
  pendingDispatchDCCount: number;
  openShortageCount: number;
  recentDispatches: DCResponseDto[];
}

export const dashboardApi = {
  getSummary: async (): Promise<ApiResponse<any>> => {
    const res = await apiClient.get<ApiResponse<any>>('/dashboard/summary');
    return res.data;
  },

  getOwnerDashboard: async (): Promise<ApiResponse<OwnerDashboardData>> => {
    const res = await apiClient.get<ApiResponse<OwnerDashboardData>>('/dashboard/owner');
    return res.data;
  },

  getAdminDashboard: async (): Promise<ApiResponse<AdminDashboardData>> => {
    const res = await apiClient.get<ApiResponse<AdminDashboardData>>('/dashboard/admin');
    return res.data;
  },

  getMainOfficeDashboard: async (): Promise<ApiResponse<MainOfficeDashboardData>> => {
    const res = await apiClient.get<ApiResponse<MainOfficeDashboardData>>('/dashboard/main-office');
    return res.data;
  },

  getOuterStateDashboard: async (): Promise<ApiResponse<OuterStateDashboardData>> => {
    const res = await apiClient.get<ApiResponse<OuterStateDashboardData>>('/dashboard/outer-state');
    return res.data;
  },

  getGodownDashboard: async (): Promise<ApiResponse<GodownDashboardData>> => {
    const res = await apiClient.get<ApiResponse<GodownDashboardData>>('/dashboard/godown');
    return res.data;
  },
};
