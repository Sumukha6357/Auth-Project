import { apiEndpoints } from "@/lib/api/endpoints";
import { APP_CONFIG } from "@/lib/config";
import { createPkcePair } from "@/lib/auth/pkce";
import { getAuthMeta, setAuthMeta } from "@/lib/auth/storage";

function randomString() {
  return crypto.randomUUID().replace(/-/g, "");
}

export async function buildAuthorizeUrl() {
  const { codeVerifier, codeChallenge } = await createPkcePair();
  const state = randomString();
  const nonce = randomString();

  setAuthMeta({ state, nonce, codeVerifier });

  const params = new URLSearchParams({
    response_type: "code",
    client_id: APP_CONFIG.clientId,
    redirect_uri: APP_CONFIG.redirectUri,
    scope: APP_CONFIG.scopes,
    state,
    nonce,
    code_challenge: codeChallenge,
    code_challenge_method: "S256",
  });

  return `${apiEndpoints.oauth.authorize}?${params.toString()}`;
}

export function validateCallbackState(state?: string | null) {
  const meta = getAuthMeta();
  return !!state && !!meta?.state && state === meta.state;
}

export async function exchangeCodeForTokens(code: string) {
  const meta = getAuthMeta();
  const response = await fetch("/api/auth/exchange", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ code, codeVerifier: meta?.codeVerifier ?? "" }),
  });

  if (!response.ok) {
    throw new Error("Failed to exchange authorization code.");
  }

  return response.json();
}

export async function refreshAccessToken() {
  const response = await fetch("/api/auth/refresh", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  });

  if (!response.ok) throw new Error("Refresh failed.");
  return response.json();
}

export async function fetchUserInfo(accessToken: string) {
  const response = await fetch(apiEndpoints.oauth.userInfo, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  if (!response.ok) throw new Error("userinfo request failed");
  return response.json();
}

export async function revokeToken() {
  await fetch("/api/auth/revoke", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  });
}
