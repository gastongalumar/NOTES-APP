import type { User } from '@/types/api';

export function displayName(user: Pick<User, 'nombre' | 'username'>): string {
  return user.nombre?.trim() || user.username;
}

export function initials(user: Pick<User, 'nombre' | 'username'>): string {
  const name = displayName(user).trim();
  const parts = name.split(/\s+/).filter(Boolean);
  if (parts.length === 0) return '?';
  if (parts.length === 1) return parts[0]!.slice(0, 2).toUpperCase();
  return `${parts[0]![0]}${parts[1]![0]}`.toUpperCase();
}
