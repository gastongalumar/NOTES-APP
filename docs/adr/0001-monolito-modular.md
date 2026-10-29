# ADR-0001 — Monolito modular

## Contexto

La app es una pieza de portfolio y debe ser operable por una sola persona. Los microservicios agregarían despliegue, observabilidad y contratos distribuidos sin beneficio.

## Decisión

Un solo backend Spring Boot con capas `controller → service → repository → model`, paquetes por responsabilidad y storage abstraído.

## Consecuencias

- Menos infraestructura y despliegue simple con Docker Compose.
- La modularidad se sostiene por paquetes, interfaces y tests, no por red.
- Si el producto crece, se pueden extraer módulos sin reescribir contratos.
