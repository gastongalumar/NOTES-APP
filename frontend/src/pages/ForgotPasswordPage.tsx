import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '@/api/auth';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import { AuthLayout } from '@/components/auth/AuthLayout';
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
    <AuthLayout
      title="Recuperar acceso"
      subtitle="Te enviamos un enlace para restablecer tu contraseña."
      footer={<Link to="/login">Volver a entrar</Link>}
    >
      {sent ? (
        <p className="auth-success" role="status">
          Si el email existe, vas a recibir un enlace en los próximos minutos. Revisá spam.
        </p>
      ) : (
        <form className="auth-form" onSubmit={submit}>
          {error ? (
            <div className="error-banner" role="alert">
              {error.message}
            </div>
          ) : null}
          <Field id="forgot-email" label="Email">
            <input
              id="forgot-email"
              className="input"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </Field>
          <Button type="submit" variant="primary">
            Enviar enlace
          </Button>
        </form>
      )}
    </AuthLayout>
  );
}
