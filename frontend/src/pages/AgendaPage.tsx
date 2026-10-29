import { useEffect, useState } from 'react';
import type { NoteSummary } from '@/types/api';
import { today, upcoming } from '@/api/notes';
import { NoteCard } from '@/components/notes/NoteCard';
import { EmptyState } from '@/components/ui/EmptyState';

interface AgendaPageProps {
  kind: 'today' | 'upcoming';
}

export function AgendaPage({ kind }: AgendaPageProps) {
  const [notes, setNotes] = useState<NoteSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    setError('');
    const request = kind === 'today' ? today() : upcoming(7);
    request
      .then(setNotes)
      .catch((caught) => setError((caught as { message?: string }).message ?? 'No se pudo cargar la agenda'))
      .finally(() => setLoading(false));
  }, [kind]);

  const title = kind === 'today' ? 'Hoy' : 'Próximos 7 días';

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Agenda</p>
          <h1>{title}</h1>
        </div>
        <p className="subtle">Vencimientos y recordatorios de tus notas.</p>
      </header>

      {error ? <div className="error-banner" role="alert">{error}</div> : null}

      {loading ? (
        <div className="empty-state">Cargando agenda…</div>
      ) : notes.length === 0 ? (
        <EmptyState title={`No hay notas para ${kind === 'today' ? 'hoy' : 'los próximos 7 días'}`}>
          Asigná una fecha de vencimiento desde el editor de una nota.
        </EmptyState>
      ) : (
        <div className="card-grid">
          {notes.map((note) => (
            <NoteCard key={note.id} note={note} />
          ))}
        </div>
      )}
    </>
  );
}
