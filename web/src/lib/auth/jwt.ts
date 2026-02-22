import { jwtDecode } from "jwt-decode";

export type JwtPayload = {
  sub?: string;
  email?: string;
  scope?: string;
  exp?: number;
  iat?: number;
  permissions?: string[];
  roles?: string[];
  [key: string]: unknown;
};

export function decodeJwt(token?: string | null): JwtPayload | null {
  if (!token) return null;
  try {
    return jwtDecode<JwtPayload>(token);
  } catch {
    return null;
  }
}
