import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import type { Category, NoteInput, NoteState, Tag } from '@/types/api';
import { createNote, deleteNote, getNote, updateNote } from '@/api/notes';
import { listCategories } from '@/api/categories';
import { listTags } from '@/api/tags';
import { fromDateTimeLocal, toDateTimeLocal } from '@/lib/format';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';
import { AttachmentPanel } from '@/components/attachments/AttachmentPanel';

const EMPTY = {
  titulo: '',
  contenido: '',
  categoriaIds: [] as number[],
  tagIds: [] as number[],
  prioridad: 2,
  estado: 'PENDIENTE' as NoteState,
};

export function NoteEditorPage() {
  const { id } = useParams();
  const noteId = id ? Number(id) : null;
  const navigate = useNavigate();
  const { push } = useToast();
  const [form, setForm] = useState(EMPTY);
  const [dueDate, setDueDate] = useState('');
  const [version, setVersion] = useState<number | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [loading, setLoading] = useState(Boolean(noteId));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [confirmDelete, setConfirmDelete] = useState(false);

  useEffect(() => {
    Promise.all([listCategories(), listTags()])
      .then(([nextCategories, nextTags]) => {
        setCategories(nextCategories);
        setTags(nextTags);
      })
      .catch(() => push('No se pudieron cargar categorías o tags', 'error'));
  }, [push]);

  useEffect(() => {
    if (!noteId) return;
    setLoading(true);
    getNote(noteId)
      .then((note) => {
        setForm({
          titulo: note.titulo,
          contenido: note.contenido ?? '',
          categoriaIds: note.categorias.map((category) => category.id),
          tagIds: note.tags.map((tag) => tag.id),
          prioridad: note.prioridad,
          estado: note.estado,
        });
        setDueDate(toDateTimeLocal(note.fechaVencimiento));
        setVersion(note.version);
      })
      .catch((caught) => setError((caught as { message?: string }).message ?? 'No se pudo cargar la nota'))
      .finally(() => setLoading(false));
  }, [noteId]);

  const update = <K extends keyof typeof form>(field: K, value: (typeof form)[K]) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const toggle = (field: 'categoriaIds' | 'tagIds', value: number) => {
    setForm((current) => {
      const list = current[field];
      return {
        ...current,
        [field]: list.includes(value) ? list.filter((item) => item !== value) : [...list, value],
      };
    });
  };

  const buildInput = (): NoteInput => ({
    ...form,
    fechaVencimiento: fromDateTimeLocal(dueDate),
  });

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    setSaving(true);
    try {
      const saved = noteId && version !== null
        ? await updateNote(noteId, { ...buildInput(), version })
        : await createNote(buildInput());
      push(noteId ? 'Nota actualizada' : 'Nota creada', 'success');
      navigate(`/notes/${saved.id}`, { replace: true });
    } catch (caught) {
      setError((caught as { message?: string }).message ?? 'No se pudo guardar la nota');
    } finally {
      setSaving(false);
    }
  };

  const remove = async () => {
    if (!noteId) return;
    try {
      await deleteNote(noteId);
      push('Nota eliminada', 'success');
      navigate('/notes', { replace: true });
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo eliminar', 'error');
    }
  };

  if (loading) {
    return <div className="empty-state">Cargando nota…</div>;
  }

  return (
    <div className="note-view">
      <header className="page-header">
        <div>
          <p className="eyebrow">{noteId ? 'Editar' : 'Nueva nota'}</p>
          <h1>{noteId ? 'Editar nota' : 'Crear nota'}</h1>
        </div>
        {noteId ? (
          <Button variant="danger" onClick={() => setConfirmDelete(true)}>
            Eliminar
          </Button>
        ) : null}
      </header>

      <form onSubmit={submit} className="auth-form">
        {error ? <div className="error-banner" role="alert">{error}</div> : null}
        <Field id="titulo" label="Título">
          <input id="titulo" className="input" value={form.titulo} onChange={(event) => update('titulo', event.target.value)} required maxLength={200} />
        </Field>
        <Field id="contenido" label="Contenido" hint="Acepta Markdown.">
          <textarea id="contenido" className="textarea" value={form.contenido} onChange={(event) => update('contenido', event.target.value)} />
        </Field>
        <div className="form-grid">
          <Field id="due" label="Vencimiento">
            <input id="due" className="input" type="datetime-local" value={dueDate} onChange={(event) => setDueDate(event.target.value)} />
          </Field>
          <Field id="prioridad" label="Prioridad">
            <select id="prioridad" className="select" value={form.prioridad} onChange={(event) => update('prioridad', Number(event.target.value))}>
              <option value={1}>Alta</option>
              <option value={2}>Media</option>
              <option value={3}>Baja</option>
            </select>
          </Field>
          <Field id="estado" label="Estado">
            <select id="estado" className="select" value={form.estado} onChange={(event) => update('estado', event.target.value as NoteState)}>
              <option value="PENDIENTE">Pendiente</option>
              <option value="EN_CURSO">En curso</option>
              <option value="HECHA">Hecha</option>
            </select>
          </Field>
        </div>

        <fieldset className="field">
          <legend>Categorías</legend>
          <div className="tag-list">
            {categories.map((category) => (
              <label className="badge" key={category.id}>
                <input type="checkbox" checked={form.categoriaIds.includes(category.id)} onChange={() => toggle('categoriaIds', category.id)} />
                {category.nombre}
              </label>
            ))}
            {categories.length === 0 ? <span className="subtle">Todavía no hay categorías.</span> : null}
          </div>
        </fieldset>

        <fieldset className="field">
          <legend>Tags</legend>
          <div className="tag-list">
            {tags.map((tag) => (
              <label className="badge" key={tag.id}>
                <input type="checkbox" checked={form.tagIds.includes(tag.id)} onChange={() => toggle('tagIds', tag.id)} />
                #{tag.nombre}
              </label>
            ))}
            {tags.length === 0 ? <span className="subtle">Todavía no hay tags.</span> : null}
          </div>
        </fieldset>

        <div className="toolbar">
          <Button type="submit" variant="primary" disabled={saving}>
            {saving ? 'Guardando…' : 'Guardar nota'}
          </Button>
          <Button onClick={() => navigate(-1)}>Cancelar</Button>
        </div>
      </form>

      {noteId ? <AttachmentPanel noteId={noteId} /> : null}

      <ConfirmDialog
        open={confirmDelete}
        title="Eliminar nota"
        message="La nota se ocultará y se conservará 30 días para purga."
        confirmLabel="Eliminar"
        onConfirm={remove}
        onCancel={() => setConfirmDelete(false)}
      />
    </div>
  );
}
