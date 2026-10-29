# Dirección de diseño — NotasApp

## Tesis

NotasApp es una herramienta de trabajo personal, no una landing. La interfaz debe sentirse como un **archivador editorial**: superficies de papel cálido, tinta oscura, un acento rojo usado como señal y tipografía con jerarquía clara. El neumorfismo queda limitado a controles y superficies táctiles; el resto se resuelve con composición, hairlines y espacio.

La app debe transmitir **control y privacidad**: nada de métricas inventadas, decoración vacía ni animaciones que distraigan.

## Principios

1. **Contenido primero:** la nota y la fecha son el centro; el chrome desaparece.
2. **Densidad legible:** listas compactas en desktop, tarjetas apiladas en mobile.
3. **Un solo acento:** el rojo señala acción primaria, foco y peligro; no se usa como decoración.
4. **Superficies con propósito:** elevación solo para lo interactivo o lo que se superpone.
5. **Movimiento sobrio:** transiciones cortas, sin autoplay, respetando `prefers-reduced-motion`.

## Paletas

### `light-neumorphic`

- Fondo: papel cálido (`--color-bg`).
- Superficie: papel elevado (`--color-surface`, `--color-surface-raised`).
- Texto: tinta (`--color-text`, `--color-text-muted`).
- Acento: rojo profundo (`--color-accent`).
- Bordes: hairlines cálidos (`--color-border`).
- Sombras: dobles, claras y oscuras, de bajo contraste.

### `dark-purple`

- Fondo: casi negro con matiz violeta (`--color-bg`).
- Superficies: violeta profundo (`--color-surface`, `--color-surface-raised`).
- Texto: marfil (`--color-text`).
- Acento: coral (`--color-accent`).
- Sombras: profundas, sin invertir los colores del tema claro.

Ambos temas se implementan **reemplazando tokens** en `[data-theme]`; no hay CSS condicional por tema.

## Tipografía

- Sans: `--font-sans` (Inter / system UI).
- Mono: `--font-mono` (JetBrains Mono / system mono) para fechas, badges y metadatos.
- Escala: `--text-xs` a `--text-3xl`; títulos con `--leading-tight` y `--tracking-tight`.
- No se cargan fuentes externas por CDN; se usa la pila del sistema o archivos self-hosted.

## Espaciado y layout

- Escala de `--space-1` a `--space-16`.
- Contenedor `--container` y `--container-narrow`.
- Sidebar fija en desktop (`--nav-width`), stack vertical en mobile.
- Breakpoints: CSS no permite usar custom properties dentro de `@media`, así que los dos cortes (`900px`, `640px`) son una excepción documentada. Se revisan en 320, 768, 1024 y 1440 px.

## Componentes

- **Button:** default, primary, ghost, danger; altura mínima `--space-10`.
- **Input/Textarea/Select:** fondo hundido, borde hairline, foco visible.
- **Card:** superficie elevada, borde y sombra suave; hover solo en tarjetas clicables.
- **Badge:** metadatos y estados; nunca como único indicador de error.
- **Dialog:** `role="dialog"`, `aria-modal`, focus trap, Escape y retorno de foco.
- **Toast:** `aria-live="polite"`; no reemplaza errores de formulario inline.
- **EmptyState:** explica qué falta y cuál es la acción siguiente.

## Motion

- Duraciones `--dur-fast`, `--dur-base`, `--dur-slow`; easing `--ease-out`.
- Sin animaciones de entrada en cada render.
- `prefers-reduced-motion` y `[data-motion="off"]` desactivan transiciones y animaciones.

## Accesibilidad

- Objetivo AA: 4.5:1 texto normal, 3:1 texto grande/UI.
- Verificar contraste con una herramienta (axe/contrast checker) en ambos temas; no se documentan ratios inventados.
- `:focus-visible` global, navegación por teclado, labels asociados y `aria-describedby` para errores.
- Landmarks (`nav`, `main`), skip link y jerarquía de encabezados correcta.
- Formularios con `aria-invalid` cuando hay error.

## Gobernanza de tokens

1. Un valor nuevo entra primero como global, luego semántico, luego componente.
2. Ningún componente usa hex/rgba/px de diseño directamente.
3. Si un componente necesita un estado nuevo, se agrega el token semántico y se documenta acá.
4. Los cambios de tokens se revisan en ambos temas y en mobile/desktop.

## Estados de producto

- **Vacío:** explicar y ofrecer acción.
- **Cargando:** skeleton o texto breve; no spinner infinito sin contexto.
- **Error:** mensaje humano, acción de reintento y sin datos falsos.
- **Sin permisos:** explicar el rol requerido, no redirigir en silencio.
