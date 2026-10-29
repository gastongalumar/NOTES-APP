import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { NotificationBell } from './NotificationBell';
import { unreadCount } from '@/api/notifications';

vi.mock('@/api/notifications', () => ({
  unreadCount: vi.fn(),
}));

function LocationProbe() {
  return <div data-testid="location">{useLocation().pathname}</div>;
}

describe('NotificationBell', () => {
  afterEach(() => vi.restoreAllMocks());

  it('muestra el contador y navega a notificaciones', async () => {
    vi.mocked(unreadCount).mockResolvedValue(3);
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={['/']}>
        <LocationProbe />
        <Routes>
          <Route path="/" element={<NotificationBell />} />
          <Route path="/notifications" element={<div>Notificaciones</div>} />
        </Routes>
      </MemoryRouter>,
    );

    expect(await screen.findByText('3')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /notificaciones/i }));
    expect(screen.getByTestId('location')).toHaveTextContent('/notifications');
  });
});
