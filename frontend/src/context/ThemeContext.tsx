import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';

export type ThemeId = 'light-neumorphic' | 'dark-purple' | 'light-cobalt' | 'dark-aurora';
export type ThemePreference = ThemeId | 'system';

export interface ThemeOption {
  id: ThemePreference;
  label: string;
  short: string;
  description: string;
}

export const THEME_OPTIONS: ThemeOption[] = [
  { id: 'system', label: 'Sistema', short: 'Auto', description: 'Sigue al dispositivo' },
  { id: 'light-cobalt', label: 'Cobalto', short: 'Cobalto', description: 'Claro, azul profundo' },
  { id: 'dark-aurora', label: 'Aurora', short: 'Aurora', description: 'Oscuro, lavanda' },
  { id: 'light-neumorphic', label: 'Papel', short: 'Papel', description: 'Cálido y neumórfico' },
  { id: 'dark-purple', label: 'Tinta', short: 'Tinta', description: 'Oscuro violeta' },
];

const DEFAULT_LIGHT: ThemeId = 'light-cobalt';
const DEFAULT_DARK: ThemeId = 'dark-aurora';
const STORAGE_KEY = 'notasapp.theme';
const THEME_IDS: ThemeId[] = ['light-neumorphic', 'dark-purple', 'light-cobalt', 'dark-aurora'];

interface ThemeContextValue {
  preference: ThemePreference;
  theme: ThemeId;
  setPreference: (preference: ThemePreference) => void;
}

const ThemeContext = createContext<ThemeContextValue | null>(null);

function isThemeId(value: string | null): value is ThemeId {
  return value !== null && THEME_IDS.includes(value as ThemeId);
}

function systemTheme(): ThemeId {
  if (typeof window.matchMedia !== 'function') return DEFAULT_LIGHT;
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? DEFAULT_DARK : DEFAULT_LIGHT;
}

function readPreference(): ThemePreference {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === 'light') return 'light-neumorphic';
  if (stored === 'dark') return 'dark-purple';
  if (stored === 'system' || isThemeId(stored)) return stored;
  return 'system';
}

function resolve(preference: ThemePreference): ThemeId {
  return preference === 'system' ? systemTheme() : preference;
}

function syncThemeColor(): void {
  const meta = document.querySelector('meta[name="theme-color"]');
  if (!meta) return;
  const background = getComputedStyle(document.documentElement)
    .getPropertyValue('--color-bg')
    .trim();
  if (background) meta.setAttribute('content', background);
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [preference, setPreferenceState] = useState<ThemePreference>(readPreference);
  const [theme, setTheme] = useState<ThemeId>(() => resolve(readPreference()));

  const apply = useCallback((next: ThemePreference) => {
    const nextTheme = resolve(next);
    document.documentElement.dataset.theme = nextTheme;
    document.documentElement.dataset.themeMode = next;
    syncThemeColor();
    setTheme(nextTheme);
  }, []);

  useEffect(() => {
    apply(preference);
    localStorage.setItem(STORAGE_KEY, preference);
    if (preference !== 'system' || typeof window.matchMedia !== 'function') return;
    const media = window.matchMedia('(prefers-color-scheme: dark)');
    const listener = () => apply('system');
    media.addEventListener('change', listener);
    return () => media.removeEventListener('change', listener);
  }, [preference, apply]);

  const setPreference = useCallback((next: ThemePreference) => {
    setPreferenceState(next);
  }, []);

  const value = useMemo(
    () => ({ preference, theme, setPreference }),
    [preference, theme, setPreference],
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
