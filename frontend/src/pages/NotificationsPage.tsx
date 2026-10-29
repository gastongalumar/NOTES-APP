import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import type { NotificationEntry } from '@/types/api';
import { listNotifications, markRead } from '@/api/notifications';
import { formatDateTime } from '@/lib/format';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/Button';

function noteId(payload: string | null): number | null {
  if (!payload) return null;
  try {
    const parsed = JSON.parse(payload) as { notaId?: number };
    return typeof parsed.notaId === 'number' ? parsed.notaId : null;
  } catch {
    return null;
  }
}

export function NotificationsPage() {
  const { push } = useToast();
  const [entries, setEntries] = useState<NotificationEntry[]>([]);
  const [error, setError] = useState('');

  const load = () => {
    listNotifications({ size: 50 })
      .then((response) => setEntries(response.content))
      .catch((caught) => setError((caught as { message?: string }).message ?? 'No se pudieron cargar las notificaciones'));
  };

  useEffect(load, []);

  const read = async (id: number) => {
    try {
      await markRead(id);
      load();
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo marcar como leída', 'error');
    }
  };

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Recordatorios</p>
          <h1>Notificaciones</h1>
        </div>
      </header>
      {error ? <div className="error-banner" role="alert">{error}</div> : null}
      {entries.length === 0 ? (
        <div className="empty-state">No tenés notificaciones.</div>
      ) : (
        <div className="card-grid">
          {entries.map((entry) => {
            const target = noteId(entry.payload);
            return (
              <article className="card" key={entry.id}>
                <div className="note-meta">
                  <span className={`badge ${entry.leidaAt ? '' : 'badge-accent'}`}>
                    {entry.leidaAt ? 'Leída' : 'Nueva'}
                  </span>
                  <span>{formatDateTime(entry.createdAt)}</span>
                </div>
                <h2>{entry.tipo === 'RECORDATORIO' ? 'Recordatorio de nota' : entry.tipo}</h2>
                {target ? <Link to={`/notes/${target}`}>Abrir nota</Link> : null}
                {!entry.leidaAt ? (
                  <Button small onClick={() => read(entry.id)}>Marcar leída</Button>
                ) : null}
              </article>
            );
          })}
        </div>
      )}
    </>
  );
}
