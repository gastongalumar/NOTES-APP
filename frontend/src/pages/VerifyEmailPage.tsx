import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { verifyEmail } from '@/api/auth';
import { AuthLayout } from '@/components/auth/AuthLayout';

export function VerifyEmailPage() {
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';
  const [status, setStatus] = useState<'loading' | 'ok' | 'error'>('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('El enlace no contiene un token válido.');
      return;
    }
    verifyEmail(token)
      .then(() => setStatus('ok'))
      .catch((caught) => {
        setStatus('error');
        setMessage((caught as { message?: string }).message ?? 'No se pudo verificar el email.');
      });
  }, [token]);

  return (
    <AuthLayout
      title={
        status === 'ok'
          ? 'Email verificado'
          : status === 'error'
            ? 'No se pudo verificar'
            : 'Verificando…'
      }
      subtitle={
        status === 'ok'
          ? 'Ya podés iniciar sesión con tu cuenta.'
          : status === 'error'
            ? 'El enlace puede haber expirado o ya haberse usado.'
            : 'Estamos confirmando tu email.'
      }
      footer={<Link to="/login">Ir a entrar</Link>}
    >
      {message ? (
        <div className="error-banner" role="alert">
          {message}
        </div>
      ) : null}
      {status === 'ok' ? (
        <p className="auth-success" role="status">
          Tu email quedó confirmado.
        </p>
      ) : null}
    </AuthLayout>
  );
}
