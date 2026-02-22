export const APP_CONFIG = {
  idpBaseUrl: process.env.NEXT_PUBLIC_IDP_BASE_URL ?? "http://localhost:9000",
  adminApiBaseUrl: process.env.NEXT_PUBLIC_ADMIN_API_BASE_URL ?? "http://localhost:9000/api/v1",
  clientId: process.env.NEXT_PUBLIC_OIDC_CLIENT_ID ?? "demo-public",
  redirectUri: process.env.NEXT_PUBLIC_OIDC_REDIRECT_URI ?? "http://localhost:3000/oidc/callback",
  scopes: process.env.NEXT_PUBLIC_OIDC_SCOPES ?? "openid profile email offline_access",
  adminPermission: process.env.NEXT_PUBLIC_ADMIN_REQUIRED_PERMISSION ?? "admin:full_access",
};
