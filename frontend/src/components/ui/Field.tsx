import type { ReactNode } from 'react';

interface FieldProps {
  id: string;
  label: string;
  error?: string;
  hint?: string;
  children: ReactNode;
  full?: boolean;
}

export function Field({ id, label, error, hint, children, full = false }: FieldProps) {
  return (
    <div className={full ? 'field full' : 'field'}>
      <label htmlFor={id}>{label}</label>
      {children}
      {hint && !error ? (
        <span className="subtle" id={`${id}-hint`}>
          {hint}
        </span>
      ) : null}
      {error ? (
        <span className="field-error" id={`${id}-error`} role="alert">
          {error}
        </span>
      ) : null}
    </div>
  );
}
