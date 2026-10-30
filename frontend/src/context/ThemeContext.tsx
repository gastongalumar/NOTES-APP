import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';

export type ThemeMode = 'system' | 'light' | 'dark';
type ResolvedTheme = 'light-neumorphic' | 'dark-purple';

interface ThemeContextValue {
  mode: ThemeMode;
  theme: ResolvedTheme;
  setMode: (mode: ThemeMode) => void;
  cycle: () => void;
}

const STORAGE_KEY = 'notasapp.theme';
const ThemeContext = createContext<ThemeContextValue | null>(null);

function syncThemeColor(): void {
  const meta = document.querySelector('meta[name="theme-color"]');
  if (!meta) return;
  const background = getComputedStyle(document.documentElement)
    .getPropertyValue('--color-bg')
    .trim();
  if (background) meta.setAttribute('content', background);
}

function systemTheme(): ResolvedTheme {
  if (typeof window.matchMedia !== 'function') return 'light-neumorphic';
  return window.matchMedia('(prefers-color-scheme: dark)').matches
    ? 'dark-purple'
    : 'light-neumorphic';
}

function readMode(): ThemeMode {
  const stored = localStorage.getItem(STORAGE_KEY);
  return stored === 'light' || stored === 'dark' || stored === 'system'
    ? stored
    : 'system';
}

function resolve(mode: ThemeMode): ResolvedTheme {
  if (mode === 'light') return 'light-neumorphic';
  if (mode === 'dark') return 'dark-purple';
  return systemTheme();
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [mode, setModeState] = useState<ThemeMode>(() => readMode());
  const [theme, setTheme] = useState<ResolvedTheme>(() => resolve(readMode()));

  const apply = useCallback((nextMode: ThemeMode) => {
    const nextTheme = resolve(nextMode);
    document.documentElement.dataset.theme = nextTheme;
    document.documentElement.dataset.themeMode = nextMode;
    syncThemeColor();
    setTheme(nextTheme);
  }, []);

  useEffect(() => {
    apply(mode);
    localStorage.setItem(STORAGE_KEY, mode);
    if (mode !== 'system' || typeof window.matchMedia !== 'function') return;
    const media = window.matchMedia('(prefers-color-scheme: dark)');
    const listener = () => apply('system');
    media.addEventListener('change', listener);
    return () => media.removeEventListener('change', listener);
  }, [mode, apply]);

  const setMode = useCallback(
    (next: ThemeMode) => {
      setModeState(next);
    },
    [],
  );

  const cycle = useCallback(() => {
    setModeState((current) =>
      current === 'system' ? 'light' : current === 'light' ? 'dark' : 'system',
    );
  }, []);

  const value = useMemo(
    () => ({ mode, theme, setMode, cycle }),
    [mode, theme, setMode, cycle],
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

export function useTheme(): ThemeContextValue {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme debe usarse dentro de ThemeProvider');
  }
  return context;
}
