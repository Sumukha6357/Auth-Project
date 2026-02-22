import { NextResponse } from "next/server";
import { APP_CONFIG } from "@/lib/config";
import { clearRefreshCookie, getRefreshCookie, setRefreshCookie } from "@/lib/auth/server";

export async function POST() {
  const refreshToken = await getRefreshCookie();
  if (!refreshToken) {
    return NextResponse.json({ error: "invalid_grant", error_description: "No refresh token cookie" }, { status: 400 });
  }

  const form = new URLSearchParams({
    grant_type: "refresh_token",
    client_id: APP_CONFIG.clientId,
    refresh_token: refreshToken,
  });

  const tokenResponse = await fetch(`${APP_CONFIG.idpBaseUrl}/oauth2/token`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: form,
    cache: "no-store",
  });

  const payload = await tokenResponse.json();
  if (!tokenResponse.ok) {
    await clearRefreshCookie();
    return NextResponse.json(payload, { status: tokenResponse.status });
  }

  if (payload.refresh_token) {
    await setRefreshCookie(payload.refresh_token);
  }

  return NextResponse.json(
    {
      access_token: payload.access_token,
      id_token: payload.id_token,
      expires_in: payload.expires_in,
      token_type: payload.token_type,
      scope: payload.scope,
    },
    { status: 200 }
  );
}
