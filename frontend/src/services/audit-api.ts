import { apiClient, ApiResponse } from './api-client';
import { PageResponse } from './notification-api';

export interface AuditLogItem {
  id: string;
  userId?: string;
  userEmail?: string;
  action: string;
  entityName: string;
  entityId?: string;
  details?: string;
  ipAddress?: string;
  createdAt: string;
}

export interface AuditLogSearchParams {
  action?: string;
  entityName?: string;
  userId?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

export const auditApi = {
  searchLogs: async (params: AuditLogSearchParams = {}): Promise<ApiResponse<PageResponse<AuditLogItem>>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<AuditLogItem>>>('/audit/logs', {
      params,
    });
    return res.data;
  },

  getEntityLogs: async (
    entityName: string,
    entityId: string,
    page = 0,
    size = 20
  ): Promise<ApiResponse<PageResponse<AuditLogItem>>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<AuditLogItem>>>(`/audit/logs/entity/${entityName}/${entityId}`, {
      params: { page, size },
    });
    return res.data;
  },
};
