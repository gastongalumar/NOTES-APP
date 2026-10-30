import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import type { NoteSummary } from '@/types/api';
import { listNotes, today } from '@/api/notes';
import { unreadCount } from '@/api/notifications';
import { useAuth } from '@/context/AuthContext';
import { displayName } from '@/lib/user';
import { Icon } from '@/components/icons/Icon';
import { NoteCard } from '@/components/notes/NoteCard';
import { EmptyState } from '@/components/ui/EmptyState';

interface Overview {
  activeCount: number;
  archivedCount: number;
  unread: number;
}

export function HomePage() {
  const { user } = useAuth();
  const [todayNotes, setTodayNotes] = useState<NoteSummary[]>([]);
  const [todayCount, setTodayCount] = useState(0);
  const [recent, setRecent] = useState<NoteSummary[]>([]);
  const [overview, setOverview] = useState<Overview>({ activeCount: 0, archivedCount: 0, unread: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    Promise.all([
      today(),
      listNotes({ page: 0, size: 4 }),
      listNotes({ page: 0, size: 1 }),
      listNotes({ page: 0, size: 1, archived: true }),
      unreadCount(),
    ])
      .then(([todayResult, recentResult, activeResult, archivedResult, unread]) => {
        if (!active) return;
        setTodayNotes(todayResult.slice(0, 4));
        setTodayCount(todayResult.length);
        setRecent(recentResult.content);
        setOverview({
          activeCount: activeResult.totalElements,
          archivedCount: archivedResult.totalElements,
          unread,
        });
      })
      .catch((caught) => {
        if (active) setError((caught as { message?: string }).message ?? 'No se pudo cargar el inicio');
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const firstName = user ? displayName(user).split(/\s+/)[0] : '';

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Tu espacio</p>
          <h1>Hola, {firstName}</h1>
        </div>
        <Link className="button button-primary" to="/notes/new">
          <Icon name="plus" />
          Nueva nota
        </Link>
      </header>

      {error ? (
        <div className="error-banner" role="alert">
          {error}
        </div>
      ) : null}

      {loading ? (
        <div className="empty-state">Cargando tu espacio…</div>
      ) : (
        <>
          <div className="stat-grid">
            <Link className="stat-card" to="/notes">
              <span className="stat-card__value">{overview.activeCount}</span>
              <span className="stat-card__label">Notas activas</span>
            </Link>
            <Link className="stat-card" to="/today">
              <span className="stat-card__value">{todayCount}</span>
              <span className="stat-card__label">Para hoy</span>
            </Link>
            <Link className="stat-card" to="/archive">
              <span className="stat-card__value">{overview.archivedCount}</span>
              <span className="stat-card__label">Archivadas</span>
            </Link>
            <Link className="stat-card" to="/notifications">
              <span className="stat-card__value">{overview.unread}</span>
              <span className="stat-card__label">Sin leer</span>
            </Link>
          </div>

          <section className="home-section" aria-labelledby="home-today">
            <div className="section-head">
              <h2 id="home-today">Para hoy</h2>
              <Link to="/today">Ver agenda</Link>
            </div>
            {todayNotes.length === 0 ? (
              <p className="home-empty">No tenés vencimientos para hoy.</p>
            ) : (
              <div className="card-grid">
                {todayNotes.map((note) => (
                  <NoteCard key={note.id} note={note} showEdit={false} />
                ))}
              </div>
            )}
          </section>

          <section className="home-section" aria-labelledby="home-recent">
            <div className="section-head">
              <h2 id="home-recent">Recientes</h2>
              <Link to="/notes">Ver todas</Link>
            </div>
            {recent.length === 0 ? (
              <EmptyState title="Todavía no hay notas">
                Creá tu primera nota para empezar.
              </EmptyState>
            ) : (
              <div className="card-grid">
                {recent.map((note) => (
                  <NoteCard key={note.id} note={note} showEdit={false} />
                ))}
              </div>
            )}
          </section>
        </>
      )}
    </>
  );
}
