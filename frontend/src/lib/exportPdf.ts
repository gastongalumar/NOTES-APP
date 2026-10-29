import type { Note } from '@/types/api';

export async function exportNotePdf(note: Note): Promise<void> {
  const { jsPDF } = await import('jspdf');
  const doc = new jsPDF({ unit: 'pt', format: 'a4' });
  const margin = 56;
  const width = doc.internal.pageSize.getWidth() - margin * 2;
  let y = margin;

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(20);
  const title = doc.splitTextToSize(note.titulo, width);
  doc.text(title, margin, y);
  y += title.length * 24 + 10;

  doc.setFont('helvetica', 'normal');
  doc.setFontSize(10);
  const meta = [
    note.estado,
    `Prioridad ${note.prioridad}`,
    note.fechaVencimiento ? `Vence: ${new Date(note.fechaVencimiento).toLocaleString('es-AR')}` : '',
    `Actualizada: ${new Date(note.updatedAt).toLocaleString('es-AR')}`,
  ].filter(Boolean).join(' · ');
  doc.text(doc.splitTextToSize(meta, width), margin, y);
  y += 24;

  doc.setFontSize(12);
  const lines = doc.splitTextToSize(note.contenido ?? 'Sin contenido.', width);
  for (const line of lines) {
    if (y > doc.internal.pageSize.getHeight() - margin) {
      doc.addPage();
      y = margin;
    }
    doc.text(line, margin, y);
    y += 16;
  }
  doc.save(`${note.titulo.replace(/[^\p{L}0-9 _-]/gu, '').slice(0, 60) || 'nota'}.pdf`);
}
