CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE user_status AS ENUM ('ACTIVE', 'LOCKED', 'DISABLED');
CREATE TYPE client_type AS ENUM ('PUBLIC', 'CONFIDENTIAL');
CREATE TYPE jwk_status AS ENUM ('ACTIVE', 'RETIRED');

CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    tenant_key VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE system_config (
    config_key VARCHAR(150) PRIMARY KEY,
    config_value VARCHAR(1000) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status user_status NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE user_tenants (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, tenant_id)
);

CREATE TABLE user_profile (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255),
    phone VARCHAR(64),
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE,
    description VARCHAR(512)
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE,
    description VARCHAR(512)
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY(role_id, permission_id)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY(user_id, role_id)
);

CREATE TABLE oauth_clients (
    client_id VARCHAR(100) PRIMARY KEY,
    client_secret_hash VARCHAR(255),
    name VARCHAR(200) NOT NULL,
    type client_type NOT NULL,
    redirect_uris TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    post_logout_redirect_uris TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    grant_types TEXT[] NOT NULL,
    scopes TEXT[] NOT NULL,
    require_pkce BOOLEAN NOT NULL DEFAULT TRUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE oauth2_authorization (
    id varchar(100) NOT NULL,
    registered_client_id varchar(100) NOT NULL,
    principal_name varchar(200) NOT NULL,
    authorization_grant_type varchar(100) NOT NULL,
    authorized_scopes varchar(1000) DEFAULT NULL,
    attributes bytea DEFAULT NULL,
    state varchar(500) DEFAULT NULL,
    authorization_code_value bytea DEFAULT NULL,
    authorization_code_issued_at timestamp DEFAULT NULL,
    authorization_code_expires_at timestamp DEFAULT NULL,
    authorization_code_metadata bytea DEFAULT NULL,
    access_token_value bytea DEFAULT NULL,
    access_token_issued_at timestamp DEFAULT NULL,
    access_token_expires_at timestamp DEFAULT NULL,
    access_token_metadata bytea DEFAULT NULL,
    access_token_type varchar(100) DEFAULT NULL,
    access_token_scopes varchar(1000) DEFAULT NULL,
    oidc_id_token_value bytea DEFAULT NULL,
    oidc_id_token_issued_at timestamp DEFAULT NULL,
    oidc_id_token_expires_at timestamp DEFAULT NULL,
    oidc_id_token_metadata bytea DEFAULT NULL,
    refresh_token_value bytea DEFAULT NULL,
    refresh_token_issued_at timestamp DEFAULT NULL,
    refresh_token_expires_at timestamp DEFAULT NULL,
    refresh_token_metadata bytea DEFAULT NULL,
    user_code_value bytea DEFAULT NULL,
    user_code_issued_at timestamp DEFAULT NULL,
    user_code_expires_at timestamp DEFAULT NULL,
    user_code_metadata bytea DEFAULT NULL,
    device_code_value bytea DEFAULT NULL,
    device_code_issued_at timestamp DEFAULT NULL,
    device_code_expires_at timestamp DEFAULT NULL,
    device_code_metadata bytea DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE oauth2_authorization_consent (
    registered_client_id varchar(100) NOT NULL,
    principal_name varchar(200) NOT NULL,
    authorities varchar(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

CREATE TABLE refresh_token_sessions (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    client_id VARCHAR(100) NOT NULL REFERENCES oauth_clients(client_id) ON DELETE CASCADE,
    device_id VARCHAR(128) NOT NULL,
    refresh_token_hash VARCHAR(128) NOT NULL,
    parent_token_hash VARCHAR(128),
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    ip VARCHAR(64),
    user_agent VARCHAR(512)
);

CREATE TABLE jwk_keys (
    kid VARCHAR(128) PRIMARY KEY,
    public_jwk JSONB NOT NULL,
    private_jwk_encrypted BYTEA NOT NULL,
    algorithm VARCHAR(32) NOT NULL,
    status jwk_status NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    rotated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE email_verification_tokens (
    token_hash VARCHAR(128) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE password_reset_tokens (
    token_hash VARCHAR(128) PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    event_type VARCHAR(128) NOT NULL,
    entity_type VARCHAR(128) NOT NULL,
    entity_id VARCHAR(256),
    success BOOLEAN NOT NULL,
    ip VARCHAR(64),
    user_agent VARCHAR(512),
    correlation_id VARCHAR(128),
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX oauth2_authorization_registered_client_id_idx ON oauth2_authorization (registered_client_id);
CREATE INDEX oauth2_authorization_principal_name_idx ON oauth2_authorization (principal_name);
CREATE UNIQUE INDEX refresh_token_sessions_token_hash_uq ON refresh_token_sessions (refresh_token_hash);
CREATE INDEX refresh_token_sessions_user_idx ON refresh_token_sessions (user_id);
CREATE INDEX refresh_token_sessions_parent_idx ON refresh_token_sessions (parent_token_hash);
CREATE INDEX refresh_token_sessions_client_idx ON refresh_token_sessions (client_id);
CREATE INDEX refresh_token_sessions_issued_idx ON refresh_token_sessions (issued_at);
CREATE INDEX refresh_token_sessions_expires_idx ON refresh_token_sessions (expires_at);
CREATE INDEX jwk_keys_status_idx ON jwk_keys(status);
CREATE INDEX users_created_at_idx ON users(created_at);
CREATE INDEX users_status_idx ON users(status);
CREATE INDEX user_roles_user_id_idx ON user_roles(user_id);
CREATE INDEX user_roles_role_id_idx ON user_roles(role_id);
CREATE INDEX role_permissions_role_id_idx ON role_permissions(role_id);
CREATE INDEX role_permissions_permission_id_idx ON role_permissions(permission_id);
CREATE INDEX oauth_clients_created_at_idx ON oauth_clients(created_at);
CREATE INDEX audit_logs_created_idx ON audit_logs(created_at);
CREATE INDEX audit_logs_event_idx ON audit_logs(event_type);
CREATE INDEX audit_logs_actor_idx ON audit_logs(actor_user_id);
