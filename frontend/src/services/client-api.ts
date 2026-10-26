import { apiClient, ApiResponse } from './api-client';

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ClientAddressDto {
  id?: string;
  siteName: string;
  addressLine1: string;
  city: string;
  state: string;
  pincode: string;
  contactPerson?: string;
  phone?: string;
  isDefault?: boolean;
}

export interface ClientContactDto {
  name: string;
  designation?: string;
  phone: string;
  email: string;
}

export interface ClientResponseDto {
  id: string;
  companyName: string;
  clientType: 'SEED_PRODUCER' | 'GOVT_CORPORATION' | 'AGRI_PROCESSOR' | 'EXPORTER' | 'PRIVATE_COMPANY';
  gstin?: string;
  pan?: string;
  state: string;
  primaryContact: ClientContactDto;
  billingContact?: ClientContactDto;
  creditDays: number;
  status: 'ACTIVE' | 'INACTIVE';
  createdBy?: string;
  createdAt: string;
  updatedAt: string;
  deliveryAddresses: ClientAddressDto[];
}

export interface ClientCreateDto {
  companyName: string;
  clientType?: 'SEED_PRODUCER' | 'GOVT_CORPORATION' | 'AGRI_PROCESSOR' | 'EXPORTER' | 'PRIVATE_COMPANY';
  gstin?: string;
  pan?: string;
  state: string;
  primaryContactName: string;
  primaryContactDesignation?: string;
  primaryContactPhone: string;
  primaryContactEmail: string;
  billingContactName?: string;
  billingContactDesignation?: string;
  billingContactPhone?: string;
  billingContactEmail?: string;
  creditDays?: number;
  initialAddress?: ClientAddressDto;
}

export interface ClientUpdateDto {
  companyName: string;
  clientType?: 'SEED_PRODUCER' | 'GOVT_CORPORATION' | 'AGRI_PROCESSOR' | 'EXPORTER' | 'PRIVATE_COMPANY';
  gstin?: string;
  pan?: string;
  state: string;
  primaryContactName: string;
  primaryContactDesignation?: string;
  primaryContactPhone: string;
  primaryContactEmail: string;
  billingContactName?: string;
  billingContactDesignation?: string;
  billingContactPhone?: string;
  billingContactEmail?: string;
  creditDays?: number;
}

export interface ClientSnapshotDto {
  clientId: string;
  companyName: string;
  gstin?: string;
  pan?: string;
  state: string;
  primaryContactName: string;
  primaryContactPhone: string;
  primaryContactEmail: string;
  billingAddressLine: string;
  deliveryAddressLine: string;
  creditDays: number;
}

export const clientApi = {
  getClients: async (params?: {
    query?: string;
    state?: string;
    status?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<PageResponse<ClientResponseDto>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<ClientResponseDto>>>('/clients', { params });
    return response.data.data;
  },

  getClientById: async (id: string): Promise<ClientResponseDto> => {
    const response = await apiClient.get<ApiResponse<ClientResponseDto>>(`/clients/${id}`);
    return response.data.data;
  },

  createClient: async (data: ClientCreateDto): Promise<ClientResponseDto> => {
    const response = await apiClient.post<ApiResponse<ClientResponseDto>>('/clients', data);
    return response.data.data;
  },

  updateClient: async (id: string, data: ClientUpdateDto): Promise<ClientResponseDto> => {
    const response = await apiClient.put<ApiResponse<ClientResponseDto>>(`/clients/${id}`, data);
    return response.data.data;
  },

  updateStatus: async (id: string, status: 'ACTIVE' | 'INACTIVE'): Promise<ClientResponseDto> => {
    const response = await apiClient.patch<ApiResponse<ClientResponseDto>>(`/clients/${id}/status`, null, {
      params: { status }
    });
    return response.data.data;
  },

  addDeliveryAddress: async (clientId: string, address: ClientAddressDto): Promise<ClientResponseDto> => {
    const response = await apiClient.post<ApiResponse<ClientResponseDto>>(`/clients/${clientId}/addresses`, address);
    return response.data.data;
  },

  setDefaultAddress: async (clientId: string, addressId: string): Promise<ClientResponseDto> => {
    const response = await apiClient.put<ApiResponse<ClientResponseDto>>(`/clients/${clientId}/addresses/${addressId}/default`);
    return response.data.data;
  },

  getClientSnapshot: async (id: string, deliveryAddressId?: string): Promise<ClientSnapshotDto> => {
    const response = await apiClient.get<ApiResponse<ClientSnapshotDto>>(`/clients/${id}/snapshot`, {
      params: { deliveryAddressId }
    });
    return response.data.data;
  }
};
