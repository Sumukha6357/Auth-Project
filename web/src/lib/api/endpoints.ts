import { APP_CONFIG } from "@/lib/config";

export const apiEndpoints = {
  admin: {
    users: `${APP_CONFIG.adminApiBaseUrl}/admin/users`,
    clients: `${APP_CONFIG.adminApiBaseUrl}/admin/clients`,
    roles: `${APP_CONFIG.adminApiBaseUrl}/admin/roles`,
    permissions: `${APP_CONFIG.adminApiBaseUrl}/admin/permissions`,
    keys: `${APP_CONFIG.adminApiBaseUrl}/admin/keys`,
    sessions: `${APP_CONFIG.adminApiBaseUrl}/admin/sessions`,
    revokeSession: `${APP_CONFIG.adminApiBaseUrl}/admin/sessions/revoke`,
    auditLogs: `${APP_CONFIG.adminApiBaseUrl}/admin/audit-logs`,
    rotateKeys: `${APP_CONFIG.adminApiBaseUrl}/admin/keys/rotate`,
    userRoles: (userId: string) => `${APP_CONFIG.adminApiBaseUrl}/admin/users/${userId}/roles`,
    patchClient: (clientId: string) => `${APP_CONFIG.adminApiBaseUrl}/admin/clients/${clientId}`,
  },
  sessions: {
    listMe: `${APP_CONFIG.adminApiBaseUrl}/sessions/me`,
    revoke: `${APP_CONFIG.adminApiBaseUrl}/sessions/me/revoke`,
    revokeAll: `${APP_CONFIG.adminApiBaseUrl}/sessions/me/revoke-all`,
  },
  oauth: {
    authorize: `${APP_CONFIG.idpBaseUrl}/oauth2/authorize`,
    token: `${APP_CONFIG.idpBaseUrl}/oauth2/token`,
    userInfo: `${APP_CONFIG.idpBaseUrl}/userinfo`,
    revoke: `${APP_CONFIG.idpBaseUrl}/oauth2/revoke`,
    jwks: `${APP_CONFIG.idpBaseUrl}/oauth2/jwks`,
    openidConfig: `${APP_CONFIG.idpBaseUrl}/.well-known/openid-configuration`,
  },
};
