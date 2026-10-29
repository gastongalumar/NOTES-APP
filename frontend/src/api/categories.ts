import { http } from './http';
import type { Category, CategoryInput } from '@/types/api';

export async function listCategories(): Promise<Category[]> {
  const { data } = await http.get<Category[]>('/categories');
  return data;
}

export async function createCategory(input: CategoryInput): Promise<Category> {
  const { data } = await http.post<Category>('/categories', input);
  return data;
}

export async function updateCategory(id: number, input: CategoryInput): Promise<Category> {
  const { data } = await http.put<Category>(`/categories/${id}`, input);
  return data;
}

export async function deleteCategory(id: number): Promise<void> {
  await http.delete(`/categories/${id}`);
}
