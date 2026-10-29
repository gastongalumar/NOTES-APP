# ADR-0002 — Access en memoria + refresh rotativo

## Contexto

El token en `localStorage` es exfiltrable por XSS y no es revocable. Un JWT de 24 h no permite cerrar sesión en servidor.

## Decisión

- Access JWT HS256 de 15 min, guardado solo en una variable de módulo del frontend.
- Refresh token opaco de 256 bits en cookie `HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/`.
- En DB solo se guarda SHA-256 del refresh; rotación por familia y detección de reuso.
- Máximo 5 familias activas por usuario; la más antigua se revoca al crear la sexta.
- Logout revoca la familia y borra la cookie.

## Consecuencias

- XSS no obtiene un token persistente.
- El refresh es el único estado de sesión; hay que protegerlo con CSRF/SameSite.
- La rotación exige coordinación single-flight en el cliente.
