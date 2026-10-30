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

### `light-cobalt`

- Fondo: papel frío gris azulado (`--color-bg`).
- Superficies: blancas con bordes hairline (`--color-surface-raised`).
- Texto: azul casi negro (`--color-text`).
- Acento: cobalto profundo (`--color-accent`).
- Sombras: bajas y difusas, sin neumorfismo marcado.

### `dark-aurora`

- Fondo: negro violáceo (`--color-bg`).
- Superficies: violeta profundo (`--color-surface`, `--color-surface-raised`).
- Texto: lavanda claro (`--color-text`).
- Acento: lavanda (`--color-accent`), con contraste oscuro para botones.
- Sombras: profundas y de bajo contraste.

`system` resuelve a `light-cobalt` o `dark-aurora`; `Papel` y `Tinta` quedan disponibles para quien prefiera la identidad anterior. El selector `ThemePicker` muestra las cinco opciones con un swatch de cada paleta y persiste la elección en `notasapp.theme`.

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
- **OfflineBanner:** aviso fijo y no bloqueante cuando `navigator.onLine` es falso; `role="status"`.
- **PwaUpdatePrompt:** aviso de versión nueva con “Actualizar” y “Ahora no”; nunca actualiza sin confirmación.
- **InstallPrompt:** botón “Instalar app” solo cuando el navegador emite `beforeinstallprompt`; no se simula en iOS.
- **AuthLayout:** dos columnas en desktop (marca + formulario) y una sola en móvil; el lockup de marca se muestra una vez por viewport. La foto `fondo-notas` cubre el shell completo y la tarjeta del formulario flota sobre ella con superficie propia (también en móvil). El texto de marca usa los tokens `--auth-brand-*` con tinta oscura en todos los temas, porque la foto es clara; el velo `--auth-photo-veil` es neutro y bajo. La foto es decorativa (sin texto alternativo) y cada texto conserva AA.
- **ThemePicker:** botón compacto con panel de radios nativos; cinco opciones (Sistema, Cobalto, Aurora, Papel, Tinta). En móvil se usa la misma lista dentro de la hoja “Más”.
- **Navegación móvil:** barra inferior fija con cinco destinos (Inicio, Notas, Agenda, Calendario, Más). “Más” abre una hoja con el resto de secciones, tema, usuario y cierre de sesión. En desktop se mantiene la sidebar agrupada por Espacio / Organizar / Administración.
- **2FA:** el secreto se ofrece como QR (`qrcode.react`) y como texto copiable; los códigos de recuperación se muestran una sola vez.
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
5. `--z-banner` (150) ordena los avisos de PWA por encima del contenido y por debajo de overlays y toasts.
6. `--space-14` (56px) define la altura de la barra superior móvil; `--qr-bg`/`--qr-fg` garantizan que el QR de 2FA se pueda escanear en ambos temas.
7. El manifest no puede leer CSS: `vite.config.ts` espeja `--color-bg` de `light-cobalt` para `background_color` y `theme_color`. Si cambia ese token, se actualizan ambos valores. El `theme-color` en runtime sí se sincroniza desde el token.
8. Los tokens `--preview-*` existen solo para dibujar los swatches del selector de tema, porque necesita mostrar las cuatro paletas al mismo tiempo.
9. `--auth-photo` apunta a `frontend/public/images/fondo-notas.jpg` y `--auth-photo-veil` es un velo neutro común. Como la foto es clara, el texto de marca usa `--auth-brand-text`, `--auth-brand-text-muted`, `--auth-brand-accent` y `--auth-brand-focus` con tinta oscura también en temas oscuros. Esos tokens se aplican solo a la columna de marca: la tarjeta del formulario mantiene `--card-bg`, su superficie en móvil y los colores semánticos normales de texto y enlaces, para conservar AA. No se usa CSS condicional por tema.

## Estados de producto

- **Vacío:** explicar y ofrecer acción.
- **Cargando:** skeleton o texto breve; no spinner infinito sin contexto.
- **Error:** mensaje humano, acción de reintento y sin datos falsos.
- **Sin permisos:** explicar el rol requerido, no redirigir en silencio.
