# web

Frontend workspace containing:
- Admin Console (Auth0-style management UI)
- OIDC Demo Client (auth code + PKCE + refresh + revoke + userinfo + JWKS)

## Setup
```bash
cd web
npm i
npm run dev
```

## Build
```bash
npm run build
```

## Docker
Subfolder Docker files are deprecated. Use root compose only:

```bash
cd ..
docker compose -f docker-compose.yml -f docker-compose.local.yml up --build
```

## Tests
```bash
npm run test
npm run test:e2e
```

## Environment (`.env.local`)
```env
NEXT_PUBLIC_IDP_BASE_URL=http://localhost:9000
NEXT_PUBLIC_ADMIN_API_BASE_URL=http://localhost:9000/api/v1
NEXT_PUBLIC_OIDC_CLIENT_ID=demo-public
NEXT_PUBLIC_OIDC_REDIRECT_URI=http://localhost:3000/oidc/callback
NEXT_PUBLIC_OIDC_SCOPES="openid profile email offline_access"
NEXT_PUBLIC_ADMIN_REQUIRED_PERMISSION="admin:full_access"
```

## OIDC Flow Used
1. `/login` triggers Authorization Code + PKCE.
2. Browser redirects to IDP `/oauth2/authorize` with code challenge, state, nonce.
3. `/oidc/callback` calls `POST /api/auth/exchange`; the Next.js server exchanges code with the IDP.
4. Refresh tokens are stored in an `httpOnly` cookie (server-side), and access tokens are held in memory + `sessionStorage`.
5. Demo pages execute `/userinfo`, `/oauth2/jwks`, refresh, and revoke flows through backend-safe routes.

## Production Notes
- Admin console pages expect full backend admin endpoints (`clients/users/roles/permissions/keys/sessions/audit-logs`).
- Security headers (including CSP) are configured in `next.config.ts`.
