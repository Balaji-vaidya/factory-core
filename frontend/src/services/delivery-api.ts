import { apiClient, ApiResponse } from './api-client';
import { PageResponse } from './client-api';

export type DCStatus = 'DRAFT' | 'DISPATCHED' | 'DELIVERED' | 'CANCELLED';

export interface DCItemDto {
  id?: string;
  poItemId: string;
  productId?: string;
  sku?: string;
  productName?: string;
  unit?: string;
  quantity: number;
  notes?: string;
}

export interface DCResponseDto {
  id: string;
  dcNumber: string;
  purchaseOrderId: string;
  poNumber: string;
  clientId: string;
  clientCompanyName: string;
  officeId: string;
  officeName: string;
  deliveryAddressId?: string;
  deliveryAddressSnapshot: string;
  status: DCStatus;
  vehicleNumber: string;
  driverName: string;
  driverPhone: string;
  transporterName: string;
  lrNumber?: string;
  ewayBillNumber?: string;
  ewayBillDate?: string;
  challanDate: string;
  dispatchedAt?: string;
  dispatchedBy?: string;
  deliveredAt?: string;
  deliveryConfirmedBy?: string;
  recipientName?: string;
  recipientPhone?: string;
  proofOfDeliveryUrl?: string;
  notes?: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  items: DCItemDto[];
}

export interface DCItemCreateDto {
  poItemId: string;
  quantity: number;
  notes?: string;
}

export interface DCCreateDto {
  purchaseOrderId: string;
  deliveryAddressId?: string;
  vehicleNumber: string;
  driverName: string;
  driverPhone: string;
  transporterName: string;
  lrNumber?: string;
  ewayBillNumber?: string;
  ewayBillDate?: string;
  challanDate?: string;
  notes?: string;
  items: DCItemCreateDto[];
}

export interface DCDeliveryConfirmDto {
  recipientName: string;
  recipientPhone: string;
  deliveryDate?: string;
  proofOfDeliveryUrl?: string;
  notes?: string;
}

export const deliveryApi = {
  getDeliveryChallans: async (params?: {
    query?: string;
    officeId?: string;
    status?: DCStatus;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<PageResponse<DCResponseDto>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<DCResponseDto>>>('/delivery-challans', { params });
    return response.data.data;
  },

  getDeliveryChallanById: async (id: string): Promise<DCResponseDto> => {
    const response = await apiClient.get<ApiResponse<DCResponseDto>>(`/delivery-challans/${id}`);
    return response.data.data;
  },

  getDeliveryChallanByNumber: async (dcNumber: string): Promise<DCResponseDto> => {
    const response = await apiClient.get<ApiResponse<DCResponseDto>>(`/delivery-challans/number/${dcNumber}`);
    return response.data.data;
  },

  getChallansByPO: async (poId: string): Promise<DCResponseDto[]> => {
    const response = await apiClient.get<ApiResponse<DCResponseDto[]>>(`/delivery-challans/po/${poId}`);
    return response.data.data;
  },

  createDeliveryChallan: async (data: DCCreateDto): Promise<DCResponseDto> => {
    const response = await apiClient.post<ApiResponse<DCResponseDto>>('/delivery-challans', data);
    return response.data.data;
  },

  dispatchDeliveryChallan: async (id: string): Promise<DCResponseDto> => {
    const response = await apiClient.post<ApiResponse<DCResponseDto>>(`/delivery-challans/${id}/dispatch`);
    return response.data.data;
  },

  confirmDelivery: async (id: string, data: DCDeliveryConfirmDto): Promise<DCResponseDto> => {
    const response = await apiClient.post<ApiResponse<DCResponseDto>>(`/delivery-challans/${id}/confirm-delivery`, data);
    return response.data.data;
  }
};
