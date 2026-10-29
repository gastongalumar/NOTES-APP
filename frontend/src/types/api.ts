export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ApiError {
  status: number;
  code: string;
  message: string;
  fieldErrors?: Record<string, string>;
}

export interface User {
  id: number;
  username: string;
  email: string;
  nombre: string | null;
  enabled: boolean;
  emailVerified: boolean;
  roles: string[];
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface NamedRef {
  id: number;
  nombre: string;
  color: string | null;
}

export type NoteState = 'PENDIENTE' | 'EN_CURSO' | 'HECHA';

export interface NoteSummary {
  id: number;
  titulo: string;
  extracto: string;
  archivada: boolean;
  fechaVencimiento: string | null;
  prioridad: number;
  estado: NoteState;
  categorias: NamedRef[];
  tags: NamedRef[];
  updatedAt: string;
}

export interface Note extends NoteSummary {
  contenido: string | null;
  version: number;
  createdAt: string;
}

export interface NoteInput {
  titulo: string;
  contenido: string;
  categoriaIds: number[];
  tagIds: number[];
  fechaVencimiento: string | null;
  prioridad: number;
  estado: NoteState;
}

export interface NoteUpdateInput extends NoteInput {
  version: number;
}

export interface Category {
  id: number;
  nombre: string;
  color: string;
  descripcion: string | null;
  cantidadNotas: number;
}

export interface CategoryInput {
  nombre: string;
  color: string;
  descripcion: string;
}

export interface Tag {
  id: number;
  nombre: string;
}

export interface Attachment {
  id: number;
  notaId: number;
  nombreOriginal: string;
  mimeType: string;
  tamanoBytes: number;
  checksumSha256: string;
  estado: string;
  scanStatus: string | null;
  createdAt: string;
  downloadUrl: string;
}

export interface AuditEntry {
  id: number;
  usuarioId: number | null;
  username: string | null;
  accion: string;
  entidad: string | null;
  entidadId: number | null;
  ip: string | null;
  userAgent: string | null;
  createdAt: string;
}

export interface ShareResponse {
  url: string;
  expiresAt: string;
}

export interface NotificationEntry {
  id: number;
  tipo: string;
  payload: string | null;
  leidaAt: string | null;
  createdAt: string;
}

export interface AdminStats {
  usuarios: number;
  notas: number;
  notasArchivadas: number;
  adjuntos: number;
  loginsExitosos: number;
  loginsFallidos: number;
}
