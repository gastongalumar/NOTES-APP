import { useEffect, useState, type ChangeEvent } from 'react';
import type { Attachment } from '@/types/api';
import {
  deleteAttachment,
  downloadAttachment,
  listAttachments,
  uploadAttachment,
} from '@/api/attachments';
import { formatBytes, formatDate } from '@/lib/format';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/Button';
import { Dialog } from '@/components/ui/Dialog';
import { AttachmentPreview } from './AttachmentPreview';

export function AttachmentPanel({ noteId }: { noteId: number }) {
  const { push } = useToast();
  const [attachments, setAttachments] = useState<Attachment[]>([]);
  const [preview, setPreview] = useState<Attachment | null>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  const load = () => {
    listAttachments(noteId)
      .then(setAttachments)
      .catch((caught) => setError((caught as { message?: string }).message ?? 'No se pudieron cargar los adjuntos'));
  };

  useEffect(load, [noteId]);

  const upload = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;
    setError('');
    setUploading(true);
    try {
      await uploadAttachment(noteId, file);
      push('Archivo subido', 'success');
      load();
    } catch (caught) {
      setError((caught as { message?: string }).message ?? 'No se pudo subir el archivo');
    } finally {
      setUploading(false);
      event.target.value = '';
    }
  };

  const remove = async (id: number) => {
    try {
      await deleteAttachment(id);
      if (preview?.id === id) setPreview(null);
      push('Adjunto eliminado', 'success');
      load();
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo eliminar', 'error');
    }
  };

  const download = async (attachment: Attachment) => {
    try {
      await downloadAttachment(attachment.id, attachment.nombreOriginal);
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo descargar', 'error');
    }
  };

  return (
    <section className="card" style={{ marginTop: 'var(--space-6)' }}>
      <div className="page-header" style={{ marginBottom: 'var(--space-4)' }}>
        <div>
          <p className="eyebrow">Archivador</p>
          <h2>Adjuntos</h2>
        </div>
        <label className="button button-primary">
          {uploading ? 'Subiendo…' : 'Subir archivo'}
          <input
            type="file"
            className="sr-only"
            accept=".pdf,.png,.jpg,.jpeg,.webp,.gif,.txt,.md,.docx,.xlsx"
            onChange={upload}
            disabled={uploading}
          />
        </label>
      </div>
      <p className="subtle">Máximo 10 MB por archivo, 50 MB por nota y 200 MB por cuenta.</p>
      {error ? <div className="error-banner" role="alert">{error}</div> : null}
      {attachments.length === 0 ? (
        <div className="empty-state">No hay adjuntos todavía.</div>
      ) : (
        <ul className="attachment-list">
          {attachments.map((attachment) => (
            <li className="attachment-row" key={attachment.id}>
              <div className="attachment-row__info">
                <strong>{attachment.nombreOriginal}</strong>
                <div className="subtle">
                  {formatBytes(attachment.tamanoBytes)} · {formatDate(attachment.createdAt)}
                </div>
              </div>
              <div className="attachment-row__actions">
                <Button small onClick={() => setPreview(attachment)}>Ver</Button>
                <Button small onClick={() => void download(attachment)}>Descargar</Button>
                <Button small variant="danger" onClick={() => void remove(attachment.id)}>
                  Eliminar
                </Button>
              </div>
            </li>
          ))}
        </ul>
      )}

      <Dialog
        open={Boolean(preview)}
        title={preview ? `Vista previa: ${preview.nombreOriginal}` : 'Vista previa'}
        onClose={() => setPreview(null)}
        footer={<Button onClick={() => setPreview(null)}>Cerrar</Button>}
      >
        {preview ? (
          <AttachmentPreview
            attachmentId={preview.id}
            mimeType={preview.mimeType}
            filename={preview.nombreOriginal}
          />
        ) : null}
      </Dialog>
    </section>
  );
}
