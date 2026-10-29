# ADR-0005 — Categorías por usuario

## Contexto

Las categorías globales permiten que un usuario vea y modifique la taxonomía de todos. El ownership debe ser uniforme.

## Decisión

- `categorias.usuario_id` NOT NULL.
- Unique `(usuario_id, nombre)`; se elimina el unique global de `nombre`.
- Todo CRUD valida ownership; borrar una categoría con notas devuelve 409.
- `nota_categoria` solo puede unir nota y categoría del mismo dueño; lo verifica `INF-04b`.

## Consecuencias

- Aislamiento multi-tenant real.
- El seed crea categorías por usuario.
- No hay taxonomía administrada globalmente; si se necesita, será un recurso distinto.
