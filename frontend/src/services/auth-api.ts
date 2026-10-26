import { apiClient, ApiResponse } from './api-client';

export interface UserProfile {
  id: string;
  email: string;
  fullName: string;
  phone: string;
  role: 'OWNER' | 'ADMIN' | 'MAIN_OFFICE' | 'OUTER_STATE_OFFICE' | 'GODOWN_MANAGER';
  officeId: string;
  officeName: string;
  state: string;
  allowedOffices: string[];
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInMs: number;
  user: UserProfile;
}

export const authApi = {
  login: async (email: string, password: string): Promise<TokenResponse> => {
    const response = await apiClient.post<ApiResponse<TokenResponse>>('/auth/login', { email, password });
    if (response.data.success && response.data.data) {
      const data = response.data.data;
      localStorage.setItem('access_token', data.accessToken);
      localStorage.setItem('refresh_token', data.refreshToken);
      localStorage.setItem('user_profile', JSON.stringify(data.user));
      return data;
    }
    throw new Error(response.data.message || 'Login failed');
  },

  logout: async (): Promise<void> => {
    try {
      await apiClient.post('/auth/logout');
    } finally {
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('user_profile');
    }
  },

  getCurrentUser: async (): Promise<UserProfile> => {
    const response = await apiClient.get<ApiResponse<UserProfile>>('/auth/me');
    return response.data.data;
  },

  refreshToken: async (): Promise<TokenResponse> => {
    const refreshToken = localStorage.getItem('refresh_token');
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }
    const response = await apiClient.post<ApiResponse<TokenResponse>>('/auth/refresh', { refreshToken });
    const data = response.data.data;
    localStorage.setItem('access_token', data.accessToken);
    localStorage.setItem('refresh_token', data.refreshToken);
    localStorage.setItem('user_profile', JSON.stringify(data.user));
    return data;
  },
};
