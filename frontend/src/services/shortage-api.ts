import { apiClient, ApiResponse } from './api-client';
import { PageResponse } from './client-api';

export type ShortageStatus = 'OPEN' | 'PARTIALLY_RESOLVED' | 'RESOLVED' | 'CANCELLED';

export interface StockShortageResponseDto {
  id: string;
  poId: string;
  poNumber: string;
  poDate: string;
  expectedDeliveryDate: string;
  clientCompanyName: string;
  poItemId: string;
  officeId: string;
  officeName: string;
  productId: string;
  sku: string;
  productName: string;
  requiredQuantity: number;
  allocatedQuantity: number;
  shortageQuantity: number;
  status: ShortageStatus;
  createdAt: string;
  updatedAt: string;
}

export const shortageApi = {
  searchShortages: async (params?: {
    officeId?: string;
    productId?: string;
    status?: ShortageStatus;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<PageResponse<StockShortageResponseDto>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<StockShortageResponseDto>>>('/shortages', { params });
    return response.data.data;
  }
};
