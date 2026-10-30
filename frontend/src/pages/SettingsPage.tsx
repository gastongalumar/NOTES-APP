import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/context/ToastContext';
import {
  changePassword,
  deleteAccount,
  requestEmailVerification,
  updateProfile,
} from '@/api/auth';
import {
  disableTwoFactor,
  setupTwoFactor,
  statusTwoFactor,
  verifyTwoFactor,
  type TwoFactorSetup,
  type TwoFactorStatus,
} from '@/api/twoFactor';
import { Button } from '@/components/ui/Button';
import { Field } from '@/components/ui/Field';
import { Dialog } from '@/components/ui/Dialog';
import { ThemeOptions } from '@/components/theme/ThemeOptions';

function messageOf(caught: unknown, fallback: string): string {
  return (caught as { message?: string }).message ?? fallback;
}

export function SettingsPage() {
  const { user, logout, refreshUser } = useAuth();
  const { push } = useToast();
  const navigate = useNavigate();
  const [profile, setProfile] = useState({ nombre: user?.nombre ?? '', email: user?.email ?? '' });
  const [passwords, setPasswords] = useState({ currentPassword: '', newPassword: '' });
  const [deletePassword, setDeletePassword] = useState('');
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [error, setError] = useState('');
  const [twoFactor, setTwoFactor] = useState<TwoFactorStatus | null>(null);
  const [setup, setSetup] = useState<TwoFactorSetup | null>(null);
  const [twoFactorCode, setTwoFactorCode] = useState('');
  const [recoveryCodes, setRecoveryCodes] = useState<string[]>([]);
  const [disableOpen, setDisableOpen] = useState(false);
  const [disablePassword, setDisablePassword] = useState('');

  useEffect(() => {
    statusTwoFactor()
      .then(setTwoFactor)
      .catch(() => setTwoFactor(null));
  }, []);

  const saveProfile = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      await updateProfile(profile);
      await refreshUser();
      push('Perfil actualizado', 'success');
    } catch (caught) {
      setError(messageOf(caught, 'No se pudo guardar'));
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
      setError(messageOf(caught, 'No se pudo cambiar la contraseña'));
    }
  };

  const requestVerification = async () => {
    try {
      await requestEmailVerification();
      push('Te enviamos un enlace de verificación', 'success');
    } catch (caught) {
      push(messageOf(caught, 'No se pudo enviar el enlace'), 'error');
    }
  };

  const startTwoFactor = async () => {
    setError('');
    setRecoveryCodes([]);
    try {
      const response = await setupTwoFactor();
      setSetup(response);
    } catch (caught) {
      setError(messageOf(caught, 'No se pudo iniciar 2FA'));
    }
  };

  const cancelTwoFactor = () => {
    setSetup(null);
    setTwoFactorCode('');
  };

  const confirmTwoFactor = async () => {
    setError('');
    try {
      const codes = await verifyTwoFactor(twoFactorCode.trim());
      setRecoveryCodes(codes);
      setSetup(null);
      setTwoFactorCode('');
      setTwoFactor({ enabled: true, recoveryCodesRemaining: codes.length });
      push('2FA activado. Guardá los códigos de recuperación.', 'success');
    } catch (caught) {
      setError(messageOf(caught, 'Código 2FA inválido'));
    }
  };

  const confirmDisable = async () => {
    setError('');
    try {
      await disableTwoFactor(disablePassword);
      setDisableOpen(false);
      setDisablePassword('');
      setTwoFactor({ enabled: false, recoveryCodesRemaining: 0 });
      push('2FA desactivado', 'success');
    } catch (caught) {
      push(messageOf(caught, 'No se pudo desactivar 2FA'), 'error');
    }
  };

  const copy = async (value: string, label: string) => {
    try {
      await navigator.clipboard.writeText(value);
      push(`${label} copiado`, 'success');
    } catch {
      push('No se pudo copiar al portapapeles', 'error');
    }
  };

  const downloadRecoveryCodes = () => {
    const blob = new Blob([recoveryCodes.join('\n')], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'notasapp-codigos-recuperacion.txt';
    link.click();
    URL.revokeObjectURL(url);
  };

  const removeAccount = async () => {
    try {
      await deleteAccount(deletePassword);
      await logout();
      navigate('/login', { replace: true });
    } catch (caught) {
      push(messageOf(caught, 'No se pudo eliminar la cuenta'), 'error');
    }
  };

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Cuenta</p>
          <h1>Configuración</h1>
        </div>
      </header>

      {error ? (
        <div className="error-banner" role="alert">
          {error}
        </div>
      ) : null}

      <div className="settings-layout">
        <section className="card">
          <header className="settings-card__head">
            <h2>Perfil</h2>
            <span className={`badge ${user?.emailVerified ? 'badge-success' : 'badge-danger'}`}>
              {user?.emailVerified ? 'Email verificado' : 'Email sin verificar'}
            </span>
          </header>
          <form className="auth-form" onSubmit={saveProfile}>
            <Field id="profile-name" label="Nombre">
              <input
                id="profile-name"
                className="input"
                value={profile.nombre}
                onChange={(event) => setProfile({ ...profile, nombre: event.target.value })}
              />
            </Field>
            <Field
              id="profile-email"
              label="Email"
              hint="Si lo cambiás, tenés que verificarlo de nuevo."
            >
              <input
                id="profile-email"
                className="input"
                type="email"
                value={profile.email}
                onChange={(event) => setProfile({ ...profile, email: event.target.value })}
              />
            </Field>
            <div className="settings-actions">
              <Button type="submit" variant="primary">
                Guardar perfil
              </Button>
              {!user?.emailVerified ? (
                <Button small onClick={requestVerification}>
                  Reenviar verificación
                </Button>
              ) : null}
            </div>
          </form>
        </section>

        <section className="card">
          <header className="settings-card__head">
            <h2>Autenticación en dos pasos</h2>
            <span className={`badge ${twoFactor?.enabled ? 'badge-success' : ''}`}>
              {twoFactor?.enabled ? 'Activa' : 'Inactiva'}
            </span>
          </header>

          {recoveryCodes.length > 0 ? (
            <div className="two-factor-recovery">
              <p className="muted">
                Guardá estos códigos ahora. Cada uno sirve una sola vez y no se vuelven a mostrar.
              </p>
              <ul className="recovery-grid">
                {recoveryCodes.map((code) => (
                  <li key={code}>{code}</li>
                ))}
              </ul>
              <div className="settings-actions">
                <Button small onClick={() => copy(recoveryCodes.join('\n'), 'Códigos')}>
                  Copiar códigos
                </Button>
                <Button small onClick={downloadRecoveryCodes}>
                  Descargar .txt
                </Button>
              </div>
            </div>
          ) : setup ? (
            <div className="two-factor-setup">
              <div className="qr-frame">
                <QRCodeSVG value={setup.otpauthUri} size={168} bgColor="transparent" fgColor="currentColor" />
              </div>
              <div className="two-factor-setup__body">
                <p className="muted">
                  Escaneá el código con Google Authenticator, 1Password o Authy. Si no podés,
                  cargá el secreto manualmente.
                </p>
                <div className="secret-row">
                  <code className="secret-box">{setup.secret}</code>
                  <Button small onClick={() => copy(setup.secret, 'Secreto')}>
                    Copiar
                  </Button>
                </div>
                <Field id="two-factor-code" label="Código de 6 dígitos">
                  <input
                    id="two-factor-code"
                    className="input input-code"
                    inputMode="numeric"
                    autoComplete="one-time-code"
                    value={twoFactorCode}
                    onChange={(event) => setTwoFactorCode(event.target.value)}
                    maxLength={6}
                  />
                </Field>
                <div className="settings-actions">
                  <Button
                    variant="primary"
                    onClick={confirmTwoFactor}
                    disabled={twoFactorCode.trim().length !== 6}
                  >
                    Confirmar y activar
                  </Button>
                  <Button variant="ghost" onClick={cancelTwoFactor}>
                    Cancelar
                  </Button>
                </div>
              </div>
            </div>
          ) : twoFactor?.enabled ? (
            <div className="two-factor-enabled">
              <p className="muted">
                Tu cuenta pide un código de tu app autenticadora al iniciar sesión.
                {twoFactor.recoveryCodesRemaining > 0
                  ? ` Te quedan ${twoFactor.recoveryCodesRemaining} códigos de recuperación.`
                  : ' No te quedan códigos de recuperación.'}
              </p>
              <div className="settings-actions">
                <Button variant="danger" onClick={() => setDisableOpen(true)}>
                  Desactivar 2FA
                </Button>
              </div>
            </div>
          ) : (
            <div className="two-factor-off">
              <p className="muted">
                Sumá una capa extra: además de tu contraseña, un código de 6 dígitos generado en
                tu teléfono.
              </p>
              <div className="settings-actions">
                <Button variant="primary" onClick={startTwoFactor}>
                  Activar 2FA
                </Button>
              </div>
            </div>
          )}
        </section>

        <section className="card">
          <header className="settings-card__head">
            <h2>Contraseña</h2>
          </header>
          <form className="auth-form" onSubmit={savePassword}>
            <Field id="current-password" label="Contraseña actual">
              <input
                id="current-password"
                className="input"
                type="password"
                autoComplete="current-password"
                value={passwords.currentPassword}
                onChange={(event) =>
                  setPasswords({ ...passwords, currentPassword: event.target.value })
                }
                required
              />
            </Field>
            <Field id="new-password" label="Nueva contraseña" hint="Mínimo 10 caracteres">
              <input
                id="new-password"
                className="input"
                type="password"
                autoComplete="new-password"
                value={passwords.newPassword}
                onChange={(event) =>
                  setPasswords({ ...passwords, newPassword: event.target.value })
                }
                required
              />
            </Field>
            <div className="settings-actions">
              <Button type="submit" variant="primary">
                Cambiar contraseña
              </Button>
            </div>
          </form>
        </section>

        <section className="card">
          <header className="settings-card__head">
            <h2>Tema</h2>
          </header>
          <ThemeOptions name="theme-settings" />
        </section>

        <section className="card">
          <header className="settings-card__head">
            <h2>Sesión y cuenta</h2>
            <span className="subtle">@{user?.username}</span>
          </header>
          <div className="settings-actions">
            <Button onClick={() => logout().then(() => navigate('/login', { replace: true }))}>
              Cerrar sesión
            </Button>
            <Button variant="danger" onClick={() => setConfirmDelete(true)}>
              Eliminar mi cuenta
            </Button>
          </div>
        </section>
      </div>

      <Dialog
        open={disableOpen}
        title="Desactivar 2FA"
        onClose={() => setDisableOpen(false)}
        footer={
          <>
            <Button onClick={() => setDisableOpen(false)}>Cancelar</Button>
            <Button variant="danger" onClick={confirmDisable} disabled={!disablePassword}>
              Desactivar
            </Button>
          </>
        }
      >
        <p>Confirmá tu contraseña para desactivar la autenticación en dos pasos.</p>
        <Field id="disable-password" label="Contraseña">
          <input
            id="disable-password"
            className="input"
            type="password"
            autoComplete="current-password"
            value={disablePassword}
            onChange={(event) => setDisablePassword(event.target.value)}
          />
        </Field>
      </Dialog>

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
        <p>La cuenta se desactiva y no podés iniciar sesión. Esta acción es irreversible.</p>
        <Field id="delete-password" label="Confirmá tu contraseña">
          <input
            id="delete-password"
            className="input"
            type="password"
            value={deletePassword}
            onChange={(event) => setDeletePassword(event.target.value)}
          />
        </Field>
      </Dialog>
    </>
  );
}
