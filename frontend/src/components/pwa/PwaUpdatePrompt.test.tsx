import { act, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { PwaUpdatePrompt } from './PwaUpdatePrompt';
import { applyPwaUpdate, setPwaState } from '@/pwa/updates';

vi.mock('@/pwa/updates', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/pwa/updates')>();
  return { ...actual, applyPwaUpdate: vi.fn() };
});

describe('PwaUpdatePrompt', () => {
  afterEach(() => {
    act(() => setPwaState({ needRefresh: false }));
    vi.mocked(applyPwaUpdate).mockClear();
  });

  it('no se muestra si no hay actualización pendiente', () => {
    render(<PwaUpdatePrompt />);
    expect(screen.queryByText('Hay una versión nueva')).not.toBeInTheDocument();
  });

  it('aplica la actualización y permite posponerla', async () => {
    const user = userEvent.setup();
    render(<PwaUpdatePrompt />);

    act(() => setPwaState({ needRefresh: true }));
    expect(screen.getByText('Hay una versión nueva')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Actualizar' }));
    expect(applyPwaUpdate).toHaveBeenCalledTimes(1);

    await user.click(screen.getByRole('button', { name: 'Ahora no' }));
    expect(screen.queryByText('Hay una versión nueva')).not.toBeInTheDocument();
  });
});
