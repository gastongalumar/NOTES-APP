import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import type { ApiError } from '@/types/api';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? '/';
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [totp, setTotp] = useState('');
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(username, password, totp || undefined);
      navigate(from, { replace: true });
    } catch (caught) {
      setError(caught as ApiError);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <p className="eyebrow">Agenda personal + archivador seguro</p>
        <h1>Entrar a NotasApp</h1>
        <p className="muted">Tus notas, tareas y archivos en un solo lugar.</p>
        <form className="auth-form" onSubmit={submit}>
          {error ? <div className="error-banner" role="alert">{error.message}</div> : null}
          <Field id="username" label="Usuario">
            <input
              id="username"
              className="input"
              autoComplete="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
          </Field>
          <Field id="password" label="Contraseña">
            <input
              id="password"
              className="input"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </Field>
          <Field id="totp" label="Código 2FA (si está activo)" hint="6 dígitos o código de recuperación">
            <input
              id="totp"
              className="input"
              autoComplete="one-time-code"
              inputMode="numeric"
              value={totp}
              onChange={(event) => setTotp(event.target.value)}
            />
          </Field>
          <Button type="submit" variant="primary" disabled={loading}>
            {loading ? 'Entrando…' : 'Entrar'}
          </Button>
        </form>
        <p className="auth-footer">
          <Link to="/forgot-password">Olvidé mi contraseña</Link>
          <br />
          ¿No tenés cuenta? <Link to="/register">Crear cuenta</Link>
        </p>
      </div>
    </div>
  );
}
