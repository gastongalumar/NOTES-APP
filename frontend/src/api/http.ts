import axios, {
  type AxiosError,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from 'axios';
import type { AuthResponse } from '@/types/api';
import { normalizeError } from './errors';
import { broadcastAuth } from './authChannel';
import {
  clearAccessToken,
  getAccessToken,
  setAccessToken,
} from '@/auth/tokenStore';

const baseURL = import.meta.env.VITE_API_URL ?? '/api/v1';

export const raw = axios.create({
  baseURL,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

export const http = axios.create({
  baseURL,
  withCredentials: true,
});

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let refreshPromise: Promise<AuthResponse> | null = null;

export async function refreshSession(): Promise<AuthResponse> {
  if (!refreshPromise) {
    refreshPromise = raw
      .post<AuthResponse>('/auth/refresh')
      .then((response) => {
        setAccessToken(response.data.accessToken);
        broadcastAuth({ type: 'token', accessToken: response.data.accessToken });
        return response.data;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

function isAuthEndpoint(url: string | undefined): boolean {
  return Boolean(url && url.includes('/auth/'));
}

http.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined;
    if (
      error.response?.status !== 401 ||
      !original ||
      original._retry ||
      isAuthEndpoint(original.url)
    ) {
      return Promise.reject(normalizeError(error));
    }
    original._retry = true;
    try {
      await refreshSession();
      return await http(original as AxiosRequestConfig);
    } catch (refreshError) {
      clearAccessToken();
      broadcastAuth({ type: 'logout' });
      window.dispatchEvent(new Event('notasapp:logout'));
      return Promise.reject(normalizeError(refreshError));
    }
  },
);

export { baseURL };
