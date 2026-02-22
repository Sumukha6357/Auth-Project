# Security Policy

## Supported Versions
- Current stable: `1.0.x`

## Reporting a Vulnerability
- Do not create a public issue for sensitive vulnerabilities.
- Report privately to the maintainers with:
  - affected component (`api`, `web`, infra)
  - impact and attack path
  - proof-of-concept and remediation suggestion
- Response target:
  - initial acknowledgment: 2 business days
  - triage decision: 5 business days

## Security Baseline
- OAuth2/OIDC via Spring Authorization Server.
- RSA signing keys with scheduled rotation and JWKS overlap.
- Secrets sourced from environment or `*_FILE` Docker-secret paths.
- Refresh token rotation + reuse detection.
- Admin APIs protected by `admin:full_access` permission.
- Structured logs with correlation IDs and sensitive-field masking.
