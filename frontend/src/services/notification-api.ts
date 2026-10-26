import { apiClient, ApiResponse } from './api-client';

export interface NotificationItem {
  id: string;
  title: string;
  message: string;
  type: string;
  referenceId?: string;
  referenceType?: string;
  read: boolean;
  readAt?: string;
  createdAt: string;
}

export interface PageResponse<T> {
  items: T[];
  currentPage: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface UnreadCountResponse {
  unreadCount: number;
}

export const notificationApi = {
  getNotifications: async (page = 0, size = 20): Promise<ApiResponse<PageResponse<NotificationItem>>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<NotificationItem>>>('/notifications', {
      params: { page, size },
    });
    return res.data;
  },

  getUnreadCount: async (): Promise<ApiResponse<UnreadCountResponse>> => {
    const res = await apiClient.get<ApiResponse<UnreadCountResponse>>('/notifications/unread-count');
    return res.data;
  },

  markAsRead: async (id: string): Promise<ApiResponse<void>> => {
    const res = await apiClient.put<ApiResponse<void>>(`/notifications/${id}/read`);
    return res.data;
  },

  markAllAsRead: async (): Promise<ApiResponse<void>> => {
    const res = await apiClient.put<ApiResponse<void>>('/notifications/read-all');
    return res.data;
  },
};
