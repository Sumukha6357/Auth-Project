import { NextResponse } from "next/server";
import { APP_CONFIG } from "@/lib/config";
import { clearRefreshCookie, getRefreshCookie } from "@/lib/auth/server";

export async function POST() {
  const refreshToken = await getRefreshCookie();

  if (refreshToken) {
    const form = new URLSearchParams({
      token: refreshToken,
      token_type_hint: "refresh_token",
      client_id: APP_CONFIG.clientId,
    });

    await fetch(`${APP_CONFIG.idpBaseUrl}/oauth2/revoke`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: form,
      cache: "no-store",
    });
  }

  await clearRefreshCookie();
  return NextResponse.json({ revoked: true }, { status: 200 });
}
