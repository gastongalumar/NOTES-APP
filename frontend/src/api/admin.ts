import { http } from './http';
import type {
  AdminStats,
  AuditEntry,
  PageResponse,
  User,
} from '@/types/api';

export async function listUsers(query: {
  page?: number;
  size?: number;
  q?: string;
}): Promise<PageResponse<User>> {
  const { data } = await http.get<PageResponse<User>>('/admin/users', { params: query });
  return data;
}

export async function setUserEnabled(id: number, enabled: boolean): Promise<User> {
  const { data } = await http.patch<User>(`/admin/users/${id}/enabled`, { enabled });
  return data;
}

export async function setUserRoles(id: number, roles: string[]): Promise<User> {
  const { data } = await http.patch<User>(`/admin/users/${id}/roles`, { roles });
  return data;
}

export async function deleteUser(id: number): Promise<void> {
  await http.delete(`/admin/users/${id}`);
}

export async function listAudit(query: {
  page?: number;
  size?: number;
  usuarioId?: number;
  accion?: string;
  desde?: string;
  hasta?: string;
}): Promise<PageResponse<AuditEntry>> {
  const { data } = await http.get<PageResponse<AuditEntry>>('/admin/audit', { params: query });
  return data;
}

export async function stats(): Promise<AdminStats> {
  const { data } = await http.get<AdminStats>('/admin/stats');
  return data;
}
