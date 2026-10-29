# NotasApp — Agenda personal + archivador seguro

Aplicación de notas y tareas con autenticación, roles, adjuntos y búsqueda. El proyecto está separado en un backend Spring Boot y un frontend React + TypeScript.

## Stack

- **Backend:** Java 17, Spring Boot 3.2, Spring Security, JPA, MySQL 8, Flyway, JWT, MapStruct, Lombok, MinIO.
- **Frontend:** React 18, TypeScript strict, Vite 5, React Router 6, Axios, CSS plano con tokens.
- **Infra:** Docker Compose, GitHub Actions, Testcontainers (tests con H2 en CI/local sin Docker).

## Funcionalidades

- Registro, login, refresh rotativo en cookie `HttpOnly`, logout y bloqueo por intentos fallidos.
- Roles reales `USER`/`ADMIN`; endpoints de administración protegidos con `@PreAuthorize`.
- Notas con soft delete, archivo, categorías, tags, vencimiento, prioridad y estado.
- Búsqueda por título/contenido y filtros por categoría/tag.
- Adjuntos con cuota, validación de MIME/magic bytes, checksum SHA-256 y descarga firmada o local.
- Auditoría de eventos de autenticación y administración.
- Dos temas (`light-neumorphic` y `dark-purple`) con `system/light/dark` y anti-FOUC.

## Inicio rápido con Docker

```bash
cp .env.example .env
# Completar DB_PASSWORD, JWT_SECRET (openssl rand -base64 48) y el resto.
docker compose up --build
```

- Frontend: <http://localhost:3000>
- API: <http://localhost:8080>
- Swagger (solo dev): <http://localhost:8080/swagger-ui.html>
- MinIO consola: <http://localhost:9001>

## Desarrollo local

### 1. Requisitos

JDK 17, Node 20+, MySQL 8. Docker es opcional para MySQL/MinIO, pero necesario para el compose completo.

### 2. Variables

```bash
cp .env.example .env
```

El backend falla al arrancar si `JWT_SECRET` no está definido. El secreto debe ser base64 de al menos 32 bytes:

```bash
openssl rand -base64 48
```

### 3. Backend

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```

En PowerShell:

```powershell
$env:JAVA_HOME = "C:\ruta\a\jdk-17"
.\gradlew.bat bootRun --args="--spring.profiles.active=dev"
```

El perfil `dev` carga `db/migration` + `db/seed`. El seed crea las cuentas de demostración **solo en desarrollo**:

| Usuario | Contraseña |
|---|---|
| `admin` | `admin123` |
| `usuario` | `usuario123` |

No usar estas credenciales en producción. El primer `ADMIN` de producción se crea con `BOOTSTRAP_ADMIN_*` solo si no existe ningún administrador.

### 4. Frontend

```bash
cd frontend
npm ci
npm run dev
```

Abre <http://localhost:5173>. Vite hace proxy de `/api` a `http://localhost:8080`.

## Base de datos y migraciones

Flyway aplica `backend/src/main/resources/db/migration/V1–V17`. La base actual de demo **se recrea**; no se migran datos legacy. Antes de recrear, guardá un `mysqldump` fuera del repositorio.

Verificación de la migración V6:

```bash
mysql -u notas -p notas_db < scripts/verify_v6_migration.sql
```

## API

Base: `/api/v1`. Autenticación con `Authorization: Bearer <access>`; refresh por cookie. Errores en `application/problem+json` con `code` y, en validación, `errors`.

Rutas principales:

- `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `GET /auth/me`
- `POST /auth/2fa/setup`, `POST /auth/2fa/verify`
- `POST /auth/password/forgot`, `POST /auth/password/reset`
- `POST /auth/email/request`, `POST /auth/email/verify`
- `GET/PATCH/DELETE /users/me`, `PUT /users/me/password`
- `GET/POST /notes`, `GET/PUT/DELETE /notes/{id}`, `PATCH /notes/{id}/archive`, `/unarchive`, `/agenda`
- `GET /notes/search`, `GET /notes/today`, `GET /notes/upcoming`
- `GET/POST /categories`, `PUT/DELETE /categories/{id}`
- `GET/POST /tags`, `PUT/DELETE /tags/{id}`
- `POST/GET /notes/{noteId}/attachments`, `GET /attachments/{id}/download`, `DELETE /attachments/{id}`
- `POST /notes/{id}/shares`, `GET /public/shares/{token}`, `GET /notes/{id}/export.md`
- `GET /admin/users`, `PATCH /admin/users/{id}/enabled`, `/roles`, `DELETE`, `GET /admin/audit`, `GET /admin/stats`

## Tests

```bash
cd backend && ./gradlew test
cd frontend && npm run typecheck && npm run test:run && npm run build
```

Los tests de backend usan H2 en perfil `test`; no requieren MySQL ni Docker. Para integración real con MySQL/MinIO, levantar el compose y ejecutar el smoke.

## Estructura

```text
backend/   Spring Boot, migraciones Flyway, tests
frontend/  Vite + React + TypeScript, tokens de diseño
docs/      dirección de diseño y ADRs
scripts/   verificación de migración y smoke
```

## Seguridad

- Access token JWT de 15 min solo en memoria; refresh opaco rotativo en cookie.
- Sin secretos en el repositorio; `.env` ignorado.
- CORS con orígenes exactos, sin `*` con credenciales.
- Contraseñas BCrypt, rate limit en auth y lockout.
- Upload con whitelist, magic bytes y cuotas; bucket privado y presigned TTL corto.
- Errores sin stack traces; Swagger deshabilitado en prod.
- Consultar `AGENTS.md` y `docs/DESIGN_DIRECTION.md` antes de tocar UI.

## Estado

Release 1 (auth, notas, categorías, tags, adjuntos, búsqueda, auditoría, temas) implementado. De R2 están implementados los campos de agenda, recordatorios in-app, notificaciones y vista calendario. R3 incluye 2FA TOTP, verificación de email, password reset, enlaces públicos, export PDF/MD y ClamAV opcional; la validación real con Docker/MySQL/MinIO/ClamAV sigue pendiente en este entorno.
