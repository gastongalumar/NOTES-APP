import { useState } from 'react';
import { Dialog } from '@/components/ui/Dialog';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import { createShare } from '@/api/shares';
import { useToast } from '@/context/ToastContext';

export function ShareDialog({ noteId, open, onClose }: { noteId: number; open: boolean; onClose: () => void }) {
  const { push } = useToast();
  const [days, setDays] = useState('7');
  const [url, setUrl] = useState('');
  const [error, setError] = useState('');

  const create = async () => {
    setError('');
    try {
      const expiresAt = new Date(Date.now() + Number(days) * 86_400_000).toISOString();
      const response = await createShare(noteId, expiresAt);
      setUrl(response.url);
      push('Enlace creado', 'success');
    } catch (caught) {
      setError((caught as { message?: string }).message ?? 'No se pudo crear el enlace');
    }
  };

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(url);
      push('Enlace copiado', 'success');
    } catch {
      push('No se pudo copiar; seleccioná el enlace manualmente', 'error');
    }
  };

  return (
    <Dialog
      open={open}
      title="Compartir nota"
      onClose={onClose}
      footer={<Button onClick={onClose}>Cerrar</Button>}
    >
      {error ? <div className="error-banner" role="alert">{error}</div> : null}
      {!url ? (
        <>
          <p className="muted">Se creará un enlace público de solo lectura.</p>
          <Field id="share-days" label="Duración">
            <select id="share-days" className="select" value={days} onChange={(e) => setDays(e.target.value)}>
              <option value="1">1 día</option>
              <option value="7">7 días</option>
              <option value="30">30 días</option>
            </select>
          </Field>
          <Button variant="primary" onClick={create} style={{ marginTop: 'var(--space-4)' }}>
            Crear enlace
          </Button>
        </>
      ) : (
        <>
          <Field id="share-url" label="Enlace">
            <input id="share-url" className="input" value={url} readOnly onFocus={(e) => e.target.select()} />
          </Field>
          <Button variant="primary" onClick={copy} style={{ marginTop: 'var(--space-4)' }}>
            Copiar enlace
          </Button>
        </>
      )}
    </Dialog>
  );
}
