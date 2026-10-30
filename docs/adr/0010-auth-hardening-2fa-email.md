# ADR-0010 — Endurecimiento de 2FA y verificación de email

## Contexto

En producción se detectaron problemas de producto y seguridad:

- Volver a pulsar “Activar 2FA” reemplazaba el secreto y dejaba `enabled_at` en null: desactivaba 2FA sin avisar.
- Los códigos de recuperación se podían reutilizar indefinidamente.
- No había forma de consultar el estado ni de desactivar 2FA desde la UI.
- Un usuario sin email verificado no podía pedir un reenvío: el endpoint exigía sesión y el login estaba bloqueado.
- Cambiar el email en el perfil no reiniciaba `emailVerified`.
- `docker-compose.yml` fijaba `MAIL_HOST: mailhog` y `FRONTEND_URL: http://localhost:3000`, ignorando el `.env` de la VM.

## Decisión

### 2FA

- El login distingue `MFA_REQUIRED` (401) de credenciales inválidas. El frontend muestra el campo de código en un segundo paso, sin recargar la página.
- `POST /auth/2fa/setup` devuelve `409 TWO_FACTOR_ALREADY_ENABLED` si ya está activo. Ya no se puede resetear por accidente.
- `GET /auth/2fa/status` informa si está activo y cuántos códigos de recuperación quedan.
- `POST /auth/2fa/disable` exige la contraseña actual y audita `TWO_FACTOR_DISABLED`.
- Los códigos de recuperación se consumen al usarse y se audita `TWO_FACTOR_RECOVERY_USED`.
- El setup devuelve `otpauthUri` y el frontend lo renderiza como QR con `qrcode.react`. Es la única dependencia nueva: un QR generado en el cliente evita mandar el secreto a un servicio externo.

### Email

- `POST /auth/email/resend` es público, responde siempre `202` y está limitado a 3 solicitudes por hora por IP. No revela si el email existe.
- La pantalla de login detecta `EMAIL_NOT_VERIFIED`, recibe el email en el `ProblemDetail` y ofrece reenviar la verificación.
- Cambiar el email en el perfil marca `emailVerified = false` y dispara un envío nuevo.
- `docker-compose.yml` ahora respeta `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM`, `FRONTEND_URL` y `RESEND_API_KEY` del entorno.

### Clave de cifrado

- `APP_ENCRYPTION_KEY` es opcional. Si está vacía, los secretos TOTP se derivan de `JWT_SECRET` (comportamiento anterior).
- Si se define, se usa una clave dedicada. **Cambiar `APP_ENCRYPTION_KEY` o `JWT_SECRET` invalida los 2FA existentes**: hay que desactivar y volver a enrolar antes del cambio.

## Consecuencias

- Un atacante con la contraseña correcta puede saber si la cuenta tiene 2FA (`MFA_REQUIRED`), pero no puede entrar sin el segundo factor.
- El reenvío público no permite enumerar cuentas y tiene rate limit.
- Los códigos de recuperación son de un solo uso; si se agotan, la única salida es un administrador o desactivar 2FA con contraseña (si aún se tiene acceso).
- La rotación de claves de cifrado es una operación de mantenimiento, no un simple cambio de variable.

## Referencias

- `backend/src/main/java/com/notasapp/service/impl/TwoFactorServiceImpl.java`
- `backend/src/main/java/com/notasapp/service/impl/EmailVerificationServiceImpl.java`
- `frontend/src/pages/LoginPage.tsx`
- `frontend/src/pages/SettingsPage.tsx`
- `docker-compose.yml`
