# Auth-Server Workspace

- Backend IDP service: [`api/`](api/README.md)
- Frontend admin + OIDC demo: [`web/`](web/README.md)
- Container platform docs: [`docs/docker.md`](docs/docker.md)

## Canonical Docker Commands
Local:
`docker compose -f docker-compose.yml -f docker-compose.local.yml up --build`

Dev-like:
`docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build`

Prod-like:
`docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build`
