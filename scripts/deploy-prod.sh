#!/usr/bin/env bash
# Deploy de producción en la VM. Requiere Docker y el repo clonado en la rama backend.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  echo "Falta .env de producción. Copiá .env.prod.example y completá los secretos." >&2
  exit 1
fi

echo "==> Actualizando código"
git pull --ff-only origin backend

echo "==> Construyendo y recreando servicios"
docker compose -f docker-compose.prod.yml up -d --build --force-recreate

PORT="${HTTP_PORT:-8091}"
echo "==> Esperando frontend y API en http://127.0.0.1:${PORT}"
for i in $(seq 1 80); do
  frontend_ok=false
  api_ok=false
  if curl -fsS "http://127.0.0.1:${PORT}/" 2>/dev/null | grep -q '<div id="root"'; then
    frontend_ok=true
  fi
  if curl -fsS "http://127.0.0.1:${PORT}/api/v1/auth/status" 2>/dev/null | grep -q '"status":"ok"'; then
    api_ok=true
  fi
  if [ "$frontend_ok" = true ] && [ "$api_ok" = true ]; then
    break
  fi
  sleep 3
  if [ "$i" -eq 80 ]; then
    echo "Frontend o API no respondieron. Revisar: docker compose -f docker-compose.prod.yml ps" >&2
    exit 1
  fi
done

echo "==> Verificando manifest"
curl -fsS "http://127.0.0.1:${PORT}/manifest.webmanifest" >/dev/null

echo "==> Estado"
docker compose -f docker-compose.prod.yml ps

echo "Deploy OK. Verificar login, mail y 2FA en el dominio público."
