import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '@/api/auth';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import type { ApiError } from '@/types/api';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    try {
      await forgotPassword(email);
      setSent(true);
    } catch (caught) {
      setError(caught as ApiError);
    }
  };

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <p className="eyebrow">Recuperar acceso</p>
        <h1>Olvidé mi contraseña</h1>
        {sent ? (
          <p className="muted">Si el email existe, vas a recibir un enlace en los próximos minutos.</p>
        ) : (
          <form className="auth-form" onSubmit={submit}>
            {error ? <div className="error-banner" role="alert">{error.message}</div> : null}
            <Field id="forgot-email" label="Email">
              <input id="forgot-email" className="input" type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </Field>
            <Button type="submit" variant="primary">Enviar enlace</Button>
          </form>
        )}
        <p className="auth-footer"><Link to="/login">Volver a entrar</Link></p>
      </div>
    </div>
  );
}
