import { useTheme, THEME_OPTIONS } from '@/context/ThemeContext';
import './ThemePicker.css';

interface ThemeOptionsProps {
  name: string;
  onSelect?: () => void;
}

export function ThemeOptions({ name, onSelect }: ThemeOptionsProps) {
  const { preference, setPreference } = useTheme();

  return (
    <div className="theme-options" role="radiogroup" aria-label="Tema de la interfaz">
      {THEME_OPTIONS.map((option) => (
        <label
          key={option.id}
          className={`theme-option${preference === option.id ? ' is-selected' : ''}`}
        >
          <input
            type="radio"
            name={name}
            value={option.id}
            checked={preference === option.id}
            onChange={() => {
              setPreference(option.id);
              onSelect?.();
            }}
          />
          <span className={`theme-swatch theme-swatch-${option.id}`} aria-hidden="true" />
          <span className="theme-option-text">
            <span className="theme-option-label">{option.label}</span>
            <span className="theme-option-description">{option.description}</span>
          </span>
          <span className="theme-option-check" aria-hidden="true">
            ✓
          </span>
        </label>
      ))}
    </div>
  );
}
