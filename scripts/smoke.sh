#!/usr/bin/env bash
set -euo pipefail

API="${API:-http://localhost:8080/api/v1}"
USERNAME="smoke_$(date +%s)"
PASSWORD="Password123"
COOKIES="$(mktemp)"

cleanup() {
  rm -f "$COOKIES"
}
trap cleanup EXIT

echo "== register =="
curl -fsS -X POST "$API/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$USERNAME\",\"email\":\"$USERNAME@example.com\",\"password\":\"$PASSWORD\",\"nombre\":\"Smoke\"}"

echo
echo "== login =="
LOGIN=$(curl -fsS -c "$COOKIES" -X POST "$API/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")
TOKEN=$(printf '%s' "$LOGIN" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
test -n "$TOKEN"

echo "== me =="
curl -fsS "$API/auth/me" -H "Authorization: Bearer $TOKEN" > /dev/null

echo "== create note =="
curl -fsS -X POST "$API/notes" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Smoke test","contenido":"ok","prioridad":2,"estado":"PENDIENTE"}' > /dev/null

echo "== refresh =="
curl -fsS -b "$COOKIES" -c "$COOKIES" -X POST "$API/auth/refresh" > /dev/null

echo "== logout =="
curl -fsS -b "$COOKIES" -X POST "$API/auth/logout" -o /dev/null

echo "OK"
