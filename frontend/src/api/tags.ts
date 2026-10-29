import { http } from './http';
import type { Tag } from '@/types/api';

export async function listTags(): Promise<Tag[]> {
  const { data } = await http.get<Tag[]>('/tags');
  return data;
}

export async function createTag(nombre: string): Promise<Tag> {
  const { data } = await http.post<Tag>('/tags', { nombre });
  return data;
}

export async function updateTag(id: number, nombre: string): Promise<Tag> {
  const { data } = await http.put<Tag>(`/tags/${id}`, { nombre });
  return data;
}

export async function deleteTag(id: number): Promise<void> {
  await http.delete(`/tags/${id}`);
}
