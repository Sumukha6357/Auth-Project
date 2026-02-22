# IDP (Auth0-like backend-only Identity Provider)

Standalone Spring Authorization Server based IdP with PostgreSQL + Flyway.

## Features
- OAuth2/OIDC endpoints: `/oauth2/authorize`, `/oauth2/token`, `/.well-known/openid-configuration`, `/oauth2/jwks`, `/userinfo`, `/oauth2/revoke`, `/oauth2/introspect`
- Authorization Code + PKCE, Client Credentials, Refresh Tokens (rotation enabled)
- DB-backed OAuth clients (public/confidential), strict redirect URI validation, public-client PKCE enforcement
- User admin APIs, password hashing (Argon2 with BCrypt fallback), email verification tokens
- Account lockout after failed attempts
- RBAC with roles/permissions; roles and permissions emitted as token claims
- Refresh token session tracking + session revoke APIs
- JWK management in DB with AES-GCM encrypted private JWK, scheduled/manual rotation
- Correlation ID filter (`X-Correlation-Id`) + audit logs for security events
- Flyway migrations for all schema objects

## Quick Start
### Local (Java)
```bash
mvn -f api/pom.xml clean package
mvn -f api/pom.xml test
mvn -f api/pom.xml spring-boot:run
```

### Docker Compose (Deprecated for this subfolder)
```bash
cd ..
docker compose -f docker-compose.yml -f docker-compose.local.yml up --build
```

Use root-level compose as the single source of truth. Subfolder Docker files are deprecated.

## Environment Variables
- `POSTGRES_URL=jdbc:postgresql://localhost:5432/idp`
- `POSTGRES_USER=idp`
- `POSTGRES_PASSWORD=idp`
- `IDP_ISSUER=http://localhost:9000`
- `IDP_KEY_ENCRYPTION_SECRET=replace-with-32-plus-char-secret`
- `IDP_ADMIN_BOOTSTRAP_EMAIL=admin@example.com`
- `IDP_ADMIN_BOOTSTRAP_PASSWORD=ChangeMe123!`
- `RUN_TESTCONTAINERS=false` (set `true` to execute integration tests)

## Seeded Clients
- `web-portal` (public, PKCE required, redirect URI `http://localhost:3000/oidc/callback`, scopes `openid profile email offline_access`)
- `system-service` (confidential, grant `client_credentials`, placeholder secret hash seeded; rotate immediately)

## OAuth2/OIDC Flows
### 1) Client Credentials
```bash
curl -u system-service:password \
  -X POST "http://localhost:9000/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=idp.read"
```

### 2) Authorization Code + PKCE
Generate verifier/challenge:
```bash
VERIFIER=$(openssl rand -base64 64 | tr -d '=+/\n' | cut -c1-64)
CHALLENGE=$(printf '%s' "$VERIFIER" | openssl dgst -binary -sha256 | openssl base64 | tr '+/' '-_' | tr -d '=\n')
```

Authorize in browser:
```text
http://localhost:9000/oauth2/authorize?response_type=code&client_id=web-portal&redirect_uri=http://localhost:3000/oidc/callback&scope=openid%20profile%20email%20offline_access&code_challenge=<CHALLENGE>&code_challenge_method=S256&state=abc123
```

Exchange code:
```bash
curl -X POST "http://localhost:9000/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code&client_id=web-portal&code=<AUTH_CODE>&redirect_uri=http://localhost:3000/oidc/callback&code_verifier=$VERIFIER"
```

### 3) Refresh Rotation
```bash
curl -X POST "http://localhost:9000/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token&client_id=web-portal&refresh_token=<REFRESH_TOKEN>"
```
Use returned refresh token for next refresh; old token reuse is detected and session family is revoked.

### 4) Revoke Token
```bash
curl -u system-service:password \
  -X POST "http://localhost:9000/oauth2/revoke" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=<TOKEN_TO_REVOKE>&token_type_hint=refresh_token"
```

### 5) UserInfo
```bash
curl -H "Authorization: Bearer <ACCESS_TOKEN>" \
  "http://localhost:9000/userinfo"
```

## Admin APIs
All `/api/v1/admin/*` endpoints require JWT with `admin:full_access`.

- `POST /api/v1/admin/users`
- `GET /api/v1/admin/users`
- `POST /api/v1/admin/users/{id}/roles`
- `POST /api/v1/admin/clients`
- `PATCH /api/v1/admin/clients/{clientId}`
- `POST /api/v1/admin/keys/rotate`
- `GET /api/v1/admin/clients`
- `GET /api/v1/admin/roles`
- `GET /api/v1/admin/permissions`
- `GET /api/v1/admin/keys`
- `GET /api/v1/admin/sessions?page=0&size=50&userId=<uuid>&clientId=<id>`
- `POST /api/v1/admin/sessions/revoke`
- `GET /api/v1/admin/audit-logs?page=0&size=50&eventType=...&success=true|false&from=...&to=...`

Public email verification:
- `POST /api/v1/public/verify-email?token=...`

Session APIs:
- `GET /api/v1/sessions/me`
- `POST /api/v1/sessions/me/revoke`
- `POST /api/v1/sessions/me/revoke-all`

## Resource Server JWT Validation (JWKS)
Spring Boot resource server config example:
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9000
          jwk-set-uri: http://localhost:9000/oauth2/jwks
```

## Tests
```bash
mvn -f api/pom.xml test
```
- Unit tests always run.
- Integration tests are Testcontainers-based and run only when `RUN_TESTCONTAINERS=true` and Docker is available.

## Database Backup/Restore (PostgreSQL)
Backup:
```bash
pg_dump -h localhost -U idp -d idp -Fc -f idp.backup
```

Restore:
```bash
dropdb -h localhost -U idp idp
createdb -h localhost -U idp idp
pg_restore -h localhost -U idp -d idp --clean --if-exists idp.backup
```

## Observability
- Actuator endpoints: `/actuator/health`, `/actuator/health/readiness`, `/actuator/metrics`, `/actuator/prometheus`
- Custom metrics:
  - `idp.oauth.requests` (token/introspect/revoke success/failure)
  - `idp.ratelimit.blocked`
  - `idp.audit.events`
