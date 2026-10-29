import { render, screen, waitFor } from '@testing-library/react';
import { afterAll, afterEach, describe, expect, it, vi } from 'vitest';
import { AttachmentPreview } from './AttachmentPreview';
import { fetchAttachmentBlob } from '@/api/attachments';

vi.mock('@/api/attachments', () => ({
  fetchAttachmentBlob: vi.fn(),
  downloadAttachment: vi.fn(),
}));

const createObjectURL = vi.fn(() => 'blob:preview');
const revokeObjectURL = vi.fn();

describe('AttachmentPreview', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  afterAll(() => {
    vi.unstubAllGlobals();
  });

  it('renderiza una imagen desde blob y libera la URL al desmontar', async () => {
    vi.stubGlobal('URL', {
      ...URL,
      createObjectURL,
      revokeObjectURL,
    });
    vi.mocked(fetchAttachmentBlob).mockResolvedValue(new Blob(['png'], { type: 'image/png' }));

    const { unmount } = render(
      <AttachmentPreview attachmentId={1} mimeType="image/png" filename="foto.png" />,
    );

    const image = await screen.findByRole('img', { name: 'foto.png' });
    expect(image).toHaveAttribute('src', 'blob:preview');

    unmount();
    await waitFor(() => expect(revokeObjectURL).toHaveBeenCalledWith('blob:preview'));
  });

  it('renderiza PDF en iframe', async () => {
    vi.stubGlobal('URL', {
      ...URL,
      createObjectURL,
      revokeObjectURL,
    });
    vi.mocked(fetchAttachmentBlob).mockResolvedValue(new Blob(['pdf'], { type: 'application/pdf' }));

    render(<AttachmentPreview attachmentId={3} mimeType="application/pdf" filename="doc.pdf" />);

    expect(await screen.findByTitle('Vista previa de doc.pdf')).toHaveAttribute('src', 'blob:preview');
  });

  it('renderiza texto plano en un pre', async () => {
    const textBlob = {
      type: 'text/plain',
      text: async () => 'hola desde txt',
    } as Blob;
    vi.mocked(fetchAttachmentBlob).mockResolvedValue(textBlob);

    render(<AttachmentPreview attachmentId={2} mimeType="text/plain" filename="nota.txt" />);

    expect(await screen.findByText('hola desde txt')).toBeInTheDocument();
  });
});
