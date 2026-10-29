# ADR-0008 — CSRF y SameSite

## Contexto

El refresh viaja en cookie. Si frontend y API están en sitios distintos, `SameSite=Strict` no se envía y se necesita un token CSRF. Si comparten dominio registrable, la cookie same-site es suficiente junto con validación de `Origin`.

## Decisión

- Frontend `app.notasapp.com` y API `api.notasapp.com` comparten dominio registrable.
- Refresh y logout usan `SameSite=Strict`, `HttpOnly`, `Secure` y validación de `Origin`.
- **No se emite CSRF token** en v1.
- `BE-10b` (double-submit CSRF) queda documentada como condicional y **no se ejecuta**.

## Condición de revisión

Si el frontend o la API cambian a otro dominio registrable, o se requiere `SameSite=None`, se debe activar `BE-10b` antes de desplegar.

## Consecuencias

- Menos complejidad en el cliente.
- La seguridad depende de mantener el mismo sitio y una allowlist exacta de CORS.
- Cualquier cambio de dominio es un cambio de seguridad, no solo de infraestructura.
