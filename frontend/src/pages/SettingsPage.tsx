import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/context/ToastContext';
import { changePassword, deleteAccount, requestEmailVerification, updateProfile } from '@/api/auth';
import { setupTwoFactor, verifyTwoFactor } from '@/api/twoFactor';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import { ThemeToggle } from '@/components/ThemeToggle';
import { Dialog } from '@/components/ui/Dialog';

export function SettingsPage() {
  const { user, logout, refreshUser } = useAuth();
  const { push } = useToast();
  const navigate = useNavigate();
  const [profile, setProfile] = useState({ nombre: user?.nombre ?? '', email: user?.email ?? '' });
  const [passwords, setPasswords] = useState({ currentPassword: '', newPassword: '' });
  const [deletePassword, setDeletePassword] = useState('');
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [error, setError] = useState('');
  const [twoFactorSecret, setTwoFactorSecret] = useState('');
  const [twoFactorCode, setTwoFactorCode] = useState('');
  const [recoveryCodes, setRecoveryCodes] = useState<string[]>([]);

  const saveProfile = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      await updateProfile(profile);
      await refreshUser();
      push('Perfil actualizado', 'success');
    } catch (caught) {
      setError((caught as { message?: string }).message ?? 'No se pudo guardar');
    }
  };

  const savePassword = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      await changePassword(passwords);
      setPasswords({ currentPassword: '', newPassword: '' });
      push('Contraseña actualizada. Se cerraron las otras sesiones.', 'success');
    } catch (caught) {
      setError((caught as { message?: string }).message ?? 'No se pudo cambiar la contraseña');
    }
  };

  const requestVerification = async () => {
    try {
      await requestEmailVerification();
      push('Te enviamos un enlace de verificación', 'success');
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo enviar el enlace', 'error');
    }
  };

  const startTwoFactor = async () => {
    setError('');
    try {
      const setup = await setupTwoFactor();
      setTwoFactorSecret(setup.secret);
      setRecoveryCodes([]);
    } catch (caught) {
      setError((caught as { message?: string }).message ?? 'No se pudo iniciar 2FA');
    }
  };

  const confirmTwoFactor = async () => {
    setError('');
    try {
      const codes = await verifyTwoFactor(twoFactorCode);
      setRecoveryCodes(codes);
      setTwoFactorCode('');
      push('2FA activado. Guardá los códigos de recuperación.', 'success');
    } catch (caught) {
      setError((caught as { message?: string }).message ?? 'Código 2FA inválido');
    }
  };

  const removeAccount = async () => {
    try {
      await deleteAccount(deletePassword);
      await logout();
      navigate('/login', { replace: true });
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo eliminar la cuenta', 'error');
    }
  };

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Cuenta</p>
          <h1>Configuración</h1>
        </div>
        <ThemeToggle />
      </header>

      {error ? <div className="error-banner" role="alert">{error}</div> : null}

      <section className="card" style={{ marginBottom: 'var(--space-6)' }}>
        <h2>Perfil</h2>
        <form className="auth-form" onSubmit={saveProfile}>
          <Field id="profile-name" label="Nombre">
            <input id="profile-name" className="input" value={profile.nombre} onChange={(e) => setProfile({ ...profile, nombre: e.target.value })} />
          </Field>
          <Field id="profile-email" label="Email">
            <input id="profile-email" className="input" type="email" value={profile.email} onChange={(e) => setProfile({ ...profile, email: e.target.value })} />
          </Field>
          <Button type="submit" variant="primary">Guardar perfil</Button>
        </form>
        {!user?.emailVerified ? (
          <div className="toolbar" style={{ marginTop: 'var(--space-4)', marginBottom: 0 }}>
            <span className="subtle">Tu email todavía no está verificado.</span>
            <Button small onClick={requestVerification}>Verificar email</Button>
          </div>
        ) : (
          <p className="subtle" style={{ marginTop: 'var(--space-3)' }}>Email verificado.</p>
        )}
      </section>

      <section className="card" style={{ marginBottom: 'var(--space-6)' }}>
        <h2>Contraseña</h2>
        <form className="auth-form" onSubmit={savePassword}>
          <Field id="current-password" label="Contraseña actual">
            <input id="current-password" className="input" type="password" autoComplete="current-password" value={passwords.currentPassword} onChange={(e) => setPasswords({ ...passwords, currentPassword: e.target.value })} required />
          </Field>
          <Field id="new-password" label="Nueva contraseña" hint="Mínimo 10 caracteres">
            <input id="new-password" className="input" type="password" autoComplete="new-password" value={passwords.newPassword} onChange={(e) => setPasswords({ ...passwords, newPassword: e.target.value })} required />
          </Field>
          <Button type="submit" variant="primary">Cambiar contraseña</Button>
        </form>
      </section>

      <section className="card" style={{ marginBottom: 'var(--space-6)' }}>
        <h2>Autenticación en dos pasos</h2>
        {recoveryCodes.length > 0 ? (
          <>
            <p className="muted">Guardá estos códigos de recuperación. No se vuelven a mostrar.</p>
            <div className="tag-list">
              {recoveryCodes.map((code) => (
                <span className="badge" key={code}>{code}</span>
              ))}
            </div>
          </>
        ) : twoFactorSecret ? (
          <>
            <p className="muted">Cargá este secreto en tu app autenticadora y confirmá el código.</p>
            <p className="badge">{twoFactorSecret}</p>
            <Field id="two-factor-code" label="Código de 6 dígitos">
              <input id="two-factor-code" className="input" inputMode="numeric" value={twoFactorCode} onChange={(e) => setTwoFactorCode(e.target.value)} />
            </Field>
            <Button variant="primary" onClick={confirmTwoFactor} disabled={twoFactorCode.length !== 6}>
              Confirmar 2FA
            </Button>
          </>
        ) : (
          <Button onClick={startTwoFactor}>Activar 2FA</Button>
        )}
      </section>

      <section className="card">
        <h2>Sesión y cuenta</h2>
        <p className="muted">Usuario: {user?.username}</p>
        <div className="toolbar">
          <Button onClick={() => logout().then(() => navigate('/login', { replace: true }))}>Cerrar sesión</Button>
          <Button variant="danger" onClick={() => setConfirmDelete(true)}>Eliminar mi cuenta</Button>
        </div>
      </section>

      <Dialog
        open={confirmDelete}
        title="Eliminar cuenta"
        onClose={() => setConfirmDelete(false)}
        footer={
          <>
            <Button onClick={() => setConfirmDelete(false)}>Cancelar</Button>
            <Button variant="danger" onClick={removeAccount} disabled={!deletePassword}>
              Eliminar cuenta
            </Button>
          </>
        }
      >
        <p>La cuenta se desactivará y no podrá iniciar sesión. Esta acción es irreversible desde la UI.</p>
        <Field id="delete-password" label="Confirmá tu contraseña">
          <input id="delete-password" className="input" type="password" value={deletePassword} onChange={(e) => setDeletePassword(e.target.value)} />
        </Field>
      </Dialog>
    </>
  );
}
