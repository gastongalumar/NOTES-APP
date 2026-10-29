# ADR-0007 — Búsqueda full-text con fallback

## Contexto

El frontend filtraba en memoria y la API no ofrecía búsqueda. Se necesita búsqueda server-side sobre título y contenido sin agregar Elasticsearch.

## Decisión

- Índice `FULLTEXT (titulo, contenido)` en MySQL InnoDB (V10).
- Búsqueda con `MATCH ... AGAINST` para términos de 3+ caracteres.
- Fallback `LIKE` para 1–2 caracteres y para compatibilidad.
- Siempre filtrada por `usuario_id` y `deleted_at IS NULL`.

## Consecuencias

- Sin infraestructura extra.
- Limitaciones de stopwords y token mínimo de InnoDB; se documentan y testean.
- El índice debe revisarse al cambiar la collation.
