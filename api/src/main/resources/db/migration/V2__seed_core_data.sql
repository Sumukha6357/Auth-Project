INSERT INTO tenants (id, tenant_key, name, active, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'default', 'Default Tenant', TRUE, now())
ON CONFLICT (tenant_key) DO NOTHING;

INSERT INTO permissions (id, name, description)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'admin:full_access', 'Full administrative access'),
    ('00000000-0000-0000-0000-000000000102', 'sessions:manage', 'Manage own sessions')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (id, name, description)
VALUES
    ('00000000-0000-0000-0000-000000000201', 'ADMIN', 'Platform administrator'),
    ('00000000-0000-0000-0000-000000000202', 'USER', 'Default user role')
ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p
  ON (r.name = 'ADMIN' AND p.name = 'admin:full_access')
  OR (r.name = 'USER' AND p.name = 'sessions:manage')
ON CONFLICT DO NOTHING;

INSERT INTO users (id, email, password_hash, status, email_verified, failed_attempts, locked_until, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000301',
    'admin@local.invalid',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoO.HA3X7YB9ox96D65gis6pZeRAEIJ5ZS',
    'ACTIVE',
    TRUE,
    0,
    NULL,
    now(),
    now()
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.email = 'admin@local.invalid'
ON CONFLICT DO NOTHING;

INSERT INTO user_tenants (user_id, tenant_id)
SELECT u.id, t.id
FROM users u
JOIN tenants t ON t.tenant_key = 'default'
WHERE u.email = 'admin@local.invalid'
ON CONFLICT DO NOTHING;

INSERT INTO oauth_clients (client_id, client_secret_hash, name, type, redirect_uris, post_logout_redirect_uris, grant_types, scopes, require_pkce, enabled, created_at)
VALUES
    (
      'web-portal',
      NULL,
      'Web Portal Public Client',
      'PUBLIC',
      ARRAY['http://localhost:3000/oidc/callback'],
      ARRAY['http://localhost:3000'],
      ARRAY['authorization_code', 'refresh_token'],
      ARRAY['openid', 'profile', 'email', 'offline_access'],
      TRUE,
      TRUE,
      now()
    ),
    (
      'system-service',
      '$2a$10$7EqJtq98hPqEX7fNZaFWoO.HA3X7YB9ox96D65gis6pZeRAEIJ5ZS',
      'Internal Service Client',
      'CONFIDENTIAL',
      ARRAY[]::TEXT[],
      ARRAY[]::TEXT[],
      ARRAY['client_credentials', 'refresh_token'],
      ARRAY['idp.read', 'idp.write'],
      FALSE,
      TRUE,
      now()
    )
ON CONFLICT (client_id) DO NOTHING;

INSERT INTO system_config (config_key, config_value, updated_at)
VALUES
    ('security.password_policy', 'minLength=12;requireUpper=true;requireDigit=true', now()),
    ('security.session.max_devices', '5', now())
ON CONFLICT (config_key) DO NOTHING;
