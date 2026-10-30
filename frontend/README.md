# NotasApp frontend

React 18 + TypeScript strict + Vite 5 + React Router 6. CSS plano con tokens.

## Scripts

```bash
npm ci
npm run dev        # http://localhost:5173
npm run typecheck
npm run test:run
npm run build
npm run preview    # build + service worker en http://localhost:4173
```

## Estructura

```text
src/api/        cliente Axios, refresh queue, módulos por recurso
src/auth/       tokenStore (solo memoria)
src/components/ ui, layout, notas, adjuntos, routing
src/context/    AuthContext, ThemeContext, ToastContext
src/pages/      login, registro, notas, editor, ajustes, admin
src/pwa/        registro del service worker y estado de actualización
src/styles/     tokens.css, global.css, app.css
src/types/      DTOs espejo del backend
public/icons/   iconos de la PWA (192, 512, maskable, apple-touch)
```

## Auth

El access token vive en una variable de módulo (`src/auth/tokenStore.ts`), nunca en `localStorage`/`sessionStorage`. El refresh usa cookie `HttpOnly` y una cola single-flight; `BroadcastChannel` coordina pestañas. Un 401 de `/auth/*` no dispara refresh.

El login es en dos pasos: primero usuario y contraseña; si la cuenta tiene 2FA, el backend responde `MFA_REQUIRED` y la UI muestra el campo de código sin recargar. Si el email no está verificado, ofrece reenviar el enlace.

## Diseño

Antes de tocar UI, leer `../AGENTS.md`, `AGENTS.md` y `../docs/DESIGN_DIRECTION.md`. Solo se usan tokens `var(--…)` de `src/styles/tokens.css`; no se hardcodean colores, espacios, tipografía, sombras, radios ni duraciones.

## Temas

Cuatro paletas completas: `light-cobalt` y `dark-aurora` (nuevas, default de `system`) y `light-neumorphic` / `dark-purple` (identidad anterior). El `ThemePicker` está en la sidebar, en la hoja “Más” y en Configuración; persiste en `notasapp.theme` y el script anti-FOUC aplica el tema antes del primer render.

## Navegación

Desktop: sidebar agrupada en Espacio / Organizar / Administración. Mobile: barra superior con marca y notificaciones, barra inferior con Inicio, Notas, Agenda, Calendario y “Más”. La hoja “Más” concentra el resto de secciones, el tema, el usuario y el cierre de sesión.

## PWA

El build genera `sw.js`, `manifest.webmanifest` e iconos instalables. El service worker no se registra en `npm run dev`: hay que usar `npm run preview`.

- Offline: se precachea solo el app shell; `/api/**` nunca se cachea.
- Actualización: aviso en pantalla, sin reemplazar la sesión sin confirmación.
- Detalle y límites en `../docs/adr/0009-pwa-offline.md`.

## Tests

Vitest + Testing Library. `npm run test:run` debe quedar verde; `npm run typecheck` y `npm run build` son parte del gate.

E2E con Playwright (requiere el compose levantado y el seed dev):

```bash
npx playwright install chromium
E2E_BASE_URL=http://localhost:3000 npm run test:e2e
```

`e2e/pwa.spec.ts` no necesita backend: valida manifest, registro del service worker y navegación offline contra el build.

```bash
npm run build
npm run preview -- --port 4173
E2E_BASE_URL=http://localhost:4173 npx playwright test e2e/pwa.spec.ts
```
