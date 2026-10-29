# ADR-0006 — Tokens y dos temas

## Contexto

El CSS anterior duplicaba variables y colores, no tenía tema oscuro real y no respetaba el sistema de diseño.

## Decisión

- `frontend/src/styles/tokens.css` es la única fuente de color, espacio, tipografía, radios, sombras y motion.
- Dos temas completos: `light-neumorphic` y `dark-purple`, por reemplazo de tokens.
- `ThemeToggle` cicla `system/light/dark`; `index.html` aplica el tema antes del paint.
- Componentes usan solo `var(--...)`; no hex/rgba/px de diseño.

## Consecuencias

- Cambiar un tema no requiere tocar componentes.
- Los nuevos estados requieren tokens nuevos documentados.
- El contraste debe verificarse en ambos temas con axe/herramienta de contraste.
