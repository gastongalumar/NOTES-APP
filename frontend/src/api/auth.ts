import { http, raw } from './http';
import { normalizeError } from './errors';
import type { AuthResponse, User } from '@/types/api';

export interface LoginInput {
  username: string;
  password: string;
  totp?: string;
}

export interface RegisterInput {
  username: string;
  email: string;
  password: string;
  nombre?: string;
}

export async function login(input: LoginInput): Promise<AuthResponse> {
  try {
    const { data } = await raw.post<AuthResponse>('/auth/login', input);
    return data;
  } catch (error) {
    throw normalizeError(error);
  }
}

export async function register(input: RegisterInput): Promise<User> {
  try {
    const { data } = await raw.post<User>('/auth/register', input);
    return data;
  } catch (error) {
    throw normalizeError(error);
  }
}

export async function logout(): Promise<void> {
  await raw.post('/auth/logout');
}

export async function me(): Promise<User> {
  const { data } = await http.get<User>('/auth/me');
  return data;
}

export async function updateProfile(input: {
  nombre?: string;
  email?: string;
}): Promise<User> {
  const { data } = await http.patch<User>('/users/me', input);
  return data;
}

export async function changePassword(input: {
  currentPassword: string;
  newPassword: string;
}): Promise<void> {
  await http.put('/users/me/password', input);
}

export async function deleteAccount(password: string): Promise<void> {
  await http.delete('/users/me', { data: { password } });
}

export async function forgotPassword(email: string): Promise<void> {
  try {
    await raw.post('/auth/password/forgot', { email });
  } catch (error) {
    throw normalizeError(error);
  }
}

export async function resetPassword(token: string, newPassword: string): Promise<void> {
  try {
    await raw.post('/auth/password/reset', { token, newPassword });
  } catch (error) {
    throw normalizeError(error);
  }
}

export async function requestEmailVerification(): Promise<void> {
  await http.post('/auth/email/request');
}

export async function resendEmailVerification(email: string): Promise<void> {
  try {
    await raw.post('/auth/email/resend', { email });
  } catch (error) {
    throw normalizeError(error);
  }
}

export async function verifyEmail(token: string): Promise<void> {
  try {
    await raw.post('/auth/email/verify', { token });
  } catch (error) {
    throw normalizeError(error);
  }
}
