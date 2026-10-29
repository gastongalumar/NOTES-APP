# AGENTS.md — NotasApp frontend

## Stack

React 18 + TypeScript strict + Vite 5 + React Router 6 + CSS plano.
Prohibido CRA, JavaScript sin tipos, CSS-in-JS y frameworks CSS.

## Antes de tocar UI

1. Leer `../docs/DESIGN_DIRECTION.md`.
2. Usar solo tokens de `src/styles/tokens.css`.
3. Si falta un token, proponerlo y documentarlo primero.
4. Revisar `src/components/ui` antes de crear un componente nuevo.

## Estructura y capas

`api/` no importa `components`, `pages` ni `context`.
`components/ui/` no importa `api` ni `pages`; recibe props y callbacks.
`pages/` orquesta; `hooks/` encapsula datos; `context/` solo estado global.
Alias `@/` para `src/`; evitar imports relativos profundos.

## Auth

- Access token solo en memoria (`src/auth/tokenStore.ts`).
- Nunca `localStorage`/`sessionStorage` para tokens.
- Refresh por cookie `HttpOnly`; el cliente hace single-flight y coordinación por `BroadcastChannel`.
- Un 401 de `/auth/*` no dispara refresh.
- Logout revoca en servidor y limpia memoria.

## Datos y errores

- Toda llamada pasa por `api/http.ts`; los componentes no llaman `axios` directo.
- Errores normalizados a `ApiError { status, code, message, fieldErrors }`.
- Prohibido inventar datos de fallback, `console.log` en producción y tragar errores.
- Paginación, filtros y búsqueda se resuelven en servidor, no en memoria.

## Diseño

- Dos temas: `light-neumorphic` y `dark-purple`; sin hex/rgba/px de diseño fuera de tokens.
- AA mínimo; `:focus-visible`; `prefers-reduced-motion`; sin `!important`.
- Diálogos con focus trap, `aria-modal` y Escape; toasts con `aria-live`.

## Testing

Vitest + Testing Library + MSW. Un test por flujo crítico.
`npm run typecheck`, `npm run test:run` y `npm run build` deben quedar limpios.
