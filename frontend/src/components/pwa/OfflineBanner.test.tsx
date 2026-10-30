import { act, render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { OfflineBanner } from './OfflineBanner';

describe('OfflineBanner', () => {
  it('aparece al perder la conexión y se oculta al recuperarla', () => {
    render(<OfflineBanner />);
    expect(screen.queryByRole('status')).not.toBeInTheDocument();

    act(() => {
      window.dispatchEvent(new Event('offline'));
    });

    expect(screen.getByRole('status')).toHaveTextContent('Sin conexión');

    act(() => {
      window.dispatchEvent(new Event('online'));
    });

    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });
});
