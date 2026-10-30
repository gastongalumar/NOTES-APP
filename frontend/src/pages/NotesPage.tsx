import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import type { Category, NoteSummary, Tag } from '@/types/api';
import { listNotes, deleteNote, setArchived } from '@/api/notes';
import { listCategories } from '@/api/categories';
import { listTags } from '@/api/tags';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/Button';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';
import { EmptyState } from '@/components/ui/EmptyState';
import { Pagination } from '@/components/ui/Pagination';
import { NoteCard } from '@/components/notes/NoteCard';

interface NotesPageProps {
  archived?: boolean;
  title?: string;
}

export function NotesPage({ archived, title = 'Tus notas' }: NotesPageProps) {
  const { push } = useToast();
  const [notes, setNotes] = useState<NoteSummary[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [q, setQ] = useState('');
  const [query, setQuery] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [tagId, setTagId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [pendingDelete, setPendingDelete] = useState<NoteSummary | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = await listNotes({
        page,
        size: 12,
        archived,
        q: query || undefined,
        categoryId: categoryId ? Number(categoryId) : undefined,
        tagId: tagId ? Number(tagId) : undefined,
      });
      setNotes(response.content);
      setTotalPages(response.totalPages);
    } catch (caught) {
      setError((caught as { message?: string }).message ?? 'No se pudieron cargar las notas');
    } finally {
      setLoading(false);
    }
  }, [page, archived, query, categoryId, tagId]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    Promise.all([listCategories(), listTags()])
      .then(([nextCategories, nextTags]) => {
        setCategories(nextCategories);
        setTags(nextTags);
      })
      .catch(() => {
        push('No se pudieron cargar categorías o tags', 'error');
      });
  }, [push]);

  const search = (event: FormEvent) => {
    event.preventDefault();
    setPage(0);
    setQuery(q.trim());
  };

  const confirmDelete = async () => {
    if (!pendingDelete) return;
    try {
      await deleteNote(pendingDelete.id);
      setPendingDelete(null);
      push('Nota eliminada', 'success');
      await load();
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo eliminar', 'error');
    }
  };

  const toggleArchive = async (note: NoteSummary) => {
    try {
      await setArchived(note.id, !note.archivada);
      push(note.archivada ? 'Nota restaurada' : 'Nota archivada', 'success');
      await load();
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo archivar', 'error');
    }
  };

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">{archived ? 'Archivo' : 'Biblioteca'}</p>
          <h1>{title}</h1>
        </div>
        <Link className="button button-primary" to="/notes/new">
          Nueva nota
        </Link>
      </header>

      <form className="toolbar" onSubmit={search} role="search">
        <label className="sr-only" htmlFor="note-search">
          Buscar notas
        </label>
        <input
          id="note-search"
          className="input grow"
          placeholder="Buscar por título o contenido…"
          value={q}
          onChange={(event) => setQ(event.target.value)}
        />
        <div className="toolbar__filters">
          <select
            className="select"
            aria-label="Filtrar por categoría"
            value={categoryId}
            onChange={(event) => {
              setPage(0);
              setCategoryId(event.target.value);
            }}
          >
            <option value="">Todas las categorías</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.nombre}
              </option>
            ))}
          </select>
          <select
            className="select"
            aria-label="Filtrar por tag"
            value={tagId}
            onChange={(event) => {
              setPage(0);
              setTagId(event.target.value);
            }}
          >
            <option value="">Todos los tags</option>
            {tags.map((tag) => (
              <option key={tag.id} value={tag.id}>
                {tag.nombre}
              </option>
            ))}
          </select>
        </div>
        <Button type="submit">Buscar</Button>
      </form>

      {error ? <div className="error-banner" role="alert">{error}</div> : null}

      {loading ? (
        <div className="empty-state">Cargando notas…</div>
      ) : notes.length === 0 ? (
        <EmptyState title={archived ? 'No hay notas archivadas' : 'Todavía no hay notas'}>
          {archived ? 'Archivá una nota para verla acá.' : 'Creá tu primera nota para empezar.'}
        </EmptyState>
      ) : (
        <div className="card-grid">
          {notes.map((note) => (
            <NoteCard
              key={note.id}
              note={note}
              onDelete={setPendingDelete}
              onArchive={toggleArchive}
            />
          ))}
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />

      <ConfirmDialog
        open={Boolean(pendingDelete)}
        title="Eliminar nota"
        message={`¿Eliminar “${pendingDelete?.titulo ?? ''}”? La nota se ocultará y se conservará 30 días para purga.`}
        confirmLabel="Eliminar"
        onConfirm={confirmDelete}
        onCancel={() => setPendingDelete(null)}
      />
    </>
  );
}
