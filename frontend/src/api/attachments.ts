import { http } from './http';
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

export async function fetchAttachmentBlob(id: number, signal?: AbortSignal): Promise<Blob> {
  const response = await http.get<Blob>(`/attachments/${id}/download`, {
    responseType: 'blob',
    signal,
  });
  return response.data;
}

export async function downloadAttachment(id: number, filename: string): Promise<void> {
  const blob = await fetchAttachmentBlob(id);
  const url = URL.createObjectURL(blob);
  try {
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.rel = 'noopener';
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
  } finally {
    // Dar tiempo al navegador a iniciar la descarga antes de liberar.
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
}
