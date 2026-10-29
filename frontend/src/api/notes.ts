import { http } from './http';
import type {
  Note,
  NoteInput,
  NoteSummary,
  NoteUpdateInput,
  PageResponse,
} from '@/types/api';

export interface NoteQuery {
  page?: number;
  size?: number;
  sort?: string;
  archived?: boolean;
  q?: string;
  categoryId?: number;
  tagId?: number;
}

export async function listNotes(query: NoteQuery): Promise<PageResponse<NoteSummary>> {
  const { data } = await http.get<PageResponse<NoteSummary>>('/notes', { params: query });
  return data;
}

export async function getNote(id: number): Promise<Note> {
  const { data } = await http.get<Note>(`/notes/${id}`);
  return data;
}

export async function createNote(input: NoteInput): Promise<Note> {
  const { data } = await http.post<Note>('/notes', input);
  return data;
}

export async function updateNote(id: number, input: NoteUpdateInput): Promise<Note> {
  const { data } = await http.put<Note>(`/notes/${id}`, input);
  return data;
}

export async function deleteNote(id: number): Promise<void> {
  await http.delete(`/notes/${id}`);
}

export async function setArchived(id: number, archived: boolean): Promise<Note> {
  const { data } = await http.patch<Note>(`/notes/${id}/${archived ? 'archive' : 'unarchive'}`);
  return data;
}

export async function agenda(from: string, to: string): Promise<NoteSummary[]> {
  const { data } = await http.get<NoteSummary[]>('/notes/agenda', { params: { from, to } });
  return data;
}

export async function today(): Promise<NoteSummary[]> {
  const { data } = await http.get<NoteSummary[]>('/notes/today');
  return data;
}

export async function upcoming(days = 7): Promise<NoteSummary[]> {
  const { data } = await http.get<NoteSummary[]>('/notes/upcoming', { params: { days } });
  return data;
}
