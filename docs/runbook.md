# Runbook — NotasApp

## Variables requeridas

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET` (base64 de 32+ bytes)
- `CORS_ALLOWED_ORIGINS` (lista separada por comas)
- `COOKIE_SECURE=false` en dev, `true` en prod
- `STORAGE_PROVIDER=local|minio`
- `BOOTSTRAP_ADMIN_*` solo la primera vez en prod

## Reset de base de datos (dev)

La DB de demo se recrea; no se migran datos legacy.

```bash
mysqldump -u root -p notas_db > backup-notasapp-$(date +%F).sql
mysql -u root -p -e "DROP DATABASE IF EXISTS notas_db; CREATE DATABASE notas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Flyway aplica V1–V17 y el seed dev crea `admin`/`usuario` solo en `dev`.

## Verificar V6

```bash
mysql -u notas -p notas_db < scripts/verify_v6_migration.sql
```

Falla con `SIGNAL` si hay categorías sin dueño, vínculos entre dueños distintos, duplicados o huérfanos.

## Producción

1. `JWT_SECRET`, `APP_ENCRYPTION_KEY` y `DB_PASSWORD` nuevos, fuera del repo.
2. `SPRING_PROFILES_ACTIVE=prod` (lo fija `docker-compose.prod.yml`).
3. `COOKIE_SECURE=true`; frontend/API en el mismo dominio registrable (`app.`/`api.`).
4. CORS exacto, sin `*`.
5. Swagger off (perfil prod).
6. Bootstrap del primer ADMIN por env; verificar y luego quitar las variables.
7. Backup de MySQL antes de migrar o resetear.
8. Deploy: `./scripts/deploy-prod.sh` (pull + build + recreate + healthcheck).

### Mail

- Con **Resend**: `MAIL_PROVIDER=resend`, `RESEND_API_KEY`, `MAIL_FROM` verificado y `REQUIRE_EMAIL_VERIFICATION=true`.
- Con **MailHog interno**: `docker compose -f docker-compose.prod.yml --profile mail up -d` y `REQUIRE_EMAIL_VERIFICATION=false` (no entrega mails reales).
- `FRONTEND_URL` debe ser la URL pública: los enlaces de verificación y reset se arman con esa base.

### Reset de datos en producción

Solo si se acepta perder todo (usuarios, notas y adjuntos):

```bash
docker compose -f docker-compose.prod.yml down
docker volume rm notas-app_notas_mysql_data notas-app_notas_storage
docker compose -f docker-compose.prod.yml up -d --build
```

El bootstrap de `BOOTSTRAP_ADMIN_*` vuelve a crear el primer ADMIN. Quitar esas variables del `.env` después.

## MinIO

- Bucket privado `notasapp`.
- Presigned GET de 60 s.
- Si MinIO cae, los uploads fallan con 5xx y las descargas firmadas también. Monitorear `/minio/health/live`.
- En dev se puede usar `STORAGE_PROVIDER=local`.

## ClamAV (opcional)

El antivirus está deshabilitado por defecto (`ANTIVIRUS_ENABLED=false`). Para habilitarlo:

```bash
docker compose --profile av up -d clamav
# En backend: ANTIVIRUS_ENABLED=true, CLAMAV_HOST=clamav, CLAMAV_PORT=3310
```

Con el flag activo, cada upload queda `scan_status=PENDING` y no se puede descargar hasta que el scheduler lo marque `READY`. Si ClamAV no responde, el adjunto queda `ERROR` y sigue bloqueado (fail-closed). Los archivos `INFECTED` se eliminan del storage y la descarga responde 410. Revisar `scan_status` en `adjuntos`.

## Email

- Dev: `MAIL_PROVIDER=mailhog` y `docker compose --profile mail up -d mailhog`; UI en `http://localhost:8025`.
- Prod: `MAIL_PROVIDER=resend` y `RESEND_API_KEY`.
- `FRONTEND_URL` se usa en los enlaces de reset y verificación.

## Troubleshooting

- **401 en refresh:** cookie ausente/expirada, `COOKIE_SECURE` incorrecto o dominio cruzado. Revisar DevTools → Application → Cookies.
- **CORS:** el `Origin` debe estar exactamente en `CORS_ALLOWED_ORIGINS`.
- **Flyway validation:** `SHOW CREATE TABLE` vs entidad; no editar migraciones aplicadas, crear una nueva.
- **Upload 415:** MIME detectado por Tika no está en la whitelist.
- **Lockout:** esperar 15 min o limpiar `failed_attempts`/`locked_until` en dev.
