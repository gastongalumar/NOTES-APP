# ADR-0003 — Flyway y reset de la DB legacy

## Contexto

`ddl-auto=update` no versiona el esquema ni permite auditar cambios. La DB actual contiene solo datos de demo.

## Decisión

- Flyway es la única fuente de esquema (`V1…V16`).
- `ddl-auto=validate`; nunca `update`.
- La DB actual se recrea; no se migran datos legacy.
- Se hace un `mysqldump` de cortesía fuera del repo.
- `scripts/verify_v6_migration.sql` valida las invariantes de categorías por usuario.

## Consecuencias

- Despliegues reproducibles y revisables.
- Se pierde el estado de demo anterior (asumido).
- Un cambio de esquema requiere una migración nueva, nunca editar una aplicada.
