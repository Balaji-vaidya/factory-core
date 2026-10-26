import { apiClient, ApiResponse } from './api-client';

export interface InventoryValuationItem {
  officeId: string;
  officeName: string;
  productId: string;
  sku: string;
  productName: string;
  category: string;
  onHandQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  basePriceINR: number;
  totalValuationINR: number;
}

export interface POFulfillmentItem {
  poId: string;
  poNumber: string;
  clientName: string;
  officeName: string;
  poDate: string;
  expectedDeliveryDate: string;
  status: string;
  priority: string;
  totalOrderedQuantity: number;
  totalDeliveredQuantity: number;
  fulfillmentPercent: number;
  totalAmountINR: number;
}

export interface DispatchReportItem {
  dcId: string;
  dcNumber: string;
  poNumber: string;
  clientName: string;
  officeName: string;
  challanDate: string;
  dispatchedAt?: string;
  deliveredAt?: string;
  status: string;
  vehicleNumber?: string;
  transporterName?: string;
  totalQuantity: number;
}

export const reportApi = {
  getInventoryValuation: async (officeId?: string): Promise<ApiResponse<InventoryValuationItem[]>> => {
    const res = await apiClient.get<ApiResponse<InventoryValuationItem[]>>('/reports/inventory-valuation', {
      params: { officeId },
    });
    return res.data;
  },

  downloadInventoryValuationCsv: async (officeId?: string): Promise<Blob> => {
    const res = await apiClient.get('/reports/inventory-valuation/export', {
      params: { officeId },
      responseType: 'blob',
    });
    return res.data;
  },

  getPOFulfillment: async (
    officeId?: string,
    startDate?: string,
    endDate?: string
  ): Promise<ApiResponse<POFulfillmentItem[]>> => {
    const res = await apiClient.get<ApiResponse<POFulfillmentItem[]>>('/reports/po-fulfillment', {
      params: { officeId, startDate, endDate },
    });
    return res.data;
  },

  downloadPOFulfillmentCsv: async (
    officeId?: string,
    startDate?: string,
    endDate?: string
  ): Promise<Blob> => {
    const res = await apiClient.get('/reports/po-fulfillment/export', {
      params: { officeId, startDate, endDate },
      responseType: 'blob',
    });
    return res.data;
  },

  getDispatches: async (startDate?: string, endDate?: string): Promise<ApiResponse<DispatchReportItem[]>> => {
    const res = await apiClient.get<ApiResponse<DispatchReportItem[]>>('/reports/dispatches', {
      params: { startDate, endDate },
    });
    return res.data;
  },
};
