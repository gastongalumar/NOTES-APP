import { useEffect, useId, useRef, useState } from 'react';
import { THEME_OPTIONS, useTheme } from '@/context/ThemeContext';
import { ThemeOptions } from './ThemeOptions';
import './ThemePicker.css';

interface ThemePickerProps {
  placement?: 'up' | 'down';
  compact?: boolean;
}

export function ThemePicker({ placement = 'down', compact = false }: ThemePickerProps) {
  const { preference } = useTheme();
  const [open, setOpen] = useState(false);
  const root = useRef<HTMLDivElement>(null);
  const trigger = useRef<HTMLButtonElement>(null);
  const panel = useRef<HTMLDivElement>(null);
  const id = useId();
  const current = THEME_OPTIONS.find((option) => option.id === preference)?.label ?? 'Sistema';

  useEffect(() => {
    if (!open) return;
    panel.current?.querySelector<HTMLInputElement>('input:checked')?.focus();

    const onPointerDown = (event: MouseEvent) => {
      if (!root.current?.contains(event.target as Node)) setOpen(false);
    };
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key !== 'Escape') return;
      setOpen(false);
      trigger.current?.focus();
    };

    document.addEventListener('mousedown', onPointerDown);
    document.addEventListener('keydown', onKeyDown);
    return () => {
      document.removeEventListener('mousedown', onPointerDown);
      document.removeEventListener('keydown', onKeyDown);
    };
  }, [open]);

  return (
    <div className={`theme-picker theme-picker--${placement}`} ref={root}>
      <button
        ref={trigger}
        type="button"
        className="theme-picker__trigger"
        aria-expanded={open}
        aria-controls={open ? `${id}-panel` : undefined}
        aria-label={`Tema: ${current}. Cambiar tema`}
        onClick={() => setOpen((value) => !value)}
      >
        <svg
          aria-hidden="true"
          viewBox="0 0 24 24"
          width="16"
          height="16"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.6"
          strokeLinecap="round"
        >
          <circle cx="12" cy="12" r="8" />
          <path d="M12 4v16" />
          <path d="M12 4a8 8 0 0 1 0 16z" fill="currentColor" stroke="none" />
        </svg>
        {compact ? <span className="sr-only">{current}</span> : <span>Tema</span>}
      </button>
      {open ? (
        <div className="theme-picker__panel" id={`${id}-panel`} ref={panel}>
          <p className="theme-picker__title">Tema</p>
          <ThemeOptions name={`theme-${id}`} onSelect={() => setOpen(false)} />
        </div>
      ) : null}
    </div>
  );
}
