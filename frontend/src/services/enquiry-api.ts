import { apiClient, ApiResponse } from './api-client';
import { PageResponse } from './notification-api';
import { ClientResponseDto } from './client-api';

export type EnquiryStatus = 'NEW' | 'CONTACTED' | 'QUOTATION_SENT' | 'CONVERTED_TO_CLIENT' | 'CLOSED';

export interface WebsiteEnquiryItem {
  id: string;
  name: string;
  email: string;
  phone: string;
  companyName?: string;
  state?: string;
  message: string;
  interestedProducts?: string;
  source: string;
  status: EnquiryStatus;
  internalNotes?: string;
  convertedClientId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface EnquirySubmitPayload {
  name: string;
  email: string;
  phone: string;
  companyName?: string;
  state?: string;
  message: string;
  interestedProducts?: string;
  source?: string;
}

export interface EnquiryStatusUpdatePayload {
  status: EnquiryStatus;
  internalNotes?: string;
}

export const enquiryApi = {
  submitPublic: async (payload: EnquirySubmitPayload): Promise<ApiResponse<WebsiteEnquiryItem>> => {
    const res = await apiClient.post<ApiResponse<WebsiteEnquiryItem>>('/public/enquiries', payload);
    return res.data;
  },

  searchEnquiries: async (
    query?: string,
    status?: EnquiryStatus,
    page = 0,
    size = 20
  ): Promise<ApiResponse<PageResponse<WebsiteEnquiryItem>>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<WebsiteEnquiryItem>>>('/enquiries', {
      params: { query, status, page, size },
    });
    return res.data;
  },

  getById: async (id: string): Promise<ApiResponse<WebsiteEnquiryItem>> => {
    const res = await apiClient.get<ApiResponse<WebsiteEnquiryItem>>(`/enquiries/${id}`);
    return res.data;
  },

  updateStatus: async (id: string, payload: EnquiryStatusUpdatePayload): Promise<ApiResponse<WebsiteEnquiryItem>> => {
    const res = await apiClient.put<ApiResponse<WebsiteEnquiryItem>>(`/enquiries/${id}/status`, payload);
    return res.data;
  },

  convertToClient: async (id: string): Promise<ApiResponse<ClientResponseDto>> => {
    const res = await apiClient.post<ApiResponse<ClientResponseDto>>(`/enquiries/${id}/convert-to-client`);
    return res.data;
  },
};
