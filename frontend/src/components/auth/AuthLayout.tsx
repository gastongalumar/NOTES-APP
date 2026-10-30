import { Link } from 'react-router-dom';
import type { ReactNode } from 'react';

interface AuthLayoutProps {
  title: string;
  subtitle?: string;
  children: ReactNode;
  footer?: ReactNode;
}

export function AuthLayout({ title, subtitle, children, footer }: AuthLayoutProps) {
  return (
    <div className="auth-shell">
      <section className="auth-brand" aria-label="NotasApp">
        <Link to="/" className="brand auth-brand__lockup">
          <span className="brand-mark" aria-hidden="true">
            N
          </span>
          <span className="brand-text">
            <strong>NotasApp</strong>
            <small>Agenda y archivo</small>
          </span>
        </Link>
        <div className="auth-brand__body">
          <h2 className="auth-brand__title">
            Todo lo que tenés que recordar, en un solo lugar.
          </h2>
          <p className="auth-brand__text">
            Notas con vencimiento, adjuntos y búsqueda. Sin ruido, sin métricas inventadas.
          </p>
        </div>
        <ul className="auth-brand__points">
          <li>Recordatorios y agenda diaria</li>
          <li>Archivos y búsqueda full-text</li>
          <li>Verificación en dos pasos opcional</li>
        </ul>
      </section>

      <main className="auth-panel">
        <div className="auth-card">
          <Link to="/" className="brand auth-lockup" aria-label="NotasApp, ir al inicio">
            <span className="brand-mark" aria-hidden="true">
              N
            </span>
            <span className="brand-text">
              <strong>NotasApp</strong>
              <small>Agenda y archivo</small>
            </span>
          </Link>
          <header className="auth-card__header">
            <h1>{title}</h1>
            {subtitle ? <p className="muted">{subtitle}</p> : null}
          </header>
          {children}
        </div>
        {footer ? <div className="auth-panel__foot">{footer}</div> : null}
      </main>
    </div>
  );
}
