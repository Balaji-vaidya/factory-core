import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
  correlationId?: string;
}

export interface FieldErrorDetail {
  field: string;
  message: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  code: string;
  message: string;
  fieldErrors: FieldErrorDetail[];
  correlationId?: string;
}

export const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // sends HttpOnly cookies for refresh token
});

// Request Interceptor: Attach JWT bearer token if stored
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('access_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Standardize error format
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
    if (error.response?.data) {
      const serverError = error.response.data;
      console.warn(`[API ERROR ${serverError.status || error.response.status}] [${serverError.code || 'UNKNOWN'}]: ${serverError.message}`);
    } else {
      console.error('[API NETWORK/SYSTEM ERROR]:', error.message);
    }
    return Promise.reject(error);
  }
);

// Health check service function
export const checkBackendHealth = async (): Promise<ApiResponse<{ status: string; application: string; system: string; brand: string }>> => {
  const response = await apiClient.get<ApiResponse<{ status: string; application: string; system: string; brand: string }>>('/health');
  return response.data;
};
