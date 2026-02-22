import { describe, expect, it } from "vitest";
import { decodeJwt } from "@/lib/auth/jwt";

function createToken(payload: Record<string, unknown>) {
  const header = Buffer.from(JSON.stringify({ alg: "none", typ: "JWT" })).toString("base64url");
  const body = Buffer.from(JSON.stringify(payload)).toString("base64url");
  return `${header}.${body}.`;
}

describe("jwt decode helper", () => {
  it("decodes payload", () => {
    const token = createToken({ sub: "123", permissions: ["admin:full_access"] });
    const decoded = decodeJwt(token);
    expect(decoded?.sub).toBe("123");
    expect(decoded?.permissions).toContain("admin:full_access");
  });
});
