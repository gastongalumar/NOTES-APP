# NotasApp frontend

React 18 + TypeScript strict + Vite 5 + React Router 6. CSS plano con tokens.

## Scripts

```bash
npm ci
npm run dev        # http://localhost:5173
npm run typecheck
npm run test:run
npm run build
```

## Estructura

```text
src/api/        cliente Axios, refresh queue, módulos por recurso
src/auth/       tokenStore (solo memoria)
src/components/ ui, layout, notas, adjuntos, routing
src/context/    AuthContext, ThemeContext, ToastContext
src/pages/      login, registro, notas, editor, ajustes, admin
src/styles/     tokens.css, global.css, app.css
src/types/      DTOs espejo del backend
```

## Auth

El access token vive en una variable de módulo (`src/auth/tokenStore.ts`), nunca en `localStorage`/`sessionStorage`. El refresh usa cookie `HttpOnly` y una cola single-flight; `BroadcastChannel` coordina pestañas. Un 401 de `/auth/*` no dispara refresh.

## Diseño

Antes de tocar UI, leer `../AGENTS.md`, `AGENTS.md` y `../docs/DESIGN_DIRECTION.md`. Solo se usan tokens `var(--…)` de `src/styles/tokens.css`; no se hardcodean colores, espacios, tipografía, sombras, radios ni duraciones. Los temas `light-neumorphic` y `dark-purple` se cambian por reemplazo de tokens.

## Tests

Vitest + Testing Library. `npm run test:run` debe quedar verde; `npm run typecheck` y `npm run build` son parte del gate.

E2E con Playwright (requiere el compose levantado y el seed dev):

```bash
npx playwright install chromium
E2E_BASE_URL=http://localhost:3000 npm run test:e2e
```
