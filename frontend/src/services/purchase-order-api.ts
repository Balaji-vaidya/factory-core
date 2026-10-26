import { apiClient, ApiResponse } from './api-client';
import { PageResponse } from './client-api';

export interface POItemDto {
  id?: string;
  productId: string;
  sku?: string;
  productName?: string;
  unit?: string;
  quantity: number;
  unitPriceINR: number;
  lineTotalINR?: number;
  reservedQuantity?: number;
  dispatchedQuantity?: number;
  deliveredQuantity?: number;
  expectedDeliveryDate?: string;
  notes?: string;
}

export interface POVersionChangeDto {
  fieldName: string;
  previousValue: string;
  newValue: string;
}

export interface POVersionDto {
  versionNumber: number;
  changedBy: string;
  changedAt: string;
  changeSummary: string;
  changes: POVersionChangeDto[];
  snapshotData?: string;
}

export interface POResponseDto {
  id: string;
  poNumber: string;
  clientId: string;
  clientCompanyName: string;
  clientState: string;
  clientSnapshot: string;
  officeId: string;
  officeName: string;
  officeState: string;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  status: 'DRAFT' | 'PENDING_OWNER_APPROVAL' | 'RETURNED' | 'APPROVED' | 'NOT_DELIVERED' | 'PARTIALLY_DELIVERED' | 'DELIVERED' | 'CANCEL_REQUESTED' | 'CANCELLED';
  totalAmountINR: number;
  poDate: string;
  expectedDeliveryDate: string;
  notes?: string;
  rejectionReason?: string;
  cancellationReason?: string;
  currentVersion: number;
  createdBy: string;
  approvedBy?: string;
  approvedAt?: string;
  createdAt: string;
  updatedAt: string;
  items: POItemDto[];
  versions?: POVersionDto[];
}

export interface POCreateDto {
  clientId: string;
  deliveryAddressId?: string;
  officeId: string;
  priority?: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  poDate?: string;
  expectedDeliveryDate: string;
  notes?: string;
  items: POItemDto[];
  submitForApproval?: boolean;
}

export interface POUpdateDto {
  clientId?: string;
  deliveryAddressId?: string;
  priority?: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  expectedDeliveryDate: string;
  notes?: string;
  items: POItemDto[];
  changeReason?: string;
  submitForApproval?: boolean;
}

export const purchaseOrderApi = {
  getPurchaseOrders: async (params?: {
    query?: string;
    officeId?: string;
    state?: string;
    status?: string;
    priority?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<PageResponse<POResponseDto>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<POResponseDto>>>('/purchase-orders', { params });
    return response.data.data;
  },

  getPurchaseOrderById: async (id: string): Promise<POResponseDto> => {
    const response = await apiClient.get<ApiResponse<POResponseDto>>(`/purchase-orders/${id}`);
    return response.data.data;
  },

  getPurchaseOrderByNumber: async (poNumber: string): Promise<POResponseDto> => {
    const response = await apiClient.get<ApiResponse<POResponseDto>>(`/purchase-orders/number/${poNumber}`);
    return response.data.data;
  },

  createPurchaseOrder: async (data: POCreateDto): Promise<POResponseDto> => {
    const response = await apiClient.post<ApiResponse<POResponseDto>>('/purchase-orders', data);
    return response.data.data;
  },

  updatePurchaseOrder: async (id: string, data: POUpdateDto): Promise<POResponseDto> => {
    const response = await apiClient.put<ApiResponse<POResponseDto>>(`/purchase-orders/${id}`, data);
    return response.data.data;
  },

  submitForApproval: async (id: string): Promise<POResponseDto> => {
    const response = await apiClient.post<ApiResponse<POResponseDto>>(`/purchase-orders/${id}/submit`);
    return response.data.data;
  },

  approvePurchaseOrder: async (id: string, notes?: string): Promise<POResponseDto> => {
    const response = await apiClient.post<ApiResponse<POResponseDto>>(`/purchase-orders/${id}/approve`, null, {
      params: { notes }
    });
    return response.data.data;
  },

  returnPurchaseOrder: async (id: string, reason: string): Promise<POResponseDto> => {
    const response = await apiClient.post<ApiResponse<POResponseDto>>(`/purchase-orders/${id}/return`, null, {
      params: { reason }
    });
    return response.data.data;
  },

  requestCancellation: async (id: string, reason: string): Promise<POResponseDto> => {
    const response = await apiClient.post<ApiResponse<POResponseDto>>(`/purchase-orders/${id}/cancel-request`, null, {
      params: { reason }
    });
    return response.data.data;
  },

  approveCancellation: async (id: string): Promise<POResponseDto> => {
    const response = await apiClient.post<ApiResponse<POResponseDto>>(`/purchase-orders/${id}/cancel-approve`);
    return response.data.data;
  },

  getVersionHistory: async (id: string): Promise<POVersionDto[]> => {
    const response = await apiClient.get<ApiResponse<POVersionDto[]>>(`/purchase-orders/${id}/versions`);
    return response.data.data;
  }
};
