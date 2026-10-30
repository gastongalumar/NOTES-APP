import { http } from './http';

export interface TwoFactorSetup {
  secret: string;
  otpauthUri: string;
}

export interface TwoFactorStatus {
  enabled: boolean;
  recoveryCodesRemaining: number;
}

export async function statusTwoFactor(): Promise<TwoFactorStatus> {
  const { data } = await http.get<TwoFactorStatus>('/auth/2fa/status');
  return data;
}

export async function setupTwoFactor(): Promise<TwoFactorSetup> {
  const { data } = await http.post<TwoFactorSetup>('/auth/2fa/setup');
  return data;
}

export async function verifyTwoFactor(code: string): Promise<string[]> {
  const { data } = await http.post<{ recoveryCodes: string[] }>('/auth/2fa/verify', { code });
  return data.recoveryCodes;
}

export async function disableTwoFactor(password: string): Promise<void> {
  await http.post('/auth/2fa/disable', { password });
}
