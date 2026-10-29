import { http } from './http';
import type { Note, ShareResponse } from '@/types/api';

export async function createShare(noteId: number, expiresAt?: string): Promise<ShareResponse> {
  const { data } = await http.post<ShareResponse>(`/notes/${noteId}/shares`, { expiresAt });
  return data;
}

export async function getPublicShare(token: string): Promise<Note> {
  const { data } = await http.get<Note>(`/public/shares/${token}`);
  return data;
}
