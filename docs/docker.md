# Docker Platform Setup

## Canonical Docker Entry Point
All containerization is centralized at repository root:
- `api/Dockerfile` and `web/Dockerfile` for image builds
- `docker-compose.yml` base stack
- overlays: `docker-compose.local.yml`, `docker-compose.dev.yml`, `docker-compose.prod.yml`
- env: `.env` (runtime), `.env.example` (template)

Subfolder Docker/compose files are deprecated and not used by root compose.

## Services
- `postgres`
- `redis`
- `flyway` (migration job)
- `api` (Spring Boot)
- `web` (Next.js)

## Run Commands
Local developer defaults:
```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml up --build
```

Shared dev-like:
```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

Prod-like:
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

## Migration Flow
- Flyway runs as dedicated `flyway` service on startup.
- SQL source: `api/src/main/resources/db/migration`.
- `api` starts only after `postgres` is healthy and `flyway` finishes successfully.
- Add new migration files using: `V{number}__description.sql`.
- On next `up` / restart, Flyway applies pending migrations automatically.

## Caching Strategy
Backend build (`api/Dockerfile`):
- Copies `api/pom.xml` first
- Runs dependency resolution in cached layer (`/root/.m2` cache mount)
- Source changes rebuild only compile/package layers

Frontend build (`web/Dockerfile`):
- Copies `web/package*.json` first
- Runs `npm ci` with cache mount (`/root/.npm`)
- Source changes rebuild app without reinstalling dependencies unless lockfile changes

## Notes
- Hibernate schema mutation is disabled in container via `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`.
- Keep `.env` local-only and never commit real secrets.
