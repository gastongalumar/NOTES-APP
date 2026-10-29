import { useEffect, useState } from 'react';
import { downloadAttachment, fetchAttachmentBlob } from '@/api/attachments';
import { Button } from '@/components/ui/Button';
import './AttachmentPreview.css';

interface AttachmentPreviewProps {
  attachmentId: number;
  mimeType: string;
  filename: string;
}

type PreviewState =
  | { kind: 'loading' }
  | { kind: 'image'; url: string }
  | { kind: 'pdf'; url: string }
  | { kind: 'text'; content: string }
  | { kind: 'other' }
  | { kind: 'error'; message: string };

export function AttachmentPreview({ attachmentId, mimeType, filename }: AttachmentPreviewProps) {
  const [state, setState] = useState<PreviewState>({ kind: 'loading' });

  useEffect(() => {
    const controller = new AbortController();
    let objectUrl: string | null = null;
    setState({ kind: 'loading' });

    fetchAttachmentBlob(attachmentId, controller.signal)
      .then(async (blob) => {
        if (controller.signal.aborted) return;
        if (mimeType.startsWith('image/')) {
          objectUrl = URL.createObjectURL(blob);
          setState({ kind: 'image', url: objectUrl });
        } else if (mimeType === 'application/pdf') {
          objectUrl = URL.createObjectURL(blob);
          setState({ kind: 'pdf', url: objectUrl });
        } else if (mimeType.startsWith('text/')) {
          const content = await blob.text();
          setState({ kind: 'text', content });
        } else {
          setState({ kind: 'other' });
        }
      })
      .catch((caught) => {
        if (controller.signal.aborted) return;
        setState({
          kind: 'error',
          message: (caught as { message?: string }).message ?? 'No se pudo cargar la vista previa',
        });
      });

    return () => {
      controller.abort();
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [attachmentId, mimeType]);

  if (state.kind === 'loading') {
    return <div className="empty-state">Cargando vista previa…</div>;
  }

  if (state.kind === 'error') {
    return <div className="error-banner" role="alert">{state.message}</div>;
  }

  if (state.kind === 'image') {
    return <img className="attachment-preview__image" src={state.url} alt={filename} />;
  }

  if (state.kind === 'pdf') {
    return (
      <iframe
        className="attachment-preview__pdf"
        src={state.url}
        title={`Vista previa de ${filename}`}
      />
    );
  }

  if (state.kind === 'text') {
    return <pre className="attachment-preview__text">{state.content}</pre>;
  }

  return (
    <div className="attachment-preview__other">
      <p className="muted">Este tipo de archivo no tiene vista previa.</p>
      <Button variant="primary" onClick={() => void downloadAttachment(attachmentId, filename)}>
        Descargar {filename}
      </Button>
    </div>
  );
}
