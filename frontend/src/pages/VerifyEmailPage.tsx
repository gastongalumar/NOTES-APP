import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { verifyEmail } from '@/api/auth';

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
    <div className="auth-shell">
      <div className="auth-card">
        <p className="eyebrow">Verificación</p>
        <h1>{status === 'ok' ? 'Email verificado' : status === 'error' ? 'No se pudo verificar' : 'Verificando…'}</h1>
        {message ? <div className="error-banner" role="alert">{message}</div> : null}
        <p className="auth-footer"><Link to="/login">Volver a entrar</Link></p>
      </div>
    </div>
  );
}
