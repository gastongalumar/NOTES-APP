import { Link } from 'react-router-dom';
import type { NoteSummary } from '@/types/api';
import { formatDate } from '@/lib/format';
import { Button } from '@/components/ui/Button';
import './NoteCard.css';

interface NoteCardProps {
  note: NoteSummary;
  onDelete?: (note: NoteSummary) => void;
  onArchive?: (note: NoteSummary) => void;
}

export function NoteCard({ note, onDelete, onArchive }: NoteCardProps) {
  return (
    <div className="note-card-shell">
      <Link className="note-card" to={`/notes/${note.id}`}>
        <article className="note-card__body">
          <div className="note-card__meta">
            <span className={`badge ${note.archivada ? '' : 'badge-accent'}`}>
              {note.archivada ? 'Archivada' : note.estado}
            </span>
            {note.prioridad === 1 ? <span className="badge badge-danger">Prioridad alta</span> : null}
            {note.fechaVencimiento ? (
              <span className="badge">Vence {formatDate(note.fechaVencimiento)}</span>
            ) : null}
          </div>

          <h2 className="note-card__title">{note.titulo}</h2>
          <p className="note-card__excerpt">{note.extracto || 'Sin contenido.'}</p>

          <div className="note-card__meta">
            {note.categorias.map((category) => (
              <span className="badge" key={`c-${category.id}`}>
                {category.nombre}
              </span>
            ))}
            {note.tags.map((tag) => (
              <span className="badge" key={`t-${tag.id}`}>
                #{tag.nombre}
              </span>
            ))}
          </div>

          <div className="note-card__meta">
            <span>{formatDate(note.updatedAt)}</span>
          </div>
        </article>
      </Link>

      <div className="note-card__actions">
        <Link
          className="button button-small"
          to={`/notes/${note.id}/edit`}
          onClick={(event) => event.stopPropagation()}
        >
          Editar
        </Link>
        {onArchive ? (
          <Button
            small
            onClick={(event) => {
              event.preventDefault();
              event.stopPropagation();
              onArchive(note);
            }}
          >
            {note.archivada ? 'Restaurar' : 'Archivar'}
          </Button>
        ) : null}
        {onDelete ? (
          <Button
            small
            variant="danger"
            onClick={(event) => {
              event.preventDefault();
              event.stopPropagation();
              onDelete(note);
            }}
          >
            Eliminar
          </Button>
        ) : null}
      </div>
    </div>
  );
}
