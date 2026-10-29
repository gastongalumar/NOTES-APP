# NotasApp backend

Spring Boot 3.2 + Java 17. API REST en `/api/v1`.

## Ejecutar

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Variables requeridas: `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`, `JWT_SECRET` (base64 ≥32 bytes), `CORS_ALLOWED_ORIGINS`. Ver `.env.example` en la raíz.

## Capas

```text
controller → service (interface) → service.impl → repository → model
                         ↘ mapper (MapStruct/manual)
                         ↘ storage (local/MinIO)
```

Los controllers solo reciben/devuelven DTOs y no importan repositories ni entidades. Las lecturas usan `@Transactional(readOnly = true)`.

## Migraciones

`src/main/resources/db/migration/V1–V17`. `ddl-auto=validate`; no se usa `update`. El perfil `dev` agrega `db/seed`; producción no carga seed.

## Tests

```bash
./gradlew test
```

`NotasApplicationTests` valida el contexto con H2. `AuthFlowIT` cubre registro, login, refresh y logout. Para MySQL real, usar Docker Compose y el script `scripts/smoke.sh`.

## Storage

`STORAGE_PROVIDER=local` guarda en `STORAGE_LOCAL_PATH`; `minio` usa `MINIO_*` y un bucket privado. El upload pasa por el backend para validar cuota, MIME y magic bytes; la descarga usa presigned URL cuando el proveedor es MinIO.
