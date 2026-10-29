import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <div className="auth-shell">
      <div className="auth-card">
        <p className="eyebrow">404</p>
        <h1>Página no encontrada</h1>
        <p className="muted">La ruta que buscás no existe o fue movida.</p>
        <Link className="button button-primary" to="/">
          Volver al inicio
        </Link>
      </div>
    </div>
  );
}
