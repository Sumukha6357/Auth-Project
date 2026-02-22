export type ApiListResponse<T> = {
  items: T[];
  total?: number;
  page?: number;
  size?: number;
};

export type Client = {
  clientId: string;
  name: string;
  type: "PUBLIC" | "CONFIDENTIAL";
  enabled: boolean;
  requirePkce: boolean;
  scopes: string[];
  grantTypes: string[];
  redirectUris: string[];
  rawSecret?: string | null;
};

export type User = {
  id: string;
  email: string;
  status: string;
  emailVerified: boolean;
  roles: string[];
  verificationToken?: string | null;
};

export type Role = { id: string; name: string; description?: string };
export type Permission = { id: string; name: string; description?: string };
export type KeyItem = { kid: string; status: string; algorithm?: string; createdAt?: string };
export type SessionItem = { id: string; clientId: string; deviceId: string; issuedAt: string; expiresAt: string; ip?: string; userAgent?: string };
export type AuditLog = { id: string; eventType: string; entityType: string; success: boolean; createdAt: string; correlationId?: string };
