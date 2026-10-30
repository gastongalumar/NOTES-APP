import { act, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { InstallPrompt } from './InstallPrompt';
import type { BeforeInstallPromptEvent } from '@/types/pwa';

function buildInstallEvent(prompt: () => Promise<void>): BeforeInstallPromptEvent {
  const event = new Event('beforeinstallprompt') as BeforeInstallPromptEvent;
  Object.defineProperties(event, {
    prompt: { value: prompt },
    userChoice: {
      value: Promise.resolve({ outcome: 'accepted', platform: 'web' }),
    },
    platforms: { value: ['web'] },
  });
  return event;
}

describe('InstallPrompt', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('no se muestra si el navegador no ofrece instalar', () => {
    render(<InstallPrompt />);
    expect(screen.queryByRole('button', { name: /instalar app/i })).not.toBeInTheDocument();
  });

  it('abre el prompt nativo y desaparece al instalar', async () => {
    const prompt = vi.fn().mockResolvedValue(undefined);
    const user = userEvent.setup();
    render(<InstallPrompt />);

    act(() => {
      window.dispatchEvent(buildInstallEvent(prompt));
    });

    const button = screen.getByRole('button', { name: /instalar app/i });
    await user.click(button);
    expect(prompt).toHaveBeenCalledTimes(1);
    expect(screen.queryByRole('button', { name: /instalar app/i })).not.toBeInTheDocument();

    act(() => {
      window.dispatchEvent(new Event('appinstalled'));
    });
  });

  it('se oculta cuando el navegador confirma la instalación', () => {
    render(<InstallPrompt />);
    act(() => {
      window.dispatchEvent(buildInstallEvent(vi.fn()));
    });
    expect(screen.getByRole('button', { name: /instalar app/i })).toBeInTheDocument();

    act(() => {
      window.dispatchEvent(new Event('appinstalled'));
    });
    expect(screen.queryByRole('button', { name: /instalar app/i })).not.toBeInTheDocument();
  });
});
