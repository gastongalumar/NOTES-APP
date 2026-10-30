import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it } from 'vitest';
import { ThemeProvider, useTheme } from './ThemeContext';

function Consumer() {
  const { preference, theme, setPreference } = useTheme();
  return (
    <div>
      <span data-testid="preference">{preference}</span>
      <span data-testid="theme">{theme}</span>
      <button type="button" onClick={() => setPreference('dark-aurora')}>
        Aurora
      </button>
    </div>
  );
}

describe('ThemeProvider', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
    document.documentElement.removeAttribute('data-theme-mode');
  });

  it('usa Cobalto por defecto en modo sistema y persiste el cambio', async () => {
    const user = userEvent.setup();
    render(
      <ThemeProvider>
        <Consumer />
      </ThemeProvider>,
    );

    expect(screen.getByTestId('preference')).toHaveTextContent('system');
    expect(screen.getByTestId('theme')).toHaveTextContent('light-cobalt');
    expect(document.documentElement.dataset.theme).toBe('light-cobalt');

    await user.click(screen.getByRole('button', { name: 'Aurora' }));

    expect(screen.getByTestId('preference')).toHaveTextContent('dark-aurora');
    expect(screen.getByTestId('theme')).toHaveTextContent('dark-aurora');
    expect(localStorage.getItem('notasapp.theme')).toBe('dark-aurora');
    expect(document.documentElement.dataset.theme).toBe('dark-aurora');
  });

  it('migra las preferencias viejas light/dark', () => {
    localStorage.setItem('notasapp.theme', 'dark');
    render(
      <ThemeProvider>
        <Consumer />
      </ThemeProvider>,
    );
    expect(screen.getByTestId('preference')).toHaveTextContent('dark-purple');
    expect(screen.getByTestId('theme')).toHaveTextContent('dark-purple');
  });
});
