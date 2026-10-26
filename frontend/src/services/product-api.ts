import { apiClient, ApiResponse } from './api-client';
import { PageResponse } from './client-api';

export interface ProductSpecDto {
  capacity: string;
  motorPower: string;
  motorRating?: string;
  dimensions: string;
  weight: string;
  airVolume?: string;
  screenArea?: string;
  seedCompatibility?: string[];
}

export interface ProductResponseDto {
  id: string;
  sku: string;
  name: string;
  category: 'SEED_CLEANER' | 'GRAVITY_SEPARATOR' | 'DESTONER' | 'SEED_TREATER' | 'ELEVATOR' | 'COLOR_SORTER' | 'TURNKEY_PLANT' | 'SPARE_PARTS';
  unit: 'PCS' | 'SET' | 'UNIT' | 'METER' | 'KG' | 'TON';
  description?: string;
  basePriceINR: number;
  leadTimeWeeks: number;
  internalStatus: 'ACTIVE' | 'INACTIVE';
  websiteVisibility: 'PUBLIC' | 'HIDDEN';
  brochureUrl?: string;
  lockedForSkuChange: boolean;
  specs?: ProductSpecDto;
  createdAt: string;
  updatedAt: string;
}

export interface ProductCreateDto {
  sku: string;
  name: string;
  category: 'SEED_CLEANER' | 'GRAVITY_SEPARATOR' | 'DESTONER' | 'SEED_TREATER' | 'ELEVATOR' | 'COLOR_SORTER' | 'TURNKEY_PLANT' | 'SPARE_PARTS';
  unit?: 'PCS' | 'SET' | 'UNIT' | 'METER' | 'KG' | 'TON';
  description?: string;
  basePriceINR: number;
  leadTimeWeeks?: number;
  websiteVisibility?: 'PUBLIC' | 'HIDDEN';
  brochureUrl?: string;
  specs?: ProductSpecDto;
}

export interface ProductUpdateDto {
  sku: string;
  name: string;
  category: 'SEED_CLEANER' | 'GRAVITY_SEPARATOR' | 'DESTONER' | 'SEED_TREATER' | 'ELEVATOR' | 'COLOR_SORTER' | 'TURNKEY_PLANT' | 'SPARE_PARTS';
  unit?: 'PCS' | 'SET' | 'UNIT' | 'METER' | 'KG' | 'TON';
  description?: string;
  basePriceINR: number;
  leadTimeWeeks?: number;
  websiteVisibility?: 'PUBLIC' | 'HIDDEN';
  brochureUrl?: string;
  specs?: ProductSpecDto;
}

export interface ProductSnapshotDto {
  productId: string;
  sku: string;
  name: string;
  category: string;
  unit: string;
  basePriceINR: number;
  capacity: string;
  motorPower: string;
}

export const productApi = {
  getProducts: async (params?: {
    query?: string;
    category?: string;
    status?: string;
    visibility?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<PageResponse<ProductResponseDto>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<ProductResponseDto>>>('/products', { params });
    return response.data.data;
  },

  getProductById: async (id: string): Promise<ProductResponseDto> => {
    const response = await apiClient.get<ApiResponse<ProductResponseDto>>(`/products/${id}`);
    return response.data.data;
  },

  getProductBySku: async (sku: string): Promise<ProductResponseDto> => {
    const response = await apiClient.get<ApiResponse<ProductResponseDto>>(`/products/sku/${sku}`);
    return response.data.data;
  },

  createProduct: async (data: ProductCreateDto): Promise<ProductResponseDto> => {
    const response = await apiClient.post<ApiResponse<ProductResponseDto>>('/products', data);
    return response.data.data;
  },

  updateProduct: async (id: string, data: ProductUpdateDto): Promise<ProductResponseDto> => {
    const response = await apiClient.put<ApiResponse<ProductResponseDto>>(`/products/${id}`, data);
    return response.data.data;
  },

  updateStatus: async (id: string, status: 'ACTIVE' | 'INACTIVE'): Promise<ProductResponseDto> => {
    const response = await apiClient.patch<ApiResponse<ProductResponseDto>>(`/products/${id}/status`, null, {
      params: { status }
    });
    return response.data.data;
  },

  updateVisibility: async (id: string, visibility: 'PUBLIC' | 'HIDDEN'): Promise<ProductResponseDto> => {
    const response = await apiClient.patch<ApiResponse<ProductResponseDto>>(`/products/${id}/visibility`, null, {
      params: { visibility }
    });
    return response.data.data;
  },

  getProductSnapshot: async (id: string): Promise<ProductSnapshotDto> => {
    const response = await apiClient.get<ApiResponse<ProductSnapshotDto>>(`/products/${id}/snapshot`);
    return response.data.data;
  }
};
