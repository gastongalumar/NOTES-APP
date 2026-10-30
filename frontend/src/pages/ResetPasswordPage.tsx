import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { resetPassword } from '@/api/auth';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import { AuthLayout } from '@/components/auth/AuthLayout';
import type { ApiError } from '@/types/api';

export function ResetPasswordPage() {
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';
  const navigate = useNavigate();
  const { push } = useToast();
  const [password, setPassword] = useState('');
  const [error, setError] = useState<ApiError | null>(null);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    try {
      await resetPassword(token, password);
      push('Contraseña actualizada. Iniciá sesión.', 'success');
      navigate('/login', { replace: true });
    } catch (caught) {
      setError(caught as ApiError);
    }
  };

  return (
    <AuthLayout
      title="Restablecer contraseña"
      subtitle="Elegí una contraseña nueva para tu cuenta."
      footer={<Link to="/login">Volver a entrar</Link>}
    >
      {!token ? (
        <div className="error-banner" role="alert">
          El enlace no contiene un token válido.
        </div>
      ) : (
        <form className="auth-form" onSubmit={submit}>
          {error ? (
            <div className="error-banner" role="alert">
              {error.message}
            </div>
          ) : null}
          <Field
            id="reset-password"
            label="Nueva contraseña"
            hint="Mínimo 10 caracteres, con mayúscula, minúscula y número."
          >
            <input
              id="reset-password"
              className="input"
              type="password"
              autoComplete="new-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </Field>
          <Button type="submit" variant="primary">
            Guardar contraseña
          </Button>
        </form>
      )}
    </AuthLayout>
  );
}
