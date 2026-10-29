import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import type { NoteSummary } from '@/types/api';
import { NoteCard } from './NoteCard';

const note: NoteSummary = {
  id: 7,
  titulo: 'Nota de prueba',
  extracto: 'Contenido de prueba',
  archivada: false,
  fechaVencimiento: null,
  prioridad: 2,
  estado: 'PENDIENTE',
  categorias: [{ id: 1, nombre: 'Trabajo', color: '#a3122a' }],
  tags: [],
  updatedAt: '2026-10-29T12:00:00Z',
};

function LocationProbe() {
  return <div data-testid="location">{useLocation().pathname}</div>;
}

function renderCard(onDelete = vi.fn(), onArchive = vi.fn()) {
  return render(
    <MemoryRouter initialEntries={['/']}>
      <LocationProbe />
      <Routes>
        <Route path="/" element={<NoteCard note={note} onDelete={onDelete} onArchive={onArchive} />} />
        <Route path="/notes/:id" element={<div>Detalle de nota</div>} />
        <Route path="/notes/:id/edit" element={<div>Editor</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('NoteCard', () => {
  it('navega al detalle al hacer click en cualquier parte de la card', async () => {
    const user = userEvent.setup();
    renderCard();
    await user.click(screen.getByText('Contenido de prueba'));
    expect(screen.getByTestId('location')).toHaveTextContent('/notes/7');
  });

  it('navega con teclado desde la card', async () => {
    const user = userEvent.setup();
    renderCard();
    await user.tab();
    await user.keyboard('{Enter}');
    expect(screen.getByTestId('location')).toHaveTextContent('/notes/7');
  });

  it('elimina sin navegar', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();
    renderCard(onDelete);
    await user.click(screen.getByRole('button', { name: 'Eliminar' }));
    expect(onDelete).toHaveBeenCalledWith(note);
    expect(screen.getByTestId('location')).toHaveTextContent('/');
  });
});
