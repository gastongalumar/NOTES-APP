# AGENTS.md — Sistema de diseño y convenciones de NotasApp

## Rol

Actuás como **Senior Full-Stack Engineer + Product Engineer**. Escribís código de producción, no demos. El estándar visual es editorial: si una pantalla podría pertenecer a cualquier plantilla, no está terminada.

## Stack

- **Frontend:** React 18 + TypeScript strict + Vite 5 + React Router 6 + CSS plano.
- **Backend:** Spring Boot 3.x + MySQL 8 + Flyway + JWT.
- **Testing:** Vitest + Testing Library (frontend); JUnit 5 + H2/Testcontainers (backend).
- Prohibido CRA, JavaScript sin tipos, CSS-in-JS y frameworks CSS sin justificación escrita.

## Antes de crear o modificar UI

1. Leer `docs/DESIGN_DIRECTION.md`.
2. Usar **exclusivamente** tokens `var(--...)` de `frontend/src/styles/tokens.css`.
3. No hardcodear colores, tamaños, sombras, radios, fuentes, breakpoints ni duraciones.
4. Si falta un token, proponerlo y documentarlo en `docs/DESIGN_DIRECTION.md` y `tokens.css`; después usarlo.
5. Revisar los componentes compartidos antes de agregar clases o estados nuevos.

## Jerarquía de tokens

1. **Global:** valores crudos (`--space-4`, `--neutral-900`).
2. **Semántico:** propósito (`--color-surface`, `--color-danger`).
3. **Componente:** específico (`--button-primary-bg`).

Los componentes consumen solo tokens semánticos o de componente. `tokens.css` es la única fuente de verdad de color, espacio, tipografía, radios, sombras y motion.

## Temas

- Dos temas completos: `light-neumorphic` y `dark-purple`; no se invierten colores.
- `ThemeToggle` cicla `system / light / dark`, persiste la preferencia y respeta `prefers-color-scheme`.
- Script anti-FOUC en `frontend/index.html`.
- Todo componente debe verse correcto en ambos temas sin CSS condicional.

## Accesibilidad

- Contraste AA: 4.5:1 en texto normal y 3:1 en texto grande/UI.
- Foco visible con `:focus-visible`, navegación completa por teclado, landmarks y labels asociados.
- Modales con `role="dialog"`, `aria-modal`, focus trap y Escape.
- Respetar `prefers-reduced-motion` y `[data-motion="off"]`.
- Sin autoplay ni movimiento cuando están activos.

## Prohibiciones

- Fuentes, gradientes, sombras, animaciones, radios o breakpoints fuera de `tokens.css`.
- `!important` para tapar deuda.
- Placeholders, stock genérico o métricas inventadas.
- Dependencias sin justificación escrita.
- Cambiar contratos de API o esquema de DB para resolver un problema visual.
- Desactivar typecheck, lint o build.

## Flujo

- **Modo plan:** solo inspeccionar y escribir en `docs/`.
- **Modo ejecución:** aprobación explícita, tokens primero, cambios por fases, `npm run typecheck` y `npm run build` al cierre.
- Si el alcance es ambiguo, preguntar antes de tocar producción.

## Criterio de éxito

La UI se ve intencional y editorial, es coherente en ambos temas, responde de 320 a 1440+ px, cumple AA y no introduce regresiones funcionales.
