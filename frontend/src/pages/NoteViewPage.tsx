import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import type { Note } from '@/types/api';
import { deleteNote, getNote, setArchived } from '@/api/notes';
import { formatDate } from '@/lib/format';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/Button';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';
import { AttachmentPanel } from '@/components/attachments/AttachmentPanel';
import { ShareDialog } from '@/components/notes/ShareDialog';
import { exportNotePdf } from '@/lib/exportPdf';
import { baseURL } from '@/api/http';

export function NoteViewPage() {
  const { id } = useParams();
  const noteId = id ? Number(id) : null;
  const navigate = useNavigate();
  const { push } = useToast();
  const [note, setNote] = useState<Note | null>(null);
  const [error, setError] = useState('');
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [shareOpen, setShareOpen] = useState(false);

  useEffect(() => {
    if (!noteId) return;
    getNote(noteId)
      .then(setNote)
      .catch((caught) => setError((caught as { message?: string }).message ?? 'No se pudo cargar la nota'));
  }, [noteId]);

  const archive = async () => {
    if (!note) return;
    try {
      const updated = await setArchived(note.id, !note.archivada);
      setNote(updated);
      push(updated.archivada ? 'Nota archivada' : 'Nota restaurada', 'success');
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo archivar', 'error');
    }
  };

  const remove = async () => {
    if (!note) return;
    try {
      await deleteNote(note.id);
      push('Nota eliminada', 'success');
      navigate('/notes', { replace: true });
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo eliminar', 'error');
    }
  };

  if (error) return <div className="error-banner" role="alert">{error}</div>;
  if (!note) return <div className="empty-state">Cargando nota…</div>;

  return (
    <article className="note-view">
      <header className="page-header">
        <div>
          <p className="eyebrow">Nota</p>
          <h1>{note.titulo}</h1>
          <div className="note-meta">
            <span className="badge">{note.estado}</span>
            <span className="badge">Prioridad {note.prioridad}</span>
            {note.fechaVencimiento ? <span className="badge">Vence {formatDate(note.fechaVencimiento)}</span> : null}
            <span>Actualizada {formatDate(note.updatedAt)}</span>
          </div>
        </div>
        <div className="toolbar" style={{ marginBottom: 0 }}>
          <Link className="button button-primary" to={`/notes/${note.id}/edit`}>
            Editar
          </Link>
          <Button onClick={archive}>{note.archivada ? 'Restaurar' : 'Archivar'}</Button>
          <Button onClick={() => setShareOpen(true)}>Compartir</Button>
          <Button onClick={() => void exportNotePdf(note)}>PDF</Button>
          <a className="button" href={`${baseURL}/notes/${note.id}/export.md`}>Markdown</a>
          <Button variant="danger" onClick={() => setConfirmDelete(true)}>
            Eliminar
          </Button>
        </div>
      </header>

      <div className="note-meta">
        {note.categorias.map((category) => (
          <span className="badge badge-accent" key={`c-${category.id}`}>{category.nombre}</span>
        ))}
        {note.tags.map((tag) => (
          <span className="badge" key={`t-${tag.id}`}>#{tag.nombre}</span>
        ))}
      </div>

      <div className="note-content">
        {note.contenido ? (
          <ReactMarkdown remarkPlugins={[remarkGfm]}>{note.contenido}</ReactMarkdown>
        ) : (
          <p className="muted">Esta nota no tiene contenido.</p>
        )}
      </div>

      <AttachmentPanel noteId={note.id} />

      <ConfirmDialog
        open={confirmDelete}
        title="Eliminar nota"
        message="La nota se ocultará y se conservará 30 días para purga."
        confirmLabel="Eliminar"
        onConfirm={remove}
        onCancel={() => setConfirmDelete(false)}
      />
      <ShareDialog noteId={note.id} open={shareOpen} onClose={() => setShareOpen(false)} />
    </article>
  );
}
