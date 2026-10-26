import { apiClient, ApiResponse } from './api-client';

export interface DocumentItem {
  id: string;
  fileName: string;
  fileType: string;
  fileSize: number;
  entityType: string;
  entityId: string;
  uploadedBy?: string;
  createdAt: string;
}

export const documentApi = {
  uploadDocument: async (file: File, entityType: string, entityId: string): Promise<ApiResponse<DocumentItem>> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('entityType', entityType);
    formData.append('entityId', entityId);

    const res = await apiClient.post<ApiResponse<DocumentItem>>('/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data;
  },

  getEntityDocuments: async (entityType: string, entityId: string): Promise<ApiResponse<DocumentItem[]>> => {
    const res = await apiClient.get<ApiResponse<DocumentItem[]>>(`/documents/entity/${entityType}/${entityId}`);
    return res.data;
  },

  downloadDocument: async (id: string): Promise<Blob> => {
    const res = await apiClient.get(`/documents/${id}/download`, {
      responseType: 'blob',
    });
    return res.data;
  },

  deleteDocument: async (id: string): Promise<ApiResponse<void>> => {
    const res = await apiClient.delete<ApiResponse<void>>(`/documents/${id}`);
    return res.data;
  },
};
