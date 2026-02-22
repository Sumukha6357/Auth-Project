const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";

function randomString(length: number) {
  const values = new Uint32Array(length);
  crypto.getRandomValues(values);
  return Array.from(values).map((x) => chars[x % chars.length]).join("");
}

async function sha256(input: string) {
  const encoder = new TextEncoder();
  const data = encoder.encode(input);
  return crypto.subtle.digest("SHA-256", data);
}

function toBase64Url(buffer: ArrayBuffer) {
  return btoa(String.fromCharCode(...new Uint8Array(buffer)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
}

export async function createPkcePair() {
  const codeVerifier = randomString(96);
  const hash = await sha256(codeVerifier);
  const codeChallenge = toBase64Url(hash);
  return { codeVerifier, codeChallenge };
}
