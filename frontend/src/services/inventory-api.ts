import { apiClient, ApiResponse } from './api-client';
import { PageResponse } from './client-api';

export type StockTransactionType =
  | 'INITIAL_STOCK'
  | 'INWARD'
  | 'PO_RESERVATION'
  | 'PO_UNRESERVATION'
  | 'DC_DISPATCH'
  | 'MANUAL_ADJUSTMENT'
  | 'RETURN_INWARD';

export interface InventoryResponseDto {
  id: string;
  officeId: string;
  officeName: string;
  officeState: string;
  productId: string;
  sku: string;
  productName: string;
  unit: string;
  onHandQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  reorderThreshold: number;
  lowStock: boolean;
  lastAdjustedAt?: string;
  updatedAt: string;
}

export interface StockLedgerResponseDto {
  id: string;
  inventoryId: string;
  officeId: string;
  officeName: string;
  productId: string;
  sku: string;
  productName: string;
  transactionType: StockTransactionType;
  quantityDelta: number;
  onHandBefore: number;
  onHandAfter: number;
  reservedBefore: number;
  reservedAfter: number;
  availableBefore: number;
  availableAfter: number;
  referenceId?: string;
  referenceType?: string;
  notes?: string;
  createdBy: string;
  createdAt: string;
}

export interface StockInwardDto {
  officeId: string;
  productId: string;
  quantity: number;
  reorderThreshold?: number;
  notes?: string;
}

export interface StockAdjustmentDto {
  officeId: string;
  productId: string;
  newOnHandQuantity: number;
  reason: string;
}

export const inventoryApi = {
  getInventory: async (params?: {
    officeId?: string;
    productId?: string;
    query?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<PageResponse<InventoryResponseDto>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<InventoryResponseDto>>>('/inventory', { params });
    return response.data.data;
  },

  getInventoryById: async (id: string): Promise<InventoryResponseDto> => {
    const response = await apiClient.get<ApiResponse<InventoryResponseDto>>(`/inventory/${id}`);
    return response.data.data;
  },

  recordInward: async (data: StockInwardDto): Promise<InventoryResponseDto> => {
    const response = await apiClient.post<ApiResponse<InventoryResponseDto>>('/inventory/inward', data);
    return response.data.data;
  },

  adjustStock: async (data: StockAdjustmentDto): Promise<InventoryResponseDto> => {
    const response = await apiClient.post<ApiResponse<InventoryResponseDto>>('/inventory/adjust', data);
    return response.data.data;
  },

  getLedger: async (params?: {
    officeId?: string;
    productId?: string;
    transactionType?: StockTransactionType;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<PageResponse<StockLedgerResponseDto>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<StockLedgerResponseDto>>>('/inventory/ledger', { params });
    return response.data.data;
  }
};
