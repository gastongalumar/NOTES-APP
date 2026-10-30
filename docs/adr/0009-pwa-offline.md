# ADR-0009 — PWA instalable y offline básico

## Contexto

NotasApp se usa como agenda personal, también desde el teléfono. El plan no incluía PWA, pero la app ya es una SPA con React Router y no tenía forma de instalarse ni de abrir sin red.

Restricciones relevantes:

- El access token vive en memoria; el refresh va en cookie `HttpOnly`.
- Las notas son privadas y el mismo dispositivo puede ser compartido.
- No se quería introducir caché de datos de negocio sin cifrado ni separación por usuario.

## Decisión

- Se usa `vite-plugin-pwa` con Workbox en modo `generateSW`.
- El service worker precachea **solo el app shell**: HTML, JS, CSS, iconos y fuentes self-hosted.
- `navigateFallback: /index.html` permite abrir cualquier ruta del SPA offline; `/api/**` queda en `navigateFallbackDenylist`.
- **No se cachean respuestas de `/api`**: ni notas, ni adjuntos, ni sesión. Sin conexión la app abre y muestra el estado de error de cada pantalla, no datos viejos de otro usuario.
- El registro usa `registerType: 'prompt'`: la actualización se avisa en pantalla y se aplica cuando la persona confirma. Se busca una versión nueva cada hora y al registrar.
- El manifest se genera desde `vite.config.ts` con iconos propios (192, 512 y maskable), `display: standalone` y shortcuts a “Nueva nota” y “Hoy”.
- `theme-color` se sincroniza desde el token `--color-bg` del tema activo, no desde un valor fijo por tema.
- `nginx.conf` sirve `sw.js`, `manifest.webmanifest` e `index.html` con `expires -1` y cachea los assets con hash durante un año.

## Consecuencias

- La app es instalable en Chrome/Edge/Android y en iOS mediante “Añadir a inicio”.
- El offline es de disponibilidad, no de datos: se puede abrir la app, pero crear, editar o consultar requiere conexión.
- Una versión nueva no reemplaza la sesión en curso sin confirmación.
- El botón “Instalar app” solo aparece cuando el navegador emite `beforeinstallprompt`; en iOS no se muestra y se documenta el flujo manual.

## Condición de revisión

Si se quiere lectura offline de notas, la solución no es cachear respuestas autenticadas en Cache Storage. Habría que evaluar almacenamiento local cifrado, con clave derivada de la sesión, expiración y borrado en logout.

## Referencias

- `frontend/vite.config.ts`
- `frontend/src/pwa/register.ts`
- `frontend/src/pwa/updates.ts`
- `frontend/src/components/pwa/`
- `frontend/nginx.conf`
