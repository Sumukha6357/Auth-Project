import { NextResponse } from "next/server";
import { APP_CONFIG } from "@/lib/config";
import { clearRefreshCookie, setRefreshCookie } from "@/lib/auth/server";

export async function POST(request: Request) {
  const body = (await request.json()) as { code?: string; codeVerifier?: string };

  if (!body.code || !body.codeVerifier) {
    return NextResponse.json({ error: "invalid_request", error_description: "Missing code or code_verifier" }, { status: 400 });
  }

  const form = new URLSearchParams({
    grant_type: "authorization_code",
    client_id: APP_CONFIG.clientId,
    code: body.code,
    redirect_uri: APP_CONFIG.redirectUri,
    code_verifier: body.codeVerifier,
  });

  const tokenResponse = await fetch(`${APP_CONFIG.idpBaseUrl}/oauth2/token`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: form,
    cache: "no-store",
  });

  const payload = await tokenResponse.json();
  if (!tokenResponse.ok) {
    return NextResponse.json(payload, { status: tokenResponse.status });
  }

  if (payload.refresh_token) {
    await setRefreshCookie(payload.refresh_token);
  } else {
    await clearRefreshCookie();
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
