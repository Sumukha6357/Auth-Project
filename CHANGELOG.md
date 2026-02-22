# Changelog

All notable changes to this repository are documented here.

## [1.0.0] - 2026-02-22
### Added
- Production-grade hardening for `api/` (HTTPS enforcement, audience validation, rate limiting, startup secret validation, security metrics, structured logs).
- Admin read APIs for roles, permissions, clients, keys, sessions, and paginated audit logs.
- Frontend hardening in `web/` with real endpoint integration, no mocked admin fallbacks, global error boundaries, CSP headers.
- Refresh-token handling moved to Next server routes with httpOnly cookie storage.
- Root deployment stack files: `docker-compose.yml`, `docker-compose.dev.yml`, `docker-compose.local.yml`, `docker-compose.default.yml`, `docker-compose.prod.yml`.
- Hardened Nginx configuration under `deploy/nginx/nginx.conf`.
- CI pipeline with backend tests, frontend lint/build/test, Trivy vulnerability scan, Docker image build.
