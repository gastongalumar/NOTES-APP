import axios, { type AxiosError } from 'axios';
import type { ApiError } from '@/types/api';

interface ProblemBody {
  status?: number;
  code?: string;
  detail?: string;
  title?: string;
  errors?: Record<string, string>;
}

export function normalizeError(error: unknown): ApiError {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ProblemBody>;
    const body = axiosError.response?.data;
    const status = axiosError.response?.status ?? 0;
    return {
      status,
      code: body?.code ?? (status ? `HTTP_${status}` : 'NETWORK_ERROR'),
      message:
        body?.detail ??
        body?.title ??
        (status === 0
          ? 'No se pudo conectar con el servidor'
          : 'Ocurrió un error inesperado'),
      fieldErrors: body?.errors,
    };
  }
  if (error instanceof Error) {
    return { status: 0, code: 'UNKNOWN', message: error.message };
  }
  return { status: 0, code: 'UNKNOWN', message: 'Ocurrió un error inesperado' };
}
