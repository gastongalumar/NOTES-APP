import { useTheme } from '@/context/ThemeContext';

const LABELS = {
  system: 'Sistema',
  light: 'Claro',
  dark: 'Oscuro',
} as const;

export function ThemeToggle() {
  const { mode, cycle } = useTheme();
  return (
    <button
      type="button"
      className="theme-toggle"
      onClick={cycle}
      aria-label={`Tema: ${LABELS[mode]}. Cambiar tema`}
    >
      <span aria-hidden="true">◐</span>
      {LABELS[mode]}
    </button>
  );
}
