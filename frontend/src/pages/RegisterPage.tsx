import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import { AuthLayout } from '@/components/auth/AuthLayout';
import type { ApiError } from '@/types/api';

export function RegisterPage() {
  const { register } = useAuth();
  const { push } = useToast();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', nombre: '', password: '' });
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(false);

  const update = (field: keyof typeof form, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    if (form.password.length < 10) {
      setError({ status: 400, code: 'VALIDATION_ERROR', message: 'La contraseña debe tener al menos 10 caracteres' });
      return;
    }
    if (!/[a-z]/.test(form.password) || !/[A-Z]/.test(form.password) || !/\d/.test(form.password)) {
      setError({ status: 400, code: 'VALIDATION_ERROR', message: 'La contraseña debe incluir mayúscula, minúscula y número' });
      return;
    }
    setLoading(true);
    try {
      await register(form);
      push('Cuenta creada. Revisá tu email para verificarla.', 'success');
      navigate('/login', { replace: true });
    } catch (caught) {
      setError(caught as ApiError);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Crear cuenta"
      subtitle="Tus notas quedan bajo tu control."
      footer={
        <span>
          ¿Ya tenés cuenta? <Link to="/login">Entrar</Link>
        </span>
      }
    >
      <form className="auth-form" onSubmit={submit}>
        {error ? (
          <div className="error-banner" role="alert">
            {error.message}
          </div>
        ) : null}
        <Field id="nombre" label="Nombre" hint="Opcional, para mostrarlo en la app.">
          <input
            id="nombre"
            className="input"
            autoComplete="name"
            value={form.nombre}
            onChange={(event) => update('nombre', event.target.value)}
          />
        </Field>
        <Field id="username" label="Usuario">
          <input
            id="username"
            className="input"
            autoComplete="username"
            value={form.username}
            onChange={(event) => update('username', event.target.value)}
            required
          />
        </Field>
        <Field id="email" label="Email" hint="Te vamos a enviar un enlace para verificarlo.">
          <input
            id="email"
            className="input"
            type="email"
            autoComplete="email"
            value={form.email}
            onChange={(event) => update('email', event.target.value)}
            required
          />
        </Field>
        <Field
          id="password"
          label="Contraseña"
          hint="Mínimo 10 caracteres, con mayúscula, minúscula y número."
        >
          <input
            id="password"
            className="input"
            type="password"
            autoComplete="new-password"
            value={form.password}
            onChange={(event) => update('password', event.target.value)}
            required
          />
        </Field>
        <Button type="submit" variant="primary" disabled={loading}>
          {loading ? 'Creando…' : 'Crear cuenta'}
        </Button>
      </form>
    </AuthLayout>
  );
}
