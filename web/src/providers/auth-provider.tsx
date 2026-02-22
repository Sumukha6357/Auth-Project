"use client";

import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import { apiEndpoints } from "@/lib/api/endpoints";
import { apiClient } from "@/lib/api/client";
import { decodeJwt, type JwtPayload } from "@/lib/auth/jwt";
import { buildAuthorizeUrl, exchangeCodeForTokens, fetchUserInfo, refreshAccessToken, revokeToken, validateCallbackState } from "@/lib/auth/oidc";
import { clearAuthStorage, getTokens, setTokens, type TokenSet } from "@/lib/auth/storage";
import { APP_CONFIG } from "@/lib/config";

type AuthUser = {
  sub?: string;
  email?: string;
  permissions: string[];
  roles: string[];
  claims: JwtPayload | null;
};

type AuthContextValue = {
  tokens: TokenSet | null;
  user: AuthUser | null;
  isAuthenticated: boolean;
  hasAdminPermission: boolean;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  completeLogin: (code: string, state: string | null) => Promise<void>;
  refresh: () => Promise<void>;
  logoutEverywhere: () => Promise<void>;
  fetchUserInfo: () => Promise<unknown | null>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function toAuthUser(tokens: TokenSet | null): AuthUser | null {
  const claims = decodeJwt(tokens?.accessToken);
  if (!claims) return null;
  return {
    sub: claims.sub as string | undefined,
    email: claims.email as string | undefined,
    permissions: (claims.permissions as string[] | undefined) ?? [],
    roles: (claims.roles as string[] | undefined) ?? [],
    claims,
  };
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [tokens, setTokenState] = useState<TokenSet | null>(null);

  useEffect(() => {
    setTokenState(getTokens());
  }, []);

  const user = useMemo(() => toAuthUser(tokens), [tokens]);

  const value: AuthContextValue = {
    tokens,
    user,
    isAuthenticated: !!tokens?.accessToken,
    hasAdminPermission: user?.permissions.includes(APP_CONFIG.adminPermission) ?? false,
    login: async () => {
      const authorizeUrl = await buildAuthorizeUrl();
      window.location.assign(authorizeUrl);
    },
    completeLogin: async (code: string, state: string | null) => {
      if (!validateCallbackState(state)) {
        throw new Error("Invalid OIDC state.");
      }
      const response = await exchangeCodeForTokens(code);
      const nextTokens: TokenSet = {
        accessToken: response.access_token,
        idToken: response.id_token,
        expiresIn: response.expires_in,
        tokenType: response.token_type,
      };
      setTokenState(nextTokens);
      setTokens(nextTokens);
      toast.success("Login successful");
    },
    refresh: async () => {
      const refreshed = await refreshAccessToken();
      const nextTokens: TokenSet = {
        accessToken: refreshed.access_token,
        idToken: refreshed.id_token ?? tokens?.idToken,
        expiresIn: refreshed.expires_in,
        tokenType: refreshed.token_type,
      };
      setTokenState(nextTokens);
      setTokens(nextTokens);
      toast.success("Token refreshed");
    },
    fetchUserInfo: async () => {
      if (!tokens?.accessToken) return null;
      return fetchUserInfo(tokens.accessToken);
    },
    logout: async () => {
      try {
        await revokeToken();
      } catch {
        // ignore revoke errors during logout
      }
      clearAuthStorage();
      setTokenState(null);
      window.location.assign("/");
    },
    logoutEverywhere: async () => {
      await apiClient.post(apiEndpoints.sessions.revokeAll);
      toast.success("All sessions revoked");
      clearAuthStorage();
      setTokenState(null);
      window.location.assign("/");
    },
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside AuthProvider");
  return context;
}
