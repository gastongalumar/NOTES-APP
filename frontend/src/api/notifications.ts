import { http } from './http';
import type { NotificationEntry, PageResponse } from '@/types/api';

export async function listNotifications(query: {
  unread?: boolean;
  page?: number;
  size?: number;
}): Promise<PageResponse<NotificationEntry>> {
  const { data } = await http.get<PageResponse<NotificationEntry>>('/notifications', {
    params: query,
  });
  return data;
}

export async function unreadCount(): Promise<number> {
  const { data } = await http.get<{ count: number }>('/notifications/unread-count');
  return data.count;
}

export async function markRead(id: number): Promise<void> {
  await http.patch(`/notifications/${id}/read`);
}
