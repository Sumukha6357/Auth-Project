# Redis Key Strategy

Use namespaced, collision-safe, and version-aware key names.

## Naming Pattern
`{domain}:{context}:{identifier...}`

## Standard Keys
- `booking:lock:{bookingId}`
- `idempotency:{tenantId}:{idempotencyKey}`
- `session:{userId}:{deviceId}`
- `cache:v1:{entity}:{id}`

## Rules
- Always include tenant context when data is tenant-scoped.
- Use explicit key versions (`cache:v1`) for schema/value evolution.
- Keep lock keys short-lived with TTL.
- Keep idempotency keys with deterministic TTL by operation type.
- Never reuse the same key pattern across unrelated domains.

## Recommended TTLs
- Booking lock: 30-120 seconds
- Idempotency keys: 24 hours (or business-specific)
- Session keys: aligned to refresh/session expiry
- Cache keys: per entity volatility

## Operational Notes
- Prefix can include environment if multiple envs share Redis (`prod:...`, `stg:...`).
- Document new key families in this file before rollout.
- Avoid storing secrets or plaintext credentials in Redis values.
