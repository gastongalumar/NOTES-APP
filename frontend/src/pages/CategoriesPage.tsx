import { useEffect, useState, type FormEvent } from 'react';
import type { Category } from '@/types/api';
import {
  createCategory,
  deleteCategory,
  listCategories,
  updateCategory,
} from '@/api/categories';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';

const EMPTY = { nombre: '', color: '', descripcion: '' };

export function CategoriesPage() {
  const { push } = useToast();
  const [categories, setCategories] = useState<Category[]>([]);
  const [form, setForm] = useState(EMPTY);
  const [editing, setEditing] = useState<number | null>(null);
  const [pendingDelete, setPendingDelete] = useState<Category | null>(null);
  const [error, setError] = useState('');

  const load = () => {
    listCategories()
      .then(setCategories)
      .catch((caught) => setError((caught as { message?: string }).message ?? 'No se pudieron cargar las categorías'));
  };

  useEffect(load, []);

  useEffect(() => {
    const accent = getComputedStyle(document.documentElement).getPropertyValue('--color-accent').trim();
    if (accent) {
      setForm((current) => (current.color ? current : { ...current, color: accent }));
    }
  }, [categories]);

  const update = (field: keyof typeof form, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      if (editing) {
        await updateCategory(editing, form);
        push('Categoría actualizada', 'success');
      } else {
        await createCategory(form);
        push('Categoría creada', 'success');
      }
      setForm(EMPTY);
      setEditing(null);
      load();
    } catch (caught) {
      setError((caught as { message?: string }).message ?? 'No se pudo guardar');
    }
  };

  const edit = (category: Category) => {
    setEditing(category.id);
    setForm({ nombre: category.nombre, color: category.color, descripcion: category.descripcion ?? '' });
  };

  const remove = async () => {
    if (!pendingDelete) return;
    try {
      await deleteCategory(pendingDelete.id);
      push('Categoría eliminada', 'success');
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
          <p className="eyebrow">Organización</p>
          <h1>Categorías</h1>
        </div>
      </header>

      <form className="card auth-form" onSubmit={submit}>
        <div className="form-grid">
          <Field id="category-name" label="Nombre">
            <input id="category-name" className="input" value={form.nombre} onChange={(e) => update('nombre', e.target.value)} required maxLength={50} />
          </Field>
          <Field id="category-color" label="Color">
            <input id="category-color" className="input input-color" type="color" value={form.color} onChange={(e) => update('color', e.target.value)} />
          </Field>
          <Field id="category-description" label="Descripción" full>
            <input id="category-description" className="input" value={form.descripcion} onChange={(e) => update('descripcion', e.target.value)} maxLength={500} />
          </Field>
        </div>
        {error ? <div className="error-banner" role="alert">{error}</div> : null}
        <div className="toolbar">
          <Button type="submit" variant="primary">{editing ? 'Guardar cambios' : 'Crear categoría'}</Button>
          {editing ? <Button onClick={() => { setEditing(null); setForm(EMPTY); }}>Cancelar</Button> : null}
        </div>
      </form>

      <div className="card-grid" style={{ marginTop: 'var(--space-6)' }}>
        {categories.map((category) => (
          <article className="card" key={category.id}>
            <span className="badge" style={{ background: category.color, color: 'var(--color-accent-contrast)' }}>
              {category.nombre}
            </span>
            <p className="muted">{category.descripcion || 'Sin descripción.'}</p>
            <p className="subtle">{category.cantidadNotas} notas</p>
            <div className="toolbar" style={{ marginBottom: 0 }}>
              <Button small onClick={() => edit(category)}>Editar</Button>
              <Button small variant="danger" onClick={() => setPendingDelete(category)}>Eliminar</Button>
            </div>
          </article>
        ))}
      </div>

      <ConfirmDialog
        open={Boolean(pendingDelete)}
        title="Eliminar categoría"
        message={`¿Eliminar “${pendingDelete?.nombre ?? ''}”? Solo es posible si no tiene notas.`}
        confirmLabel="Eliminar"
        onConfirm={remove}
        onCancel={() => setPendingDelete(null)}
      />
    </>
  );
}
