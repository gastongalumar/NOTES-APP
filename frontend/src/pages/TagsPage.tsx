import { useEffect, useState, type FormEvent } from 'react';
import type { Tag } from '@/types/api';
import { createTag, deleteTag, listTags, updateTag } from '@/api/tags';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';

export function TagsPage() {
  const { push } = useToast();
  const [tags, setTags] = useState<Tag[]>([]);
  const [name, setName] = useState('');
  const [editing, setEditing] = useState<number | null>(null);
  const [pendingDelete, setPendingDelete] = useState<Tag | null>(null);
  const [error, setError] = useState('');

  const load = () => {
    listTags()
      .then(setTags)
      .catch((caught) => setError((caught as { message?: string }).message ?? 'No se pudieron cargar los tags'));
  };

  useEffect(load, []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      if (editing) {
        await updateTag(editing, name);
        push('Tag actualizado', 'success');
      } else {
        await createTag(name);
        push('Tag creado', 'success');
      }
      setName('');
      setEditing(null);
      load();
    } catch (caught) {
      setError((caught as { message?: string }).message ?? 'No se pudo guardar');
    }
  };

  const remove = async () => {
    if (!pendingDelete) return;
    try {
      await deleteTag(pendingDelete.id);
      push('Tag eliminado', 'success');
      setPendingDelete(null);
      load();
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo eliminar', 'error');
    }
  };

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Etiquetas libres</p>
          <h1>Tags</h1>
        </div>
      </header>

      <form className="card auth-form" onSubmit={submit}>
        <Field id="tag-name" label={editing ? 'Editar tag' : 'Nuevo tag'}>
          <input id="tag-name" className="input" value={name} onChange={(event) => setName(event.target.value)} required maxLength={50} />
        </Field>
        {error ? <div className="error-banner" role="alert">{error}</div> : null}
        <div className="toolbar">
          <Button type="submit" variant="primary">{editing ? 'Guardar' : 'Crear tag'}</Button>
          {editing ? <Button onClick={() => { setEditing(null); setName(''); }}>Cancelar</Button> : null}
        </div>
      </form>

      <div className="tag-list" style={{ marginTop: 'var(--space-6)' }}>
        {tags.map((tag) => (
          <span className="badge" key={tag.id}>
            #{tag.nombre}
            <Button small variant="ghost" onClick={() => { setEditing(tag.id); setName(tag.nombre); }}>Editar</Button>
            <Button small variant="ghost" onClick={() => setPendingDelete(tag)}>Eliminar</Button>
          </span>
        ))}
      </div>

      <ConfirmDialog
        open={Boolean(pendingDelete)}
        title="Eliminar tag"
        message={`¿Eliminar #${pendingDelete?.nombre ?? ''}? Se quitará de las notas.`}
        confirmLabel="Eliminar"
        onConfirm={remove}
        onCancel={() => setPendingDelete(null)}
      />
    </>
  );
}
