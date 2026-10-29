import { http } from './http';

export interface TwoFactorSetup {
  secret: string;
  otpauthUri: string;
}

export async function setupTwoFactor(): Promise<TwoFactorSetup> {
  const { data } = await http.post<TwoFactorSetup>('/auth/2fa/setup');
  return data;
}

export async function verifyTwoFactor(code: string): Promise<string[]> {
  const { data } = await http.post<{ recoveryCodes: string[] }>('/auth/2fa/verify', { code });
  return data.recoveryCodes;
}
