import { cookies } from "next/headers";

const REFRESH_COOKIE = "idp_refresh_token";

function isSecure() {
  return process.env.NODE_ENV === "production";
}

export async function setRefreshCookie(refreshToken: string) {
  const store = await cookies();
  store.set(REFRESH_COOKIE, refreshToken, {
    httpOnly: true,
    secure: isSecure(),
    sameSite: "lax",
    path: "/",
    maxAge: 60 * 60 * 24 * 30,
  });
}

export async function getRefreshCookie() {
  const store = await cookies();
  return store.get(REFRESH_COOKIE)?.value ?? null;
}

export async function clearRefreshCookie() {
  const store = await cookies();
  store.set(REFRESH_COOKIE, "", {
    httpOnly: true,
    secure: isSecure(),
    sameSite: "lax",
    path: "/",
    maxAge: 0,
  });
}
