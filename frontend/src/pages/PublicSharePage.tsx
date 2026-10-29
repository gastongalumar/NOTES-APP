import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import type { Note } from '@/types/api';
import { getPublicShare } from '@/api/shares';
import { formatDate } from '@/lib/format';

export function PublicSharePage() {
  const { token } = useParams();
  const [note, setNote] = useState<Note | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!token) return;
    getPublicShare(token)
      .then(setNote)
      .catch((caught) => setError((caught as { message?: string }).message ?? 'Enlace no disponible'));
  }, [token]);

  return (
    <div className="auth-shell">
      <article className="auth-card note-view" style={{ maxWidth: 'var(--container-narrow)' }}>
        <p className="eyebrow">Nota compartida · solo lectura</p>
        {error ? <div className="error-banner" role="alert">{error}</div> : null}
        {!note && !error ? <p className="muted">Cargando…</p> : null}
        {note ? (
          <>
            <h1>{note.titulo}</h1>
            <div className="note-meta">
              <span className="badge">{note.estado}</span>
              <span>Actualizada {formatDate(note.updatedAt)}</span>
            </div>
            <div className="note-meta">
              {note.categorias.map((category) => (
                <span className="badge" key={category.id}>{category.nombre}</span>
              ))}
              {note.tags.map((tag) => (
                <span className="badge" key={tag.id}>#{tag.nombre}</span>
              ))}
            </div>
            <div className="note-content">
              {note.contenido ? <ReactMarkdown remarkPlugins={[remarkGfm]}>{note.contenido}</ReactMarkdown> : <p className="muted">Sin contenido.</p>}
            </div>
          </>
        ) : null}
      </article>
    </div>
  );
}
