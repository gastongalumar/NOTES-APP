import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { LoginPage } from './LoginPage';

const { loginMock, resendMock } = vi.hoisted(() => ({
  loginMock: vi.fn(),
  resendMock: vi.fn(),
}));

vi.mock('@/context/AuthContext', () => ({
  useAuth: () => ({ login: loginMock }),
}));

vi.mock('@/api/auth', () => ({
  resendEmailVerification: resendMock,
}));

function renderLogin() {
  render(
    <MemoryRouter initialEntries={['/login']}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<div>Inicio</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  afterEach(() => {
    loginMock.mockReset();
    resendMock.mockReset();
  });

  it('pide el código 2FA en un segundo paso', async () => {
    const user = userEvent.setup();
    loginMock
      .mockRejectedValueOnce({ status: 401, code: 'MFA_REQUIRED', message: 'Ingresá el código' })
      .mockResolvedValueOnce(undefined);

    renderLogin();
    await user.type(screen.getByLabelText('Usuario'), 'gaston');
    await user.type(screen.getByLabelText('Contraseña'), 'secreta123');
    await user.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(
      await screen.findByRole('heading', { name: 'Verificación en dos pasos' }),
    ).toBeInTheDocument();

    await user.type(screen.getByLabelText('Código'), '123456');
    await user.click(screen.getByRole('button', { name: 'Verificar' }));

    expect(loginMock).toHaveBeenLastCalledWith('gaston', 'secreta123', '123456');
    expect(await screen.findByText('Inicio')).toBeInTheDocument();
  });

  it('ofrece reenviar la verificación si el email no está verificado', async () => {
    const user = userEvent.setup();
    loginMock.mockRejectedValueOnce({
      status: 403,
      code: 'EMAIL_NOT_VERIFIED',
      message: 'Verificá tu email antes de iniciar sesión',
      email: 'gaston@example.com',
    });
    resendMock.mockResolvedValueOnce(undefined);

    renderLogin();
    await user.type(screen.getByLabelText('Usuario'), 'gaston');
    await user.type(screen.getByLabelText('Contraseña'), 'secreta123');
    await user.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(await screen.findByLabelText('Tu email')).toHaveValue('gaston@example.com');
    await user.click(screen.getByRole('button', { name: 'Reenviar verificación' }));

    expect(resendMock).toHaveBeenCalledWith('gaston@example.com');
    expect(await screen.findByText(/Si el email existe/)).toBeInTheDocument();
  });
});
