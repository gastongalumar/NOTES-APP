import { useEffect, useRef, useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { resendEmailVerification } from '@/api/auth';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import { AuthLayout } from '@/components/auth/AuthLayout';
import type { ApiError } from '@/types/api';

type Step = 'credentials' | 'code';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? '/';
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [totp, setTotp] = useState('');
  const [step, setStep] = useState<Step>('credentials');
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(false);
  const [resendEmail, setResendEmail] = useState('');
  const [resendState, setResendState] = useState<'idle' | 'sending' | 'sent'>('idle');
  const codeRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (step === 'code') codeRef.current?.focus();
  }, [step]);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(username, password, step === 'code' ? totp.trim() : undefined);
      navigate(from, { replace: true });
    } catch (caught) {
      const apiError = caught as ApiError;
      if (apiError.code === 'MFA_REQUIRED') {
        setStep('code');
        setTotp('');
        return;
      }
      setError(apiError);
      if (apiError.code === 'EMAIL_NOT_VERIFIED') {
        setResendEmail(apiError.email ?? '');
      }
      if (step === 'code') setTotp('');
    } finally {
      setLoading(false);
    }
  };

  const backToCredentials = () => {
    setStep('credentials');
    setTotp('');
    setError(null);
  };

  const resend = async () => {
    setResendState('sending');
    try {
      await resendEmailVerification(resendEmail);
      setResendState('sent');
    } catch (caught) {
      setResendState('idle');
      setError(caught as ApiError);
    }
  };

  const emailNotVerified = error?.code === 'EMAIL_NOT_VERIFIED';

  return (
    <AuthLayout
      title={step === 'code' ? 'Verificación en dos pasos' : 'Entrar'}
      subtitle={
        step === 'code'
          ? 'Ingresá el código de tu app autenticadora.'
          : 'Usá tu usuario y contraseña para continuar.'
      }
    >
      <form className="auth-form" onSubmit={submit}>
        {error && !emailNotVerified ? (
          <div className="error-banner" role="alert">
            {error.message}
          </div>
        ) : null}

        {step === 'credentials' ? (
          <>
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
          </>
        ) : (
          <>
            <p className="auth-step">
              Cuenta <strong>{username}</strong>
              <button type="button" className="link-button" onClick={backToCredentials}>
                Cambiar
              </button>
            </p>
            <Field
              id="totp"
              label="Código"
              hint="6 dígitos de tu app o un código de recuperación."
            >
              <input
                id="totp"
                ref={codeRef}
                className="input input-code"
                autoComplete="one-time-code"
                inputMode="numeric"
                value={totp}
                onChange={(event) => setTotp(event.target.value)}
                maxLength={8}
                required
              />
            </Field>
          </>
        )}

        <Button type="submit" variant="primary" disabled={loading}>
          {loading ? 'Verificando…' : step === 'code' ? 'Verificar' : 'Entrar'}
        </Button>
      </form>

      {emailNotVerified ? (
        <div className="auth-notice" role="alert">
          <p>{error.message}</p>
          {resendState === 'sent' ? (
            <p className="subtle">
              Si el email existe, te enviamos un enlace nuevo. Revisá spam o promociones.
            </p>
          ) : (
            <div className="auth-notice__form">
              <Field id="resend-email" label="Tu email">
                <input
                  id="resend-email"
                  className="input"
                  type="email"
                  autoComplete="email"
                  value={resendEmail}
                  onChange={(event) => setResendEmail(event.target.value)}
                />
              </Field>
              <Button
                small
                onClick={resend}
                disabled={!resendEmail || resendState === 'sending'}
              >
                {resendState === 'sending' ? 'Enviando…' : 'Reenviar verificación'}
              </Button>
            </div>
          )}
        </div>
      ) : null}

      {step === 'credentials' ? (
        <div className="auth-links">
          <Link to="/forgot-password">Olvidé mi contraseña</Link>
          <span aria-hidden="true">·</span>
          <Link to="/register">Crear cuenta</Link>
        </div>
      ) : (
        <div className="auth-links">
          <button type="button" className="link-button" onClick={backToCredentials}>
            Volver al inicio de sesión
          </button>
        </div>
      )}
    </AuthLayout>
  );
}
