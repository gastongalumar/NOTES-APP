import { http, baseURL } from './http';
import type { Attachment } from '@/types/api';

export async function listAttachments(noteId: number): Promise<Attachment[]> {
  const { data } = await http.get<Attachment[]>(`/notes/${noteId}/attachments`);
  return data;
}

export async function uploadAttachment(noteId: number, file: File): Promise<Attachment> {
  const form = new FormData();
  form.append('file', file);
  const { data } = await http.post<Attachment>(`/notes/${noteId}/attachments`, form);
  return data;
}

export async function deleteAttachment(id: number): Promise<void> {
  await http.delete(`/attachments/${id}`);
}

export function attachmentDownloadUrl(id: number): string {
  return `${baseURL}/attachments/${id}/download`;
}
