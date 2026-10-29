# ADR-0004 — MinIO con abstracción de storage

## Contexto

Guardar adjuntos como BLOB en MySQL complica backups, cuotas y servido. Se necesita un storage S3-compatible en local y producción.

## Decisión

- `StorageService` con implementaciones `LocalStorageService` (dev) y `MinioStorageService`.
- Bucket privado; descarga por presigned URL de 60 s cuando el proveedor es MinIO.
- Upload pasa por el backend para validar MIME, magic bytes, tamaño y cuota.
- Nombre de objeto UUID; checksum SHA-256 en metadata.

## Consecuencias

- MySQL queda liviano y el storage se escala aparte.
- MinIO es una dependencia operativa; hay fallback local para desarrollo.
- La URL firmada no debe loguearse ni cachearse.
