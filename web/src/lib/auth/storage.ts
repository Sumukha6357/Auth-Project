export type TokenSet = {
  accessToken: string;
  idToken?: string;
  expiresIn?: number;
  tokenType?: string;
};

const TOKEN_KEY = "idp_web_tokens";
const META_KEY = "idp_web_auth_meta";

let memoryTokenSet: TokenSet | null = null;

export function setTokens(tokens: TokenSet | null) {
  memoryTokenSet = tokens;
  if (typeof window === "undefined") return;
  if (!tokens) {
    sessionStorage.removeItem(TOKEN_KEY);
    return;
  }
  sessionStorage.setItem(TOKEN_KEY, JSON.stringify(tokens));
}

export function getTokens() {
  if (memoryTokenSet) return memoryTokenSet;
  if (typeof window === "undefined") return null;
  const raw = sessionStorage.getItem(TOKEN_KEY);
  if (!raw) return null;
  try {
    memoryTokenSet = JSON.parse(raw) as TokenSet;
    return memoryTokenSet;
  } catch {
    return null;
  }
}

export function setAuthMeta(meta: Record<string, string>) {
  if (typeof window === "undefined") return;
  sessionStorage.setItem(META_KEY, JSON.stringify(meta));
}

export function getAuthMeta() {
  if (typeof window === "undefined") return null;
  const raw = sessionStorage.getItem(META_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as Record<string, string>;
  } catch {
    return null;
  }
}

export function clearAuthStorage() {
  memoryTokenSet = null;
  if (typeof window === "undefined") return;
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(META_KEY);
}
